package com.mooo.ewolvy.broadcastdiscoverytests

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.net.wifi.WifiManager
import android.support.design.widget.Snackbar
import android.util.Log
import com.mooo.ewolvy.broadcastdiscovery.BroadcastDiscoveryActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.math.BigInteger
import java.net.InetAddress
import java.net.UnknownHostException
import java.nio.ByteOrder


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
        /*
        val intent = Intent(this@MainActivity, BroadcastDiscoveryActivity::class.java)
        val extras = Bundle()
        extras.putString(BroadcastDiscoveryActivity.EXTRA_SERVICE, edit_service.text.toString())
        extras.putInt(BroadcastDiscoveryActivity.EXTRA_PORT, edit_port.text.toString().toInt())
        extras.putLong(BroadcastDiscoveryActivity.EXTRA_TIMEOUT, edit_timeout.text.toString().toLong())

        intent.putExtra(BROADCAST_EXTRAS, extras)

        startActivityForResult(intent, REQUEST_CODE_BCD)
        */
        Snackbar.make(root_layout,
            wifiIpAddress(this) + " " + wifiIpNetmask(this),
            Snackbar.LENGTH_LONG).show()
    }

    private fun wifiIpAddress(context: Context): String? {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        var ipAddress = wifiManager.dhcpInfo.ipAddress

        // Convert little-endian to big-endian if needed
        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            ipAddress = Integer.reverseBytes(ipAddress)
        }

        val ipByteArray = BigInteger.valueOf(ipAddress.toLong()).toByteArray()

        var ipAddressString: String?
        try {
            ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress()
        } catch (ex: UnknownHostException) {
            Log.e(BroadcastDiscoveryActivity.BROADCAST_TAG, "Unable to get host address.")
            ipAddressString = null
        }

        return ipAddressString
    }

    private fun wifiIpNetmask(context: Context): String? {
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
}
