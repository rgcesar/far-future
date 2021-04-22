package com.example.farfuture

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun toGraphClick (view : View) {
        val intent = Intent(this, DataGraph::class.java)
        startActivity(intent)
    }

    fun toCommandsClick (view : View) {
        val intent = Intent(this, Commands::class.java)
        startActivity(intent)
    }

    fun toSettingsClick (view : View) {
        val intent = Intent(this, Settings::class.java)
        startActivity(intent)
    }

}