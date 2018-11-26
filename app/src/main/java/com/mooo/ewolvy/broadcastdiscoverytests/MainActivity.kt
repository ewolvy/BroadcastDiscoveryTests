package com.mooo.ewolvy.broadcastdiscoverytests

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.support.design.widget.Snackbar
import android.widget.EditText
import com.mooo.ewolvy.broadcastdiscovery.BroadcastDiscoveryActivity
import kotlinx.android.synthetic.main.activity_main.*

const val BROADCAST_EXTRAS = "BROADCAST_EXTRAS"
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
        val intent = Intent(this@MainActivity, BroadcastDiscoveryActivity::class.java)
        val extras = Bundle()
        extras.putString(BroadcastDiscoveryActivity.EXTRA_SERVICE, edit_service.text.toString())
        extras.putInt(BroadcastDiscoveryActivity.EXTRA_PORT, edit_port.text.toString().toInt())
        extras.putLong(BroadcastDiscoveryActivity.EXTRA_TIMEOUT, edit_timeout.text.toString().toLong())

        intent.putExtra(BROADCAST_EXTRAS, extras)

        startActivityForResult(intent, REQUEST_CODE_BCD)
    }
}
