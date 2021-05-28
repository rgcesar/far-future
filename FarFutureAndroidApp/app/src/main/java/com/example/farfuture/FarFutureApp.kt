package com.example.farfuture

import android.app.Application
import android.util.Log
import com.amplifyframework.AmplifyException
import com.amplifyframework.api.ApiException
import com.amplifyframework.api.aws.AWSApiPlugin
import com.amplifyframework.api.rest.RestOptions
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify.API
import com.amplifyframework.core.Amplify.Auth
import com.amplifyframework.kotlin.core.Amplify

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.net.URI
import java.net.URISyntaxException

import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONArray
import java.lang.Exception
import java.time.LocalDate


import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalUnit
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread


class FarFutureApp : Application() {

    private val URL = "http://e34feed038dc.ngrok.io/"
    private var socket : Socket? = null
    private val SOCKET_TAG = "SocketIO"
    private val API_TAG : String = "Amplify-API"

    var Time : MutableList<LocalDateTime> = ArrayList()
    var Tempurature : MutableList<Double> = ArrayList()
    var Pressure : MutableList<Double> = ArrayList()
    var Humidity : MutableList<Double> = ArrayList()
    var LightLevel : MutableList<Double> = ArrayList()
    var soilMoisture : MutableList<Double> = ArrayList()

    var graphTimeDelta : Long = 12


    override fun onCreate() {
        super.onCreate()

        // temp, pressure, humidity, light, waterlevel


        //val d1 : Date = parseDateString("2014-2-5 6:07:40")
        //val d2 : Date = parseDateString("2014-2-6 6:07:40")
        //val d3 : Date = parseDateString("2014-2-7 6:07:40")
        //val d4 : Date = parseDateString("2014-2-8 6:07:40")
        //val d5 : Date = parseDateString("2014-2-9 6:07:40")

        //Time = arrayListOf(d1, d2, d3, d4, d5)
        //Tempurature = arrayListOf(1, 2, 3, 4, 5)
        //Pressure = arrayListOf(2, 2, 5, 4, 5)
        //Humidity = arrayListOf(3, 2, 3, 4, 5)
        //LightLevel = arrayListOf(4, 2, -1, 4, 5)


        try {
            val uri: URI = URI.create(URL)
            val options = IO.Options()
            options.reconnection = true
            options.forceNew = true
            socket = IO.socket(uri, options)
            Log.d("SocketIO", "Setup run")
        } catch (e: URISyntaxException) {
            Log.e("SocketIO", "Failed to open socket", e)
        }
        val socket = socket

        if (socket != null) {

            socket.on(Socket.EVENT_CONNECT) {
                Log.i(SOCKET_TAG, "Socket connected.")


            }
            socket.on(Socket.EVENT_CONNECT_ERROR) {
                Log.e(SOCKET_TAG, "Socket connection error")
            }
            socket.on(Socket.EVENT_DISCONNECT) {
                Log.e(SOCKET_TAG, "Socket disconnected.")
            }
            socket.on("my response") { args ->
                //Log.d(SOCKET_TAG, args[0] as String)
                socket.emit("server", "server")

            }

            socket.on("client") { args ->
                Log.d(SOCKET_TAG, args[0] as String)
                // time format "%Y-%m-%d %H:%M:%S"

                val jsonObj = JSONObject(args[0] as String)
                val time = jsonObj.getString("time")
                val temp = jsonObj.getDouble("temp")
                val humidity = jsonObj.getDouble("humidity")
                val pressure = jsonObj.getDouble("pressure")
                val light = jsonObj.getDouble("lightlevel")
                val moisture = jsonObj.getDouble("soilmoist")
                Log.i(SOCKET_TAG, "Data received.")
                Time.add(parseDateString(time))
                Tempurature.add(temp)
                Pressure.add(pressure)
                Humidity.add(humidity)
                LightLevel.add(light)
                soilMoisture.add(moisture)
            }
            connectSocket()

            Log.i(SOCKET_TAG, "End setup.")
        } else {
            Log.e(SOCKET_TAG, "Socket not initialized.")
        }

        try {
            Amplify.addPlugin(AWSApiPlugin())
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.configure(applicationContext)
            Log.i("Amplify", "Initialized Amplify")
        } catch (error: AmplifyException) {
            Log.e("Amplify", "Could not initialize Amplify", error)
        }




        updateBackgroundData()


    }

    /*
    {
        "TableName": "sensors_ddb",
        "KeyConditionExpression": "timep = :v1",
        "ExpressionAttributeValues": {
            ":v1": {
                "S": "2021-05-13 15"
            }
        }
    }
    */

