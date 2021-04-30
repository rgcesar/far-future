package com.example.farfuture

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View

import io.socket.client.Socket
import io.socket.emitter.Emitter;

class Commands : AppCompatActivity() {
    private val SOCKET_TAG = "SocketIO"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_commands)
        val app : FarFutureApp = application as FarFutureApp
        val socket : Socket? = app.getSocket()





    }

    fun send() {
        val app : FarFutureApp = application as FarFutureApp
        val message = "test message"
        val socket : Socket? = app.getSocket()
        if (TextUtils.isEmpty(message) || !(socket!!.connected())) {
            Log.e(SOCKET_TAG, "Send failed")
            return
        }


        socket.emit("test message", message)
    }


    fun onBackClick (view : View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        val app : FarFutureApp = application as FarFutureApp
        app.disconnectSocket()
    }
}