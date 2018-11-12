package com.mooo.ewolvy.broadcastdiscovery

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.R.attr.port



/**
 *******************************************************************************
This library will need some arguments when called with StartActivityForResult
and will return the server selected by the user, if any.

The needed parameters are:
broadcast.port: port number on which the servers are listening
broadcast.service: the service name you are looking for
broadcast.maxTimeout: the maximum time to wait for a response from the servers

The final result will be on the intent:
broadcast.server: the server information selected by the user (if any) as String
 *******************************************************************************
 */

const val BUNDLE_EXTRAS = "BUNDLE_EXTRAS"

class BroadcastDiscovery : AppCompatActivity() {

    private lateinit var serviceName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_broadcast_discovery)

        getValuesFromIntent()

        findViewById<Button>(R.id.button_send).setOnClickListener{onSendButtonClick()}
    }

    private fun getValuesFromIntent(){
        val extras = intent.getBundleExtra(BUNDLE_EXTRAS)
        if (extras != null) {
            port = extras.getString("broadcast.port", "").toInt()
            serviceName = extras.getString("broadcast.service", "NO_SERVICE_ERROR")
            maxWaitTime = extras.getInt("broadcast.maxtimeout", 10000)
        } else {
            port = ""
            service = "NO_SERVICE"
            maxWaitTime = 10000
        }

        serviceName = ""
    }

    private fun onSendButtonClick(){
        val texto = findViewById<EditText>(R.id.edit_text).text.toString()
        intent.putExtra("broadcast.server", texto)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}
