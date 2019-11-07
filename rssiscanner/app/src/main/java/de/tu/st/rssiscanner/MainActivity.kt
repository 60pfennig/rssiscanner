package de.tu.st.rssiscanner

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.widget.TextView
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.bluetooth.BluetoothAdapter
import androidx.core.app.ComponentActivity
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class MainActivity : AppCompatActivity() {
    private lateinit var mBluetoothManager: BluetoothManager
    private val mBluetoothDeviceList = mutableListOf<BluetoothDevice>()
    lateinit var mConnectButton: Button
    lateinit var mStartServerButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_main)
        Log.v(TAG, "moin ihr luschen!")


        mBluetoothManager = BluetoothManager(this.applicationContext)

        viewManager = LinearLayoutManager(this)
        viewAdapter = RecyclerAdapter(mBluetoothManager.mDiscoveredDevices)
        mBluetoothManager.mBluetoothDeviceAdapter = viewAdapter



        recyclerView = findViewById<RecyclerView>(R.id.deviceList).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        mConnectButton = findViewById<Button>(R.id.connectButton)
        mStartServerButton = findViewById(R.id.serverStart)
        mConnectButton.setOnClickListener{onConnectClick()}
        mStartServerButton.setOnClickListener {mBluetoothManager.startServer()}
        super.onCreate(savedInstanceState)
    }

    private fun onConnectClick(){
        mBluetoothManager.startDiscovery()
    }

    override fun onDestroy() {
        mBluetoothManager.cleanUp()
        super.onDestroy()
    }
}
