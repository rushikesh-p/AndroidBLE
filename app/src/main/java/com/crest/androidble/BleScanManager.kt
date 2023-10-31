package com.crest.androidble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.crest.androidble.BuildConfig.DEBUG
import com.crest.androidble.model.BleScanCallback

class BleScanManager(
    btManager: BluetoothManager,
    private val scanPeriod: Long = DEFAULT_SCAN_PERIOD,
    private val scanCallback: BleScanCallback = BleScanCallback()
) {
    private val btAdapter = btManager.adapter
    private val bleScanner = btAdapter.bluetoothLeScanner

    var beforeScanActions: MutableList<() -> Unit> = mutableListOf()
    var afterScanActions: MutableList<() -> Unit> = mutableListOf()

    private var scanning = false

    private val handler = Handler(Looper.getMainLooper())

    @SuppressLint("MissingPermission")
    fun scanBleDevices() {
        fun stopScan() {
            if (DEBUG) Log.d(TAG, "${::scanBleDevices.name} - scan stop")
            scanning = false
            bleScanner.stopScan(scanCallback)
            executeAfterScanActions()
        }

        if (scanning) {
            stopScan()
        } else {
            handler.postDelayed({ stopScan() }, scanPeriod)
            executeBeforeScanActions()
            if (DEBUG) Log.d(TAG, "${::scanBleDevices.name} - scan start")
            scanning = true
            bleScanner.startScan(scanCallback)
        }
    }

    private fun executeBeforeScanActions() {
        executeListOfFunctions(beforeScanActions)
    }

    private fun executeAfterScanActions() {
        executeListOfFunctions(afterScanActions)
    }

    companion object {
        private val TAG = BleScanManager::class.java.simpleName
        const val DEFAULT_SCAN_PERIOD: Long = 10000
        private fun executeListOfFunctions(toExecute: List<() -> Unit>) {
            toExecute.forEach {
                it()
            }
        }
    }
}