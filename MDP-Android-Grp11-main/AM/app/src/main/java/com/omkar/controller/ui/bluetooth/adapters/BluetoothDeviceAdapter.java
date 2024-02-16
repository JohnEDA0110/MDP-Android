package com.omkar.controller.ui.bluetooth.adapters;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.omkar.controller.R;

import org.w3c.dom.Text;

import java.util.ArrayList;

@SuppressLint("MissingPermission")
public class BluetoothDeviceAdapter extends BaseAdapter {

    private ArrayList<BluetoothDevice> devices = new ArrayList<>();
    private Context context;
    private LayoutInflater inflater;

    public BluetoothDeviceAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return devices.size();
    }

    @Override
    public Object getItem(int position) {
        if (position < devices.size()) {
            return devices.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        if (position < devices.size()) {
            return position;
        }
        return 0;
    }

    // Add a new device to the list and notify the adapter
    public void add(BluetoothDevice device) {
        if(!devices.contains(device)) devices.add(device);
        notifyDataSetChanged();
    }

    // Clear the list and notify the adapter
    public void clear() {
        devices.clear();
        notifyDataSetChanged();
    }

    // Remove a device from the list and notify the adapter
    public void removeDevice(BluetoothDevice device) {
        devices.remove(device);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // return activity_connected_devices_list_view with name of bluetooth device in position
        convertView = inflater.inflate(R.layout.device_list_view, null);
        TextView textView = convertView.findViewById(R.id.deviceName);
        textView.setText(devices.get(position).getName());
        TextView address = convertView.findViewById(R.id.deviceAddress);
        address.setText(devices.get(position).getAddress());
        return convertView;
    }

}
