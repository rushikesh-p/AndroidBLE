package com.crest.androidble.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.crest.androidble.R
import com.crest.androidble.model.BleDevice

class BleDeviceAdapter(private val devices: List<BleDevice>) :
    RecyclerView.Adapter<BleDeviceAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val deviceNameTextView: TextView = itemView.findViewById(R.id.device_name)
        val lblRssi: TextView = itemView.findViewById(R.id.lbl_rssi)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val deviceView = inflater.inflate(R.layout.device_row_layout, parent, false)
        return ViewHolder(deviceView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val device = devices[position]
        val textView = holder.deviceNameTextView
        val lblRssi = holder.lblRssi
        textView.text = device.name
        lblRssi.text = "RSSI: " + device.rssi
    }

    override fun getItemCount(): Int {
        return devices.size
    }
}