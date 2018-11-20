package com.mooo.ewolvy.broadcastdiscovery

import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_broadcast_discovery.*
import org.json.JSONObject
import java.lang.ref.WeakReference


/**
************************************************************************************
This library will need some arguments when called with StartActivityForResult
and will return the server selected by the user, if any.

The needed parameters must be on a BUNDLE EXTRA called "BROADCAST_EXTRAS" and are:
broadcast.port: port number on which the servers are listening
broadcast.service: the service name you are looking for
broadcast.maxTimeout: the maximum time to wait for a response from the servers

The final result will be on the intent:
broadcast.server: the server information selected by the user (if any) as String
broadcast.status: OK or ERROR_XXXX [where XXXX = error code] as String
************************************************************************************
*/

const val BROADCAST_EXTRAS = "BROADCAST_EXTRAS"
const val ERROR_NO_SERVICE = "ERROR_NO_SERVICE"

class BroadcastDiscoveryActivity : AppCompatActivity() {

    private lateinit var serviceName: String
    private var port: Int = 0
    private var timeOut: Int = 0

    private val serverList: ArrayList<Server> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_broadcast_discovery)

        getValuesFromIntent()

        if (serviceName == ERROR_NO_SERVICE) {
            //TODO: Manage wrong calling to the library
        }

        list_view.setOnClickListener{onServerSelected(it)}
    }

    private fun getValuesFromIntent(){
        val extras = intent.getBundleExtra(BROADCAST_EXTRAS)
        if (extras != null) {
            serviceName = extras.getString("broadcast.service", ERROR_NO_SERVICE)
            port = extras.getInt("broadcast.port", 0)
            timeOut = extras.getInt("broadcast.maxTimeout", 10000)
        } else {
            serviceName = ERROR_NO_SERVICE
        }
    }

    private fun addServer(server: JSONObject){
        serverList.add(Server (server.getString("Description"), server))
    }

    private fun onServerSelected(v: View){
        //TODO: manage server selection and return to calling Activity
    }

    private class FetchData internal constructor(context: BroadcastDiscoveryActivity): AsyncTask<String, JSONObject, Unit>(){

        private val activityReference: WeakReference<BroadcastDiscoveryActivity> = WeakReference(context)

        override fun doInBackground(vararg p0: String?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onProgressUpdate(vararg values: JSONObject?) {
            super.onProgressUpdate(*values)
            val activity = activityReference.get()
            if (activity == null || activity.isFinishing) return
            activity.addServer(values[0]!!)
        }
    }
}
