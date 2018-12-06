package com.mooo.ewolvy.broadcastdiscoverytests

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.support.design.widget.Snackbar
import android.util.Log
import com.mooo.ewolvy.broadcastdiscovery.BroadcastDiscoveryActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject


const val REQUEST_CODE_BCD = 1
const val TEST_TAG = "TEST_TAG"

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button_test.setOnClickListener {testBroadcastDiscovery()}
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val response = data?.getStringExtra(BroadcastDiscoveryActivity.EXTRA_SERVER)
        if (requestCode == REQUEST_CODE_BCD && resultCode == RESULT_OK && response != null) {
            Snackbar.make(
                root_layout, // Parent view
                JSONObject(response).getJSONObject("META").getString("Description"), // Message to show
                Snackbar.LENGTH_LONG // How long to display the message.
            ).show()
            Log.d(TEST_TAG, "Message shown")
        } else {
            Log.d(TEST_TAG, "Not shown: $response $resultCode ${data == null}")
        }
    }

    private fun testBroadcastDiscovery (){
        val intent = Intent(this@MainActivity, BroadcastDiscoveryActivity::class.java)
        val extras = Bundle()
        extras.putString(BroadcastDiscoveryActivity.EXTRA_SERVICE, edit_service.text.toString())
        extras.putInt(BroadcastDiscoveryActivity.EXTRA_PORT, edit_port.text.toString().toInt())
        extras.putLong(BroadcastDiscoveryActivity.EXTRA_TIMEOUT, edit_timeout.text.toString().toLong())

        intent.putExtra(BroadcastDiscoveryActivity.BROADCAST_EXTRAS, extras)

        startActivityForResult(intent, REQUEST_CODE_BCD)
    }
}
