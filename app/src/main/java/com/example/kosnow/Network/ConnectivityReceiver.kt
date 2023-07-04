package com.example.kosnow.Network

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import com.example.kosnow.MyApplication

class ConnectivityReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, arg1: Intent) {
        val cm: ConnectivityManager = context
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        val isConnected = (activeNetwork != null
                && activeNetwork.isConnectedOrConnecting)
        if (connectivityReceiverListener != null) {
            connectivityReceiverListener!!.onNetworkConnectionChanged(isConnected)
        }
    }

    interface ConnectivityReceiverListener {
        fun onNetworkConnectionChanged(isConnected: Boolean)
    }

    companion object {
        var connectivityReceiverListener: ConnectivityReceiverListener? = null
        val isConnected: Boolean
            get() {
                return true
//                val cm: ConnectivityManager = MyApplication.instance?.applicationContext
//                    ?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//                val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
//                return (activeNetwork != null
//                        && activeNetwork.isConnectedOrConnecting)
            }
    }
}