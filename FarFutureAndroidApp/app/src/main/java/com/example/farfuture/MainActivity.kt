package com.example.farfuture

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.TextView

import com.amplifyframework.api.rest.RestOptions
import com.amplifyframework.core.Amplify
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*
        Amplify.Auth.fetchAuthSession(
            { Log.i("AmplifyQuickstart", "Auth session = $it") },
            { Log.e("AmplifyQuickstart", "Failed to fetch auth session") }
        )
        */
        val app : FarFutureApp = application as FarFutureApp
        val dataDisplay : TextView = findViewById(R.id.topView)
        dataDisplay.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        val timeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd\nhh:mm:ss")
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


    private fun getTodo() {
        val request = RestOptions.builder()
            .addPath("/todo")
            .build()


        Amplify.API.get(request,
            { Log.i("MyAmplifyApp", "GET succeeded: $it") },
            { Log.e("MyAmplifyApp", "GET failed.", it) }
        )
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

    override fun onDestroy() {
        super.onDestroy()
        val app : FarFutureApp = application as FarFutureApp
        app.disconnectSocket()
    }

}