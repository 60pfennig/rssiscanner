package de.tu.st.rssiscanner

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import java.io.IOException
import java.util.*

const val TAG = "BluetoothRSSI"

class BluetoothManager(private val mContext: Context){
    val mServerServiceName = "RSSIReader"
    val mBluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    val mUUID: UUID = UUID.randomUUID()

    val mGattManager = GattManager(mContext)

    var mBluetoothConnections = mutableListOf<BluetoothSocket>()
    val mDiscoveredDevices = mutableListOf<BluetoothDevice>()
    var mBluetoothDeviceAdapter: RecyclerView.Adapter<*>? = null
    private var mAcceptThread: AcceptThread? = null
    private var mConnectThread: ConnectThread? = null

    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            when(intent.action ?: "") {
                BluetoothDevice.ACTION_FOUND -> {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    val device: BluetoothDevice =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    val deviceName = device.name
                    val deviceHardwareAddress = device.address // MAC address
                    Log.v(TAG, "$deviceName found!")
                    if(!mDiscoveredDevices.contains(device)) {
                        mDiscoveredDevices.add(device)
                        mGattManager.addDevice(device)
                        mBluetoothDeviceAdapter?.notifyDataSetChanged()
                    }
                }
            }
        }
    }


    init {
        if (mBluetoothAdapter == null) {
            // Device doesn't support Bluetooth
        }
        if (mBluetoothAdapter?.isEnabled == false) {
            //:...
        }
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        mContext.registerReceiver(receiver, filter)
    }

    private fun makeDiscoverable(){
        val discoverableIntent: Intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
            setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
        }
        mContext.applicationContext.startActivity(discoverableIntent)
    }

    fun startServer(){
        makeDiscoverable()
        mAcceptThread = AcceptThread()
        mAcceptThread?.start()
    }

    fun stopServer(){
        mAcceptThread?.cancel()
    }

    fun startDiscovery(){
        Log.v(TAG, "starting discovery")
        mBluetoothAdapter?.startDiscovery()
    }


    private fun connectTo(device: BluetoothDevice){
        mConnectThread?.let { it.cancel() }
        mConnectThread = ConnectThread(device)
        mConnectThread?.start()
    }

    fun connectToPaired(){
        Log.v(TAG, "Trying to connect to paired device..")
        val pairedDevices: Set<BluetoothDevice>? = mBluetoothAdapter?.bondedDevices
        pairedDevices?.forEach { device ->
            val deviceName = device.name
            val deviceHardwareAddress = device.address // MAC address
            Log.v(TAG, "Paired devices: $deviceName")
        }
        if(!pairedDevices.isNullOrEmpty()){
            Log.v(TAG, "Connecting to paired device..")
            connectTo(pairedDevices.first())
        }
    }

    fun manageMyConnectedSocket(socket: BluetoothSocket){
        //if a socket to the same remote device dont exists, add the socket to the connections
        if (mBluetoothConnections.none { it.remoteDevice == socket.remoteDevice }) {
            Log.v(TAG, "Connection added to ${socket.remoteDevice}")
            mBluetoothConnections.add(socket)
        }
        else
            socket.close()
    }



    private inner class ConnectThread(device: BluetoothDevice) : Thread() {

        private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            device.createRfcommSocketToServiceRecord(mUUID)
        }

        override fun run() {
            // Cancel discovery because it otherwise slows down the connection.
            mBluetoothAdapter?.cancelDiscovery()

            mmSocket?.use { socket ->
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                Log.v(TAG, "Connecting Thread runnning")
                socket.connect()

                // The connection attempt succeeded. Perform work associated with
                // the connection in a separate thread.
                manageMyConnectedSocket(socket)
            }
        }

        // Closes the client socket and causes the thread to finish.
        fun cancel() {
            try {
                mmSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the client socket", e)
            }
        }
    }

    private inner class AcceptThread : Thread() {

        private val mmServerSocket: BluetoothServerSocket? by lazy(LazyThreadSafetyMode.NONE) {
            mBluetoothAdapter?.listenUsingInsecureRfcommWithServiceRecord(mServerServiceName, mUUID)
        }

        override fun run() {
            // Keep listening until exception occurs or a socket is returned.
            Log.v(TAG, "Server Thread running!")
            var shouldLoop = true
            while (shouldLoop) {
                val socket: BluetoothSocket? = try {
                    mmServerSocket?.accept()
                } catch (e: IOException) {
                    Log.e(TAG, "Socket's accept() method failed", e)
                    shouldLoop = false
                    null
                }
                socket?.also {
                    manageMyConnectedSocket(it)
                    mmServerSocket?.close()
                    Log.v(TAG, "Server socket accepted!")
                    shouldLoop = false
                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        fun cancel() {
            try {
                mmServerSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the connect socket", e)
            }
        }
    }

    fun cleanUp(){
        mBluetoothAdapter?.cancelDiscovery()
        mConnectThread?.cancel()
        mAcceptThread?.cancel()
        mBluetoothConnections.forEach{it.close()}
        mContext.unregisterReceiver(receiver)
    }
}