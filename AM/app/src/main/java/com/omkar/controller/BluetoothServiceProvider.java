package com.omkar.controller;

import com.omkar.controller.ui.bluetooth.services.BluetoothService;

public class BluetoothServiceProvider {

    private static BluetoothService bluetoothService;

    public static BluetoothService getBluetoothService() {
        return bluetoothService;
    }

    public static void setBluetoothService(BluetoothService bluetoothService) {
        BluetoothServiceProvider.bluetoothService = bluetoothService;
    }

}
