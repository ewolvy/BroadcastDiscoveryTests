package com.mooo.ewolvy.broadcastdiscoverytests

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.content.Intent
import android.support.design.widget.Snackbar
import android.widget.EditText
import com.mooo.ewolvy.broadcastdiscovery.BroadcastDiscovery
import kotlinx.android.synthetic.main.activity_main.*

const val BUNDLE_EXTRAS = "BUNDLE_EXTRAS"
const val REQUEST_CODE_BCD = 1

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button_test.setOnClickListener {testBroadcastDiscovery()}
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_BCD && resultCode == RESULT_OK && data != null) Snackbar.make(
            root_layout, // Parent view
            data.getStringExtra("broadcast.server"), // Message to show
            Snackbar.LENGTH_SHORT // How long to display the message.
        ).show()
    }

    private fun testBroadcastDiscovery (){
        val intent = Intent(this@MainActivity, BroadcastDiscovery::class.java)
        val extras = Bundle()
        extras.putString("broadcast.service", findViewById<EditText>(R.id.edit_service).text.toString())
        extras.putInt("broadcast.port", findViewById<EditText>(R.id.edit_port).text.toString().toInt())
        extras.putInt("broadcast.maxTimeout", findViewById<EditText>(R.id.edit_timeout).text.toString().toInt())

        intent.putExtra(BUNDLE_EXTRAS, extras)

        startActivityForResult(intent, REQUEST_CODE_BCD)
    }
}
