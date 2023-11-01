package com.crest.androidble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crest.androidble.adapter.BleDeviceAdapter
import com.crest.androidble.databinding.ActivityConnectBinding
import com.crest.androidble.databinding.ActivityMainBinding
import com.crest.androidble.model.AdapterOnClick
import com.crest.androidble.model.BleDevice
import com.crest.androidble.model.BleScanCallback
import com.lorenzofelletti.permissions.PermissionManager
import com.lorenzofelletti.permissions.dispatcher.dsl.*


class MainActivity : AppCompatActivity(), AdapterOnClick {
    private lateinit var binding: ActivityMainBinding
    lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var permissionManager: PermissionManager
    private lateinit var btManager: BluetoothManager
    private lateinit var bleScanManager: BleScanManager
    private lateinit var foundDevices: MutableList<BleDevice>
    lateinit var mainHandler: Handler
    private val readDevicesList = object : Runnable {
        override fun run() {
            getBLEDevices()
            mainHandler.postDelayed(this, 15000)
        }
    }
    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private const val BLE_PERMISSION_REQUEST_CODE = 1
        var bleDevice: BleDevice? = null;
        var bluetoothGatt: BluetoothGatt? = null
        var flag: String? = "Not Connected"
    }

    private val mReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                //when discovery finds a device
                BluetoothDevice.ACTION_FOUND -> {
                    val device =
                        intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    Log.d("MainActivity", "device found")
                    if (device != null &&
                        device.name != null
                    ) {
//                        viewModel.addDiscoveredDevice(device)
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.d("MainActivity", "ACTION_DISCOVERY_FINISHED")
//                    viewModel.scanningFinished()
//                    //if there are no device show proper message
//                    if (viewModel.discoveredDevices.isEmpty()) {
//                        Toast.makeText(
//                            applicationContext,
//                            "Unfortunately no devices were found in your vicinity",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    } else {
//                        Toast.makeText(applicationContext, "Scan finished", Toast.LENGTH_SHORT)
//                            .show()
//                    }
                }
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
//                    //Since our app needs bluetooth to work correctly we don't let the user turn it off
//                    if (bluetoothAdapter.state == BluetoothAdapter.STATE_OFF
//                    ) {
//                        enableBluetooth()
//                    }
                }
            }
        }
    }


    var bluetoothGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(
            gatt: BluetoothGatt, status: Int,
            newState: Int
        ) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                runOnUiThread {
                    Log.i(TAG, "Connected to GATT server.")
                    binding.clLoader.visibility = View.GONE;
                    binding.rvFoundDevices.visibility = View.VISIBLE;
                    binding.lblTitle.visibility = View.VISIBLE;
                    binding.clConnect.visibility = View.VISIBLE
                    binding.clListing.visibility = View.GONE
                    bindDataAfterConnect()
                    Toast.makeText(this@MainActivity, "Connected", Toast.LENGTH_SHORT).show()
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                visibleListingView();
            }
        }
    }

    @SuppressLint("MissingInflatedId", "MissingPermission")
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        mainHandler = Handler(Looper.getMainLooper())
        setContentView(binding.root)
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        if (!bluetoothAdapter.isEnabled) {
            enableBluetooth()
            return;
        }
        initUI()
    }

    @SuppressLint("MissingPermission")
    private fun initUI() {
        pemissionInit()
        foundDevices = BleDevice.createBleDevicesList()
        val adapter = BleDeviceAdapter(this, foundDevices, this)
        binding.rvFoundDevices.adapter = adapter
        binding.rvFoundDevices.layoutManager = LinearLayoutManager(this)
        btManager = getSystemService(BluetoothManager::class.java)
        bleScanManager = BleScanManager(btManager, 5000, scanCallback = BleScanCallback({
            val rssi = it?.rssi
            Log.d(TAG, "initUI => " + it?.device?.address)
            val name = it?.device?.address
            if (name.isNullOrBlank()) return@BleScanCallback
            val device = BleDevice(name, rssi, it.device)
            val flag: Boolean = getDeviceExist(foundDevices, device)
            if (!flag) {
                foundDevices.add(device)
                adapter.notifyItemInserted(foundDevices.size - 1)
            }
        }))
        bleScanManager.beforeScanActions.add {
            foundDevices.size.let {
                foundDevices.clear()
                adapter.notifyItemRangeRemoved(0, it)
            }
        }
//        mainHandler.post(readDevicesList)
        getBLEDevices();
        binding.btnDisconnect.setOnClickListener {
            flag = "Disconnected";
            bluetoothGatt!!.disconnect()
        }
//        binding.lblCancel.setOnClickListener {
//            flag = "Cancelled";
//            bluetoothGatt!!.disconnect()
//            visibleListingView();
//        }
        bindDataAfterConnect()
//        initRegisterReceiver()
    }

    private fun getBLEDevices() {
        permissionManager checkRequestAndDispatch BLE_PERMISSION_REQUEST_CODE
    }

    private fun visibleListingView() {
        runOnUiThread {
            Log.i(TAG, "Disconnected from GATT server.")
            binding.clLoader.visibility = View.GONE;
            binding.rvFoundDevices.visibility = View.VISIBLE;
            binding.lblTitle.visibility = View.VISIBLE;
            binding.clConnect.visibility = View.GONE
            binding.clListing.visibility = View.VISIBLE
            permissionManager checkRequestAndDispatch BLE_PERMISSION_REQUEST_CODE
            Toast.makeText(
                this@MainActivity,
                flag, Toast.LENGTH_SHORT
            ).show()
        }
    }

    @SuppressLint("MissingPermission", "NewApi")
    private fun bindDataAfterConnect() {
        if (bleDevice == null) {
            return
        }
        if (bleDevice?.device?.alias.isNullOrBlank()) {
            binding.deviceName.text = bleDevice?.name
        } else {
            binding.deviceName.text = bleDevice?.device?.alias
        }
        binding.lblRssi.text = "RSSI: " + (bleDevice?.rssi)

    }

    private fun getDeviceExist(foundDevices: MutableList<BleDevice>, device: BleDevice): Boolean {
        var flag: Boolean = false;
        foundDevices.forEach { it ->
            if (it.name == device.name) {
                flag = true;
                return@forEach
            }
        }
        return flag;
    }

    private fun initRegisterReceiver() {
        // Register for broadcasts when a device is discovered
        val filter = IntentFilter()
        //register a broadcast receiver to check if the user disables his Bluetooth (or it has it already disabled)
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        //receivers for device discovering
        filter.addAction(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        registerReceiver(mReceiver, filter)
    }


    @RequiresApi(Build.VERSION_CODES.S)
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionManager.dispatchOnRequestPermissionsResult(requestCode, grantResults)
    }

    private fun pemissionInit() {
        val requiredPermissionsInitialClient =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                )
            } else {
                arrayOf(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            }
        permissionManager = PermissionManager(this)
        permissionManager buildRequestResultsDispatcher {
            withRequestCode(BLE_PERMISSION_REQUEST_CODE) {
                checkPermissions(requiredPermissionsInitialClient)
                showRationaleDialog(getString(R.string.ble_permission_rationale))
                doOnGranted { bleScanManager.scanBleDevices() }
                doOnDenied {
                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.ble_permissions_denied_message),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }


    @SuppressLint("MissingPermission", "NewApi", "SetTextI18n")
    override fun onClick(item: BleDevice) {
        bleDevice = item;
        flag = "Not Connected";
        binding.clLoader.visibility = View.VISIBLE;
        binding.rvFoundDevices.visibility = View.GONE;
        binding.lblTitle.visibility = View.GONE;
        var deviceName: String? = ""
        deviceName = if (bleDevice?.device?.alias.isNullOrBlank()) {
            bleDevice?.name
        } else {
            bleDevice?.device?.alias
        }
        binding.lblDevice.text = "Connecting to $deviceName.";
        bluetoothGatt = item.device?.connectGatt(this, false, bluetoothGattCallback)
    }

    private val enableBluetoothResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Toast.makeText(this, "Bluetooth Enabled!", Toast.LENGTH_SHORT).show()
            initUI()
        } else {
            Toast.makeText(this, "Bluetooth is required for this app to run", Toast.LENGTH_SHORT)
                .show()
            this.finish()
        }
    }

    private fun enableBluetooth() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        enableBluetoothResultLauncher.launch(enableBtIntent)
    }


    override fun onResume() {
        super.onResume()
//        if (mainHandler != null)
//            mainHandler.post(readDevicesList)
        if (permissionManager != null)
            permissionManager checkRequestAndDispatch BLE_PERMISSION_REQUEST_CODE
    }

    override fun onPause() {
        super.onPause()
//        if (mainHandler != null)
//            mainHandler.removeCallbacks(readDevicesList)
    }

    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        super.onDestroy()

        // Make sure we're not doing discovery anymore
        if (bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
        }

        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver)
        if (bluetoothGatt != null)
            bluetoothGatt?.disconnect();
    }

}