package com.example.farfuture

import android.app.Application
import android.util.Log
import com.amplifyframework.AmplifyException
import com.amplifyframework.core.Amplify
import com.amplifyframework.api.aws.AWSApiPlugin

class AmplifyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        try {
            Amplify.addPlugin(AWSApiPlugin())
            Amplify.configure(applicationContext)
            Log.i("Amplify", "Initialized Amplify")
        } catch (error: AmplifyException) {
            Log.e("Amplify", "Could not initialize Amplify", error)
        }
    }
}