package de.tu.st.rssiscanner

import android.bluetooth.*
import android.bluetooth.BluetoothProfile.STATE_CONNECTED
import android.bluetooth.BluetoothProfile.STATE_DISCONNECTED
import android.content.Context
import android.content.Intent
import android.util.Log

const val GATT_CONNECTED = "gattconnected"
const val GATT_DISCONNECTED = "gattdisconnected"

class GattManager(private val ctx: Context) {
    private var connectionState = BluetoothProfile.STATE_DISCONNECTED
    private val mBluetoothGatts = mutableListOf<BluetoothGatt>()
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(
            gatt: BluetoothGatt,
            status: Int,
            newState: Int
        ) {
            val intentAction: String
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    intentAction = GATT_CONNECTED
                    connectionState = STATE_CONNECTED
                    broadcastUpdate(intentAction)
                    Log.i(TAG, "Connected to GATT server.")
                    /*Log.i(
                        TAG, "Attempting to start service discovery: " +
                                bluetoothGatt?.discoverServices()
                    ) */
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    intentAction = GATT_DISCONNECTED
                    connectionState = STATE_DISCONNECTED
                    Log.i(TAG, "Disconnected from GATT server.")
                    broadcastUpdate(intentAction)
                }
            }
        }

        // New services discovered
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            when (status) {
                //BluetoothGatt.GATT_SUCCESS -> broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED)
                else -> Log.w(TAG, "onServicesDiscovered received: $status")
            }
        }

        // Result of a characteristic read operation
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    //broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
                }
            }
        }
    }

    private fun broadcastUpdate(intentAction: String) {
        val intent = Intent(intentAction)
        //sendBroadcast(intent)
    }

    fun addDevice(device: BluetoothDevice){
        mBluetoothGatts.add(device.connectGatt(ctx, false, gattCallback))
    }
}