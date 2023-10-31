package com.crest.androidble.model

data class BleDevice(val name: String, val rssi: Int?) {
    companion object {
        fun createBleDevicesList(): MutableList<BleDevice> {
            return mutableListOf()
        }
    }
}
