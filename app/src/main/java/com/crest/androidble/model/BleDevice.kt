package com.crest.androidble.model

import android.bluetooth.BluetoothDevice
import java.io.Serializable

data class BleDevice(val name: String, val rssi: Int?, val device: BluetoothDevice?) :
    Serializable {
    companion object {
        fun createBleDevicesList(): MutableList<BleDevice> {
            return mutableListOf()
        }
    }
}
