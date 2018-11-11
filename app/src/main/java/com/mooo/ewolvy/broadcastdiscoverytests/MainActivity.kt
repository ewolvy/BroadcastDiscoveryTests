package com.mooo.ewolvy.broadcastdiscoverytests

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.content.Intent
import android.support.design.widget.Snackbar
import com.mooo.ewolvy.broadcastdiscovery.BroadcastDiscovery

const val BUNDLE_EXTRAS = "BUNDLE_EXTRAS"
const val REQUEST_CODE_BCD = 1

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.button_test).setOnClickListener {
            val intent = Intent(this@MainActivity, BroadcastDiscovery::class.java)
            val extras = Bundle()
            extras.putString("broadcast.service", "BROADCAST_REALREMOTE")
            extras.putString("broadcast.port", "19103")
            extras.putInt("broadcast.maxTimeout", 5000)

            intent.putExtra(BUNDLE_EXTRAS, extras)

            startActivityForResult(intent, REQUEST_CODE_BCD)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_BCD && resultCode == RESULT_OK && data != null) Snackbar.make(
            findViewById(R.id.root_layout), // Parent view
            data.getStringExtra("broadcast.server"), // Message to show
            Snackbar.LENGTH_SHORT // How long to display the message.
        ).show()
    }
}