    fun updateBackgroundData() {
        val timearr = ArrayList<LocalDateTime>(Time.size)
           // LocalDateTime.of(Time[it].year, Time[it].month, Time[it].dayOfMonth, Time[it].hour, 0)
        Time.forEach() {
            val temp = it.truncatedTo(ChronoUnit.HOURS)
            if (!timearr.contains(temp)) {
                timearr.add(temp)
            }
        }
        var currentTime = LocalDateTime.now()
        currentTime = currentTime.truncatedTo(ChronoUnit.HOURS)
        var oldTime = LocalDateTime.now().minus(graphTimeDelta, ChronoUnit.HOURS).truncatedTo(ChronoUnit.HOURS)
        val getTimes : ArrayList<LocalDateTime?> = ArrayList<LocalDateTime?>()
        if (oldTime != null) {

            while (oldTime != currentTime.plus(1, ChronoUnit.HOURS) && oldTime <= currentTime) {
                if (!timearr.contains(oldTime)) {
                    getTimes.add(oldTime)
                }
                oldTime = oldTime.plus(1, ChronoUnit.HOURS)
            }
        }
        Log.d("Amplify-API", "getTimes to be fetched: $getTimes")
        //2021-05-13 16
        //year-month-day hour
        val getQueue : PriorityQueue<String> = PriorityQueue()
        getTimes.forEach() {
            var res : String = ""
            val queryFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH")
            if (it != null) {
                res = it.format(queryFormat)
            }
            /*
            if (it?.monthValue!! < 10) {
                res = "${it?.year}-0${it?.monthValue}-${it?.dayOfMonth} ${it?.hour}"
            }
            else {
                res = "${it?.year}-${it?.monthValue}-${it?.dayOfMonth} ${it?.hour}"
            }
             */

            getQueue.add(res)
        }

        Log.d(API_TAG, "querystring: ${getQueue.toString()}")
        if (!getQueue.isEmpty()) {
            //val newDataList: ArrayList<JSONObject?> =
            fetchData(getQueue)
            //Log.d(API_TAG, "start parse")
            //newDataList

        }
    }

    private fun connectSocket() {
        socket?.connect()
    }

    fun disconnectSocket() {
        socket?.disconnect()
        socket?.off()
    }

    fun getSocket() : Socket? {
        if (socket != null) {
            return socket
        } else {
            Log.e("SocketIO", "getSocket call with un-initialized socket")
        }
        return null
    }


    private fun parseDateString (dateString : String) : LocalDateTime {
        //"%Y-%m-%d %H:%M:%S"
        var year : Int = 0
        var month : Int = 0
        var date : Int = 0
        var hour : Int = 0
        var minute : Int = 0
        var second : Int = 0
        val split = dateString.split("-", " ", ":")
        year = split[0].toInt()
        month = split[1].toInt()
        date = split[2].toInt()
        hour = split[3].toInt()
        minute = split[4].toInt()
        second = split[5].toInt()
        val d1 : LocalDateTime = LocalDateTime.of(year, month, date, hour, minute, second)
        return d1
        //return Date.from(d1.atZone(ZoneId.systemDefault()).toInstant())
    }


    private fun fetchData(getQueue : PriorityQueue<String>)= runBlocking {


        val respData : ArrayList<JSONObject?> = ArrayList()
        val serverGet : Deferred<ArrayList<JSONObject?>> = async {

            while (!getQueue.isEmpty()) {
                val respObj : JSONObject? = letsGet(getQueue.remove())
                if (respObj != null) {
                    respData.add(respObj)
                }
            }
            respData
        }
        Log.i(API_TAG, "JSON obj count: ${serverGet.await().size}")


        val timeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH")
        serverGet.await().forEach {
            try {
                Log.i(API_TAG, "JSONObj string: ${it.toString()}")

                val count : Int? = it?.getInt("Count")
                Log.i(API_TAG, "JSON obj count: $count")

                val items : JSONArray? = it?.getJSONArray("Items")
                Log.i(API_TAG, "items: ${items.toString()}")

                val payload: JSONObject? = items?.getJSONObject(0)?.getJSONObject("payload")?.getJSONObject("M")

                Log.i(API_TAG, "payload: ${payload.toString()}")
                if (payload != null) {
                    val timeString = payload.getJSONObject("timep").getString("S")
                    Log.i(API_TAG, "timeString: $timeString")

                    val dateTime = LocalDateTime.parse(timeString, timeFormat)
                    Log.i(API_TAG, dateTime.format(timeFormat))

                    Log.i(API_TAG, "payload: ${payload.toString()}")
                    Time.add(dateTime)
                    Tempurature.add(payload.getJSONObject("temp").getDouble("S"))
                    Pressure.add(payload.getJSONObject("pressure").getDouble("S"))
                    Humidity.add(payload.getJSONObject("humidity").getDouble("S"))
                    LightLevel.add(payload.getJSONObject("lightlevel").getDouble("S"))
                    soilMoisture.add(payload.getJSONObject("soilmoist").getDouble("S"))
                }


            } catch (e : Exception) {
                Log.e(API_TAG, "Failed to extract data from JSON.")
                Log.e(API_TAG, "Error: ${e.toString()}")
                Log.e(API_TAG, "${e.stackTrace}")
                Log.e(API_TAG, "${e.message}")
            }

        }
        Log.i(API_TAG, "Parse done.")
        Log.i(API_TAG, "Time: ${Time.toString()}")
        Log.i(API_TAG, "Tempurature: ${Tempurature.toString()}")
        Log.i(API_TAG, "Pressure: ${Pressure.toString()}")

        Log.d(API_TAG, "sizes: time:${Time.size}, pressure:${Pressure.size}, tempurature:${Tempurature.size}," +
                "\nhumidity:${Humidity.size}, lightlevel:${LightLevel.size}, soilmoisture:${soilMoisture.size}")



    }

    private suspend fun letsGet (queryString : String) : JSONObject? {
        val request = RestOptions.builder()
            .addPath("/plantdata")
            .addQueryParameters(mapOf("time" to queryString))
            .build()

        try {
            val response = Amplify.API.get(request)
            Log.i("Amplify", "GET succeeded: ${response.data}")
            return response.data.asJSONObject()
        } catch (error : ApiException) {
            Log.e("Amplify", "GET failed", error)
        }
        return null

    }
}


