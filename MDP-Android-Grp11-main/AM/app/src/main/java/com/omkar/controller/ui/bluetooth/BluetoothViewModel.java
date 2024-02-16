package com.omkar.controller.ui.bluetooth;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.omkar.controller.BluetoothServiceProvider;
import com.omkar.controller.ui.bluetooth.adapters.BluetoothDeviceAdapter;
import com.omkar.controller.ui.bluetooth.services.BluetoothService;

import java.nio.charset.StandardCharsets;

public class BluetoothViewModel extends ViewModel {

    private BluetoothService bluetoothService;
    private Context context;

    public BluetoothViewModel(Context context, BluetoothDeviceAdapter connectedListAdapter, BluetoothDeviceAdapter scannedListAdapter){
        this.bluetoothService = BluetoothServiceProvider.getBluetoothService(); //new BluetoothService(context, handler, connectedListAdapter, scannedListAdapter);
        this.bluetoothService.setConnectedListAdapter(connectedListAdapter);
        this.bluetoothService.setScannedListAdapter(scannedListAdapter);
        this.context = context;

//        this.bluetoothService.endConnection();
//        this.bluetoothService.startAcceptThread();
    }

    public void scanForDevices() {
        Toast.makeText(context, "Scanning for devices...", Toast.LENGTH_SHORT).show();
        bluetoothService.scanBluetoothDevices();
    }

    public void disableBluetooth() {
        bluetoothService.disableBluetooth();
    }

    public void enableBluetooth() {
        bluetoothService.enableBluetooth();
    }

    public boolean isBluetoothAdapterEnabled() {
        return BluetoothService.isBluetoothEnabled();
    }

    public void connectToDevice(String address) {
        bluetoothService.connectToDevice(bluetoothService.getRemoteDevice(address));
    }

    public void updateConnectedList(){
        bluetoothService.updateConnectedList();
    }

    public void sendMessage(String message) {
        try{
            bluetoothService.writeMessageToDevice(message.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            Toast.makeText(context, "Error sending message", Toast.LENGTH_SHORT).show();
        }
    }

}