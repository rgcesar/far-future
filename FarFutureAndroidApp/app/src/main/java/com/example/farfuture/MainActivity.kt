package com.example.farfuture

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val app : FarFutureApp = application as FarFutureApp
        val dataDisplay : TextView = findViewById(R.id.topView)
        dataDisplay.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        val timeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd\nhh:mm:ss")

        // update live display of sensor data
        val updateHandler : Handler = Handler(Looper.getMainLooper())
        class UpdateRunner() : Runnable {
            override fun run() {
                if (app.Time.size > 0) {
                    val displayString: String = "" +
                            "Time:\n${app.Time[app.Time.size - 1].format(timeFormat)}\n" +
                            "Temperature: %.2f\n".format(app.Tempurature[app.Tempurature.size - 1]) +
                            "Pressure: %.2f\n".format(app.Pressure[app.Pressure.size - 1]) +
                            "Humidity: %.2f\n".format(app.Humidity[app.Humidity.size - 1]) +
                            "LightLevel: %.2f\n".format(app.LightLevel[app.LightLevel.size - 1]) +
                            "SoilMoisture: %.2f\n".format(app.soilMoisture[app.soilMoisture.size - 1])
                    dataDisplay.text = displayString
                }
                updateHandler.postDelayed(this, 500)
            }
        }
        updateHandler.post(UpdateRunner())
    }

    // Activity movement functions
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