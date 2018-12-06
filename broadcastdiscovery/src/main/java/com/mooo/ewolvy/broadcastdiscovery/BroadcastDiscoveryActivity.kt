package com.mooo.ewolvy.broadcastdiscovery

import android.app.Activity
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_broadcast_discovery.*
import org.json.JSONObject
import java.lang.ref.WeakReference
import android.widget.ArrayAdapter
import android.util.Log
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.*


/**
**************************************************************************************************
This library will need some arguments when called with StartActivityForResult
and will return the server selected by the user, if any.

The needed parameters must be on a BUNDLE EXTRA called "BROADCAST_EXTRAS" and are:
broadcast.port: port number on which the servers are listening
broadcast.service: the service name you are looking for
broadcast.timeout: the maximum time to wait for a response from the servers (milliseconds in long)
broadcast.resend: time to wait for resend the broadcast petition (milliseconds in long)

The final result will be on the intent:
 - If was successful: Result will be RESULT_OK and data will contain:
    broadcast.server: the server information selected by the user (if any) as String
 - If error: Result will be RESULT_CANCELLED and data will contain a FetchDataErrorStatus code
*************************************************************************************************
*/


class BroadcastDiscoveryActivity : AppCompatActivity() {
    companion object {
        const val BROADCAST_EXTRAS = "BROADCAST_EXTRAS"
        const val BROADCAST_TAG = "BROADCAST_TAG"
        const val DEFAULT_TIMEOUT = 2000L
        const val ERROR_NO_SERVICE = "ERROR_NO_SERVICE"
        const val EXTRA_ERROR_CODE = "broadcast.error"
        const val EXTRA_SERVER = "broadcast.server"
        const val EXTRA_SERVICE = "broadcast.service"
        const val EXTRA_PORT = "broadcast.port"
        const val EXTRA_TIMEOUT = "broadcast.timeout"
        const val EXTRA_RESEND_TIME = "broadcast.resend"
    }

    private lateinit var serviceName: String
    private lateinit var arrayAdapter: ArrayAdapter<Server>
    private lateinit var fetchData: FetchData
    private var port: Int = 0
    private var timeOut: Long = 0
    private var resendTime: Long = 0

