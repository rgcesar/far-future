package com.example.farfuture

import android.app.Application
import android.util.Log
import com.amplifyframework.AmplifyException
import com.amplifyframework.api.ApiException
import com.amplifyframework.api.aws.AWSApiPlugin
import com.amplifyframework.api.rest.RestOptions
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
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


import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.collections.ArrayList


class FarFutureApp : Application() {

    // website url (changes on system reboot and must be manual updated)
    private val URL = "http://e2110d330fa3.ngrok.io/"

    //
    private var socket : Socket? = null
    private val SOCKET_TAG = "SocketIO"
    private val API_TAG : String = "Amplify-API"

    // Lists to contain the sensor data and timestamps
    var Time : MutableList<LocalDateTime> = ArrayList()
    var Tempurature : MutableList<Double> = ArrayList()
    var Pressure : MutableList<Double> = ArrayList()
    var Humidity : MutableList<Double> = ArrayList()
    var LightLevel : MutableList<Double> = ArrayList()
    var soilMoisture : MutableList<Double> = ArrayList()

    // How long into the past historical data is fetched
    // for, in hours
    var graphTimeDelta : Long = 12


    override fun onCreate() {
        super.onCreate()

        // Open socket
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

        // Setup event responses
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
                socket.emit("server", "server")
            }
            // Receive data
            socket.on("client") { args ->
                Log.d(SOCKET_TAG, args[0] as String)
                //  parse json
                val jsonObj = JSONObject(args[0] as String)
                val time = jsonObj.getString("time")
                val temp = jsonObj.getDouble("temp")
                val humidity = jsonObj.getDouble("humidity")
                val pressure = jsonObj.getDouble("pressure")
                val light = jsonObj.getDouble("lightlevel")
                val moisture = jsonObj.getDouble("soilmoist")

                Log.i(SOCKET_TAG, "Data received.")
                val localtime = parseDateString(time)
                // add data to lists
                if (Time.contains(localtime)) {
                    val index = Time.indexOf(localtime)
                    Tempurature[index] = (Tempurature[index] + temp) / 2
                    Pressure[index] = (Pressure[index] + pressure) / 2
                    Humidity[index] = (Humidity[index] + humidity) / 2
                    LightLevel[index] = (LightLevel[index] + light) / 2
                    soilMoisture[index] = (soilMoisture[index] + moisture) / 2
                }
                else {
                    Time.add(localtime)
                    Tempurature.add(temp)
                    Pressure.add(pressure)
                    Humidity.add(humidity)
                    LightLevel.add(light)
                    soilMoisture.add(moisture)
                }
            }

            Log.i(SOCKET_TAG, "End setup.")
        } else {
            Log.e(SOCKET_TAG, "Socket not initialized.")
        }

        // Initialize AWS amplify
        try {
            Amplify.addPlugin(AWSApiPlugin())
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.configure(applicationContext)
            Log.i("Amplify", "Initialized Amplify")
        } catch (error: AmplifyException) {
            Log.e("Amplify", "Could not initialize Amplify", error)
        }

        // fetch historical data
        updateBackgroundData()
    }

    fun updateBackgroundData() {
        val timearr = ArrayList<LocalDateTime>(Time.size)
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
        val getQueue : PriorityQueue<String> = PriorityQueue()
        getTimes.forEach() {
            var res : String = ""
            val queryFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH")
            if (it != null) {
                res = it.format(queryFormat)
            }
            getQueue.add(res)
        }

        Log.d(API_TAG, "querystring: ${getQueue.toString()}")
        if (!getQueue.isEmpty()) {
            fetchData(getQueue)
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
        // Parse a date string in the format:
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
        return LocalDateTime.of(year, month, date, hour, minute, second)
    }

    // function to call async task
    private fun fetchData(getQueue : PriorityQueue<String>)= runBlocking {
        val respData : ArrayList<JSONObject?> = ArrayList()
        val serverGet : Deferred<ArrayList<JSONObject?>> = async {
            while (!getQueue.isEmpty()) {
                val respObj : JSONObject? = get(getQueue.remove())
                if (respObj != null) {
                    respData.add(respObj)
                }
            }
            respData
        }

        Log.i(API_TAG, "JSON obj count: ${serverGet.await().size}")
        val timeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH")

        // Parse all responses
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
                    // add data to lists
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

        connectSocket()

        Log.i(API_TAG, "Parse done.")
        Log.i(API_TAG, "Time: ${Time.toString()}")
        Log.i(API_TAG, "Tempurature: ${Tempurature.toString()}")
        Log.i(API_TAG, "Pressure: ${Pressure.toString()}")
        Log.d(API_TAG, "sizes: time:${Time.size}, pressure:${Pressure.size}, tempurature:${Tempurature.size}," +
                "\nhumidity:${Humidity.size}, lightlevel:${LightLevel.size}, soilmoisture:${soilMoisture.size}")
    }

    private suspend fun get (queryString : String) : JSONObject? {
        // build AWS get request
        val request = RestOptions.builder()
            .addPath("/plantdata")
            .addQueryParameters(mapOf("time" to queryString))
            .build()

        // attempt request through amplify
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


