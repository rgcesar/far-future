package com.example.farfuture

import android.app.Application
import android.util.Log
import com.amplifyframework.api.rest.RestOptions
import com.amplifyframework.kotlin.core.Amplify
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import java.net.URI
import java.net.URISyntaxException


import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.FormatStyle
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread


class FarFutureApp : Application() {

    private val URL = "http://b14b098b05d2.ngrok.io/"
    private var socket : Socket? = null
    private val SOCKET_TAG = "SocketIO"

    var Time : MutableList<Date> = ArrayList<Date>()
    var Tempurature : MutableList<Int> = ArrayList<Int>()
    var Pressure : MutableList<Int> = ArrayList<Int>()
    var Humidity : MutableList<Int> = ArrayList<Int>()
    var LightLevel : MutableList<Int> = ArrayList<Int>()

    override fun onCreate() {
        super.onCreate()


        // temp, pressure, humidity, light, waterlevel


        val d1 : Date = parseDateString("2014-2-5 6:07:40")
        val d2 : Date = parseDateString("2014-2-6 6:07:40")
        val d3 : Date = parseDateString("2014-2-7 6:07:40")
        val d4 : Date = parseDateString("2014-2-8 6:07:40")
        val d5 : Date = parseDateString("2014-2-9 6:07:40")

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
                val temp = jsonObj.getInt("temp")
                val humidity = jsonObj.getInt("humidity")
                val pressure = jsonObj.getInt("pressure")
                val light = jsonObj.getInt("lightlevel")
                Log.i(SOCKET_TAG, "Data received.")
                Time.add(parseDateString(time))
                Tempurature.add(temp)
                Pressure.add(pressure)
                Humidity.add(humidity)
                LightLevel.add(light)


            }
            connectSocket()

            Log.d(SOCKET_TAG, "End setup.")
        } else {
            Log.e(SOCKET_TAG, "Socket not initialized.")
        }
        Log.d(SOCKET_TAG, "End setup.")





        /*
        try {
            Amplify.addPlugin(AWSApiPlugin())
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.configure(applicationContext)
            Log.i("Amplify", "Initialized Amplify")
        } catch (error: AmplifyException) {
            Log.e("Amplify", "Could not initialize Amplify", error)
        }


        val request = RestOptions.builder()
            .addPath("/todo/1")
            .addBody("{\"name\":\"Mow the lawn\"}".toByteArray())
            .build()
        */
    }

    fun connectSocket() {
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

    suspend fun  put(request: RestOptions) {
        Amplify.API.put(request,
            "api"
        )
    }

    fun parseDateString (dateString : String) : Date {
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
        return Date.from(d1.atZone(ZoneId.systemDefault()).toInstant())
    }
}


