package com.example.farfuture

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View

import com.amazonaws.mobile.auth.core.signin.AuthException
import com.amplifyframework.AmplifyException
import com.amplifyframework.api.aws.AWSApiPlugin
import com.amplifyframework.api.rest.RestOptions
import com.amplifyframework.core.Amplify

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

}