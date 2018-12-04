package com.mooo.ewolvy.broadcastdiscovery

import android.app.Activity
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_broadcast_discovery.*
import org.json.JSONObject
import java.lang.ref.WeakReference
import android.widget.ArrayAdapter
import android.support.design.widget.Snackbar
import android.util.Log
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.*


/**
************************************************************************************
This library will need some arguments when called with StartActivityForResult
and will return the server selected by the user, if any.

The needed parameters must be on a BUNDLE EXTRA called "BROADCAST_EXTRAS" and are:
broadcast.port: port number on which the servers are listening
broadcast.service: the service name you are looking for
broadcast.timeout: the maximum time to wait for a response from the servers

The final result will be on the intent:
broadcast.server: the server information selected by the user (if any) as String
broadcast.status: OK or ERROR_XXXX [where XXXX = error code] as String
************************************************************************************
*/


class BroadcastDiscoveryActivity : AppCompatActivity() {
    companion object {
        const val BROADCAST_EXTRAS = "BROADCAST_EXTRAS"
        const val BROADCAST_TAG = "BROADCAST_TAG"
        const val DEFAULT_TIMEOUT = 2000L
        const val ERROR_NO_SERVICE = "ERROR_NO_SERVICE"
        const val EXTRA_SERVICE = "broadcast.service"
        const val EXTRA_PORT = "broadcast.port"
        const val EXTRA_TIMEOUT = "broadcast.timeout"
    }

    private lateinit var serviceName: String
    private lateinit var arrayAdapter: ArrayAdapter<Server>
    private var port: Int = 0
    private var timeOut: Long = 0

    private val serverList: ArrayList<Server> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_broadcast_discovery)

        getValuesFromIntent()

        if (serviceName == ERROR_NO_SERVICE) {
            //TODO ("Manage wrong calling to the library")
        }

        if (isWifiConnected()) {
            list_view.setOnItemClickListener{ parent, view, position, id ->
                onServerSelected(parent, view, position, id)}
            arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, serverList)
            list_view.adapter = arrayAdapter
            val fetchData = FetchData(this)
            fetchData.execute("BROADCAST_REALREMOTE", port.toString())
        } else {
            //TODO("Manage no Wifi connection")
        }
    }

    private fun getValuesFromIntent(){
        val extras = intent.getBundleExtra(BROADCAST_EXTRAS)
        if (extras != null) {
            serviceName = extras.getString(EXTRA_SERVICE, ERROR_NO_SERVICE)
            port = extras.getInt(EXTRA_PORT, 0)
            timeOut = extras.getLong(EXTRA_TIMEOUT, DEFAULT_TIMEOUT)
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

    /*private fun addServer(server: String){
        serverList.add(Server (server, null))
        arrayAdapter.notifyDataSetChanged()
    }*/

    private fun onServerSelected(parent: View, view: View, position: Int, id: Long){
        //TODO("manage server selection and return to calling Activity")
        Snackbar.make(
            parent, // Parent view
            "Prueba $position", // Message to show
            Snackbar.LENGTH_LONG // How long to display the message.
        ).show()
    }

    private class FetchData internal constructor(context: BroadcastDiscoveryActivity): AsyncTask<String, JSONObject, FetchDataErrorStatus>(){

        private val activityReference: WeakReference<BroadcastDiscoveryActivity> = WeakReference(context)
        private val managedIps = arrayListOf<InetAddress>()

        override fun doInBackground(vararg arguments: String?): FetchDataErrorStatus {
            val activity = activityReference.get()?: return FetchDataErrorStatus.INVALID_ACTIVITY
            val broadcastAddress = getBroadcastAddress(activity)

            val datagramSocket = DatagramSocket()
            datagramSocket.broadcast = true
            val sendData = arguments[0]?.toByteArray() ?: return FetchDataErrorStatus.INVALID_SEND_DATA
            val port = arguments[1]?.toInt() ?: return FetchDataErrorStatus.INVALID_PORT

            try {
                val sendPacket = DatagramPacket(sendData, sendData.size, broadcastAddress, port)
                datagramSocket.send(sendPacket)
                Log.d(BROADCAST_TAG, "Request packet sent to: ${broadcastAddress.toString()}")
            } catch (e: Exception) {
                Log.d(BROADCAST_TAG, e.toString())
            } finally {
                datagramSocket.close()
            }

            val serverSocket = ServerSocket(19103)
            serverSocket.soTimeout = 5000
            var clientSocket: Socket? = null
            try{
                clientSocket = serverSocket.accept()
            } catch (e: SocketTimeoutException){
                Log.d(BROADCAST_TAG, "Timeout reached")
            } finally {
                serverSocket.close()
            }

            Log.d(BROADCAST_TAG, "Received from: ${clientSocket?.inetAddress.toString()}")

            val br = BufferedReader(InputStreamReader(clientSocket?.getInputStream() ?: return FetchDataErrorStatus.CLIENT_SOCKET_ERROR))
            val message = br.readLine()
            clientSocket.close()
            publishProgress(JSONObject(message))

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
            when (result){
                FetchDataErrorStatus.NO_ERROR -> Log.d(BROADCAST_TAG, "FetchData finished correctly")
                FetchDataErrorStatus.INVALID_SEND_DATA -> TODO("Manage error")
                FetchDataErrorStatus.INVALID_PORT -> TODO("Manage error")
                FetchDataErrorStatus.CLIENT_SOCKET_ERROR -> TODO("Manage error")
                FetchDataErrorStatus.INVALID_ACTIVITY -> TODO("Manage error")
            }
        }

        /*private fun wifiIpAddress(context: Context): String {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            var ipAddress = wifiManager.dhcpInfo.ipAddress

            // Convert little-endian to big-endian if needed
            if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
                ipAddress = Integer.reverseBytes(ipAddress)
            }

            val ipByteArray = BigInteger.valueOf(ipAddress.toLong()).toByteArray()

            val ipAddressString = try {
                InetAddress.getByAddress(ipByteArray).hostAddress
            } catch (ex: UnknownHostException) {
                Log.e(BroadcastDiscoveryActivity.BROADCAST_TAG, "Unable to get host address.")
                null
            }

            return ipAddressString?: ""
        }

        private fun wifiIpNetMask(context: Context): String {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            var netmask = wifiManager.dhcpInfo.netmask

            // Convert little-endian to big-endian if needed
            if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
                netmask = Integer.reverseBytes(netmask)
            }
            val longMask = netmask + 4294967296

            val byteA = longMask / 256 / 256 / 256
            val byteB = (longMask - byteA * 256 * 256 * 256) / 256 / 256
            val byteC = (longMask - byteA * 256 * 256 * 256 - byteB * 256 * 256) / 256
            val byteD = longMask - byteA * 256 * 256 * 256 - byteB * 256 * 256 - byteC * 256

            return byteA.toString() + "." +
                    byteB.toString() + "." +
                    byteC.toString() + "." +
                    byteD.toString()
        }

        private fun wifiIpBroadcast(ipAddress: String, netMask: String): String {
            val intIp = ipAddress.split(".").map { it.toInt() }
            val intNetMask = netMask.split(".").map { it.toInt() }

            val intBroadcast = (0 until intIp.size).map {(intIp[it] or intNetMask[it].inv()) + 256}
            return intBroadcast.joinToString(".")
        }*/
    }
}
