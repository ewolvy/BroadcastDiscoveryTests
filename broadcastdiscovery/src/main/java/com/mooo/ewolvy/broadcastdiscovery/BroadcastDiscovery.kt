package com.mooo.ewolvy.broadcastdiscovery

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText


/**
********************************************************************************
This library will need some arguments when called with StartActivityForResult
and will return the server selected by the user, if any.

The needed parameters are:
broadcast.port: port number on which the servers are listening
broadcast.service: the service name you are looking for
broadcast.maxTimeout: the maximum time to wait for a response from the servers

The final result will be on the intent:
broadcast.server: the server information selected by the user (if any) as String
********************************************************************************
*/

const val BUNDLE_EXTRAS = "BUNDLE_EXTRAS"

class BroadcastDiscovery : AppCompatActivity() {

    private lateinit var serviceName: String
    private var port: Int = 0
    private var timeOut: Int = 0

    private val serverList: ArrayList<Server> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_broadcast_discovery)

        getValuesFromIntent()
    }

    private fun getValuesFromIntent(){
        val extras = intent.getBundleExtra(BUNDLE_EXTRAS)
        if (extras != null) {
            serviceName = extras.getString("broadcast.service", "NO_SERVICE_ERROR")
            port = extras.getInt("broadcast.port", 0)
            timeOut = extras.getInt("broadcast.maxTimeout", 10000)
        } else {
            port = 0
            serviceName = "NO_SERVICE_ERROR"
            timeOut = 10000
        }
    }
}