    private val serverList: ArrayList<Server> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_broadcast_discovery)

        getValuesFromIntent()

        if (serviceName == ERROR_NO_SERVICE) {
            returnWithError(FetchDataErrorStatus.INVALID_SERVICE)
            return
        }

        if (isWifiConnected()) {
            list_view.setOnItemClickListener{ _, _, position, _ ->
                onServerSelected(position)}
            arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, serverList)
            list_view.adapter = arrayAdapter
            fetchData = FetchData(this)
            fetchData.execute(serviceName, port.toString(), timeOut.toString(), resendTime.toString())
        } else {
            returnWithError(FetchDataErrorStatus.NO_WIFI_CONNECTION_ERROR)
        }
    }

    override fun onPause() {
        super.onPause()
        if (!fetchData.isCancelled) fetchData.cancel(true)
    }

    override fun onResume() {
        super.onResume()
        if (fetchData.isCancelled) fetchData.execute(serviceName, port.toString())
    }

    private fun returnWithError(errorCode: FetchDataErrorStatus){
        val result = intent
        result.putExtra(EXTRA_ERROR_CODE, errorCode)
        setResult(Activity.RESULT_CANCELED, result)
        finish()
    }

    private fun getValuesFromIntent(){
        val extras = intent.getBundleExtra(BROADCAST_EXTRAS)
        if (extras != null) {
            serviceName = extras.getString(EXTRA_SERVICE, ERROR_NO_SERVICE)
            port = extras.getInt(EXTRA_PORT, -1)
            timeOut = extras.getLong(EXTRA_TIMEOUT, DEFAULT_TIMEOUT)
            resendTime = extras.getLong(EXTRA_RESEND_TIME, DEFAULT_TIMEOUT)
        } else {
            serviceName = ERROR_NO_SERVICE
        }
    }

    private fun isWifiConnected(): Boolean{
        val cm = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.getNetworkCapabilities(cm.activeNetwork).hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    private fun addServer(server: JSONObject){
        serverList.add(Server (server.getJSONObject("META").getString("Description"), server))
        arrayAdapter.notifyDataSetChanged()
    }

    private fun onServerSelected(position: Int){
        intent.putExtra(EXTRA_SERVER, serverList[position].responseAsString())
        setResult(RESULT_OK, intent)
        Log.d(BROADCAST_TAG, "${serverList[position]} $RESULT_OK")
        finish()
    }

    private class FetchData internal constructor(context: BroadcastDiscoveryActivity): AsyncTask<String, JSONObject, FetchDataErrorStatus>(){

        private val activityReference: WeakReference<BroadcastDiscoveryActivity> = WeakReference(context)
        private val managedIps = arrayListOf<InetAddress>()

        override fun doInBackground(vararg arguments: String?): FetchDataErrorStatus {
            val activity = activityReference.get()?: return FetchDataErrorStatus.INVALID_ACTIVITY
            val broadcastAddress = getBroadcastAddress(activity)
            var timeStamp: Long = 0

            val sendData = arguments[0]?.toByteArray() ?: return FetchDataErrorStatus.INVALID_SEND_DATA
            val port = arguments[1]?.toInt() ?: return FetchDataErrorStatus.INVALID_PORT
            val timeout = arguments[2]?.toInt() ?: return FetchDataErrorStatus.INVALID_TIMEOUT
            val resendTime = arguments[3]?.toInt() ?: return FetchDataErrorStatus.INVALID_RESEND_TIME

            while (activityReference.get() != null && !isCancelled) {
                if (System.currentTimeMillis() - timeStamp > resendTime) {
                    val datagramSocket = DatagramSocket()
                    datagramSocket.broadcast = true
                    try {
                        val sendPacket = DatagramPacket(sendData, sendData.size, broadcastAddress, port)
                        datagramSocket.send(sendPacket)
                        timeStamp = System.currentTimeMillis()
                        Log.d(BROADCAST_TAG, "Request packet sent to: ${broadcastAddress.toString()}")
                    } catch (e: Exception) {
                        Log.d(BROADCAST_TAG, e.toString())
                        return FetchDataErrorStatus.INVALID_PORT
                    } finally {
                        datagramSocket.close()
                    }
                }
                val serverSocket = ServerSocket(19103)
                serverSocket.soTimeout = timeout
                var clientSocket: Socket? = null
                try {
                    clientSocket = serverSocket.accept()
                } catch (e: SocketTimeoutException) {
                    Log.d(BROADCAST_TAG, "Timeout reached")
                } finally {
                    serverSocket.close()
                }

                Log.d(BROADCAST_TAG, "Received from: ${clientSocket?.inetAddress.toString()}")
                if (clientSocket != null && !managedIps.any {it == clientSocket.inetAddress}) {
                    managedIps.add(clientSocket.inetAddress)
                    val br = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
                    val message = br.readLine()
                    clientSocket.close()
                    publishProgress(JSONObject(message))
                }
            }
            return FetchDataErrorStatus.NO_ERROR
        }

        private fun getBroadcastAddress(context: Context): InetAddress? {
            val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager?
            val dhcp = wifi?.dhcpInfo ?: return null

            val broadcast = dhcp.ipAddress and dhcp.netmask or dhcp.netmask.inv()
            val quads = ByteArray(4)
            for (k in 0..3)
                quads[k] = (broadcast shr k * 8).toByte()
            return InetAddress.getByAddress(quads)
        }

        override fun onProgressUpdate(vararg values: JSONObject?) {
            super.onProgressUpdate(*values)
            val activity: BroadcastDiscoveryActivity = activityReference.get()?: return
            if (activity.isFinishing) return
            activity.addServer(values[0]?: return)
        }

        override fun onPostExecute(result: FetchDataErrorStatus) {
            super.onPostExecute(result)
            val activity: BroadcastDiscoveryActivity = activityReference.get()?: return
            when (result){
                FetchDataErrorStatus.NO_ERROR -> Log.d(BROADCAST_TAG, "FetchData finished correctly")
                FetchDataErrorStatus.INVALID_SEND_DATA -> activity.returnWithError(FetchDataErrorStatus.INVALID_SEND_DATA)
                FetchDataErrorStatus.INVALID_PORT -> activity.returnWithError(FetchDataErrorStatus.INVALID_PORT)
                FetchDataErrorStatus.CLIENT_SOCKET_ERROR -> activity.returnWithError(FetchDataErrorStatus.CLIENT_SOCKET_ERROR)
                FetchDataErrorStatus.INVALID_ACTIVITY -> activity.returnWithError(FetchDataErrorStatus.INVALID_ACTIVITY)
                FetchDataErrorStatus.INVALID_TIMEOUT -> activity.returnWithError(FetchDataErrorStatus.INVALID_TIMEOUT)
                FetchDataErrorStatus.INVALID_RESEND_TIME -> activity.returnWithError(FetchDataErrorStatus.INVALID_RESEND_TIME)
                else -> activity.returnWithError(FetchDataErrorStatus.UNKNOWN_ERROR)
            }
        }
    }
}
