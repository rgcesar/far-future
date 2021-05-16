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





    }

    /*
    fun send_light_on_command(view: View) {
        val app : FarFutureApp = application as FarFutureApp
        val socket : Socket? = app.getSocket()
        if (!(socket!!.connected())) {
            Log.e(SOCKET_TAG, "Send failed")
            return
        }
        socket.emit("lighton")
    }

    fun send_light_off_command(view: View) {
        val app : FarFutureApp = application as FarFutureApp
        val socket : Socket? = app.getSocket()
        if (!(socket!!.connected())) {
            Log.e(SOCKET_TAG, "Send failed")
            return
        }
        socket.emit("lightoff")
    }

    fun send_water_command(view: View) {
        val app : FarFutureApp = application as FarFutureApp
        val socket : Socket? = app.getSocket()
        if (!(socket!!.connected())) {
            Log.e(SOCKET_TAG, "Send failed")
            return
        }
        socket.emit("water")
    }

    fun send_fan_on_command(view: View) {
        val app : FarFutureApp = application as FarFutureApp
        val socket : Socket? = app.getSocket()
        if (!(socket!!.connected())) {
            Log.e(SOCKET_TAG, "Send failed")
            return
        }
        socket.emit("fanon")
    }

    fun send_fan_off_command(view: View) {
        val app : FarFutureApp = application as FarFutureApp
        val socket : Socket? = app.getSocket()
        if (!(socket!!.connected())) {
            Log.e(SOCKET_TAG, "Send failed")
            return
        }
        socket.emit("fanoff")
    }
    */

    fun onBackClick (view : View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        val app : FarFutureApp = application as FarFutureApp
        //app.disconnectSocket()
    }
}