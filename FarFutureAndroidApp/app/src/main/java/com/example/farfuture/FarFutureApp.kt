package com.example.farfuture

import android.app.Application
import android.util.Log
import com.amplifyframework.api.rest.RestOptions
import com.amplifyframework.kotlin.core.Amplify
import io.socket.client.IO
import io.socket.client.Socket
import java.net.URI
import java.net.URISyntaxException

class FarFutureApp : Application() {

    private val URL = "http://4e2fb8b750fc.ngrok.io/"
    private var socket : Socket? = null
    private val SOCKET_TAG = "SocketIO"

    override fun onCreate() {
        super.onCreate()


        // temp, pressure, humidity, light
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
                Log.d(SOCKET_TAG, args[0] as String)
            }
            socket.on("client") { args ->
                Log.d(SOCKET_TAG, args[0] as String)
            }
            connectSocket()
        } else {
            Log.e(SOCKET_TAG, "Socket not initialized.")
        }

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
}