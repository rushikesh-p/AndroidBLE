package com.crest.androidble.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.crest.androidble.ConnectActivity
import com.crest.androidble.R
import com.crest.androidble.model.AdapterOnClick
import com.crest.androidble.model.BleDevice

class BleDeviceAdapter(
    private val context: Context,
    private val devices: List<BleDevice>,
    val adapterOnClick: AdapterOnClick
) :

    RecyclerView.Adapter<BleDeviceAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val deviceNameTextView: TextView = itemView.findViewById(R.id.device_name)
        val lblRssi: TextView = itemView.findViewById(R.id.lbl_rssi)
        val cv: CardView = itemView.findViewById(R.id.cv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val deviceView = inflater.inflate(R.layout.device_row_layout, parent, false)
        return ViewHolder(deviceView)
    }

    @SuppressLint("SetTextI18n", "NewApi", "MissingPermission")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val device = devices[position]
        val textView = holder.deviceNameTextView
        val lblRssi = holder.lblRssi
        val cv = holder.cv
        if (device.device?.alias.isNullOrBlank()) {
            textView.text = device.name
        } else {
            textView.text = device.device?.alias
        }
        lblRssi.text = "RSSI: " + device.rssi
        cv.setOnClickListener {
            adapterOnClick.onClick(device)
        }
    }

    override fun getItemCount(): Int {
        return devices.size
    }
}