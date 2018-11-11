package com.mooo.ewolvy.broadcastdiscovery

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.content.Intent
import android.widget.EditText


class BroadcastDiscovery : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_broadcast_discovery)
        findViewById<Button>(R.id.button_send).setOnClickListener{
            val texto = findViewById<EditText>(R.id.edit_text).text.toString()
            intent.putExtra("broadcast.server", texto)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }
}
