package com.crest.androidble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crest.androidble.adapter.BleDeviceAdapter
import com.crest.androidble.databinding.ActivityConnectBinding
import com.crest.androidble.model.BleDevice
import com.crest.androidble.model.BleScanCallback
import com.lorenzofelletti.permissions.PermissionManager
import com.lorenzofelletti.permissions.dispatcher.dsl.*


class ConnectActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConnectBinding
    private lateinit var item: BleDevice;

    @SuppressLint("MissingPermission", "SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConnectBinding.inflate(layoutInflater)
        setContentView(binding.root)
        item = MainActivity.Companion.bleDevice!!
        if (item.device?.alias.isNullOrBlank()) {
            binding.deviceName.text = item.name
        } else {
            binding.deviceName.text = item.device?.alias
        }
        binding.lblRssi.text = "RSSI: " + item.rssi
        binding.btnDisconnect.setOnClickListener {
            MainActivity.Companion.bluetoothGatt!!.disconnect()
        }
    }

    companion object {
        private val TAG = ConnectActivity::class.java.simpleName
        private const val BLE_PERMISSION_REQUEST_CODE = 1
    }
}