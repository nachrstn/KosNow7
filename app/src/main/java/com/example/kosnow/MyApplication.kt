package com.example.kosnow

import android.app.Application
import com.example.kosnow.Network.ConnectivityReceiver

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    fun setConnectivityListener(listener: ConnectivityReceiver.ConnectivityReceiverListener) {
        ConnectivityReceiver.connectivityReceiverListener = listener
    }

    companion object {
        @get:Synchronized
        var instance: MyApplication? = null
            private set
    }
}