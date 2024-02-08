package com.omkar.controller.ui.bluetooth.services;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.omkar.controller.R;
import com.omkar.controller.ui.bluetooth.ConnectionException;
import com.omkar.controller.ui.bluetooth.adapters.BluetoothDeviceAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

@SuppressLint("MissingPermission")
public class BluetoothService {

    private final BluetoothAdapter bluetoothAdapter;
    private final Handler handler;
    private final Context context;
    private IntentFilter intentFilter;
    private BluetoothDeviceAdapter connectedListAdapter;
    private BluetoothDeviceAdapter scannedListAdapter;
    private static final String TAG = "BluetoothService";

    private ConnectThread connectThread;
    private ConnectedThread connectedThread;
    private AcceptThread acceptThread;
    private final UUID uuid;

    private BluetoothDevice connectedDevice;

    public interface MessageConstants {
        int MESSAGE_READ = 0;
        int MESSAGE_WRITE = 1;
        int MESSAGE_TOAST = 2;
        int MESSAGE_CONNECTED = 3;
        int MESSAGE_DISCONNECT = 4;
        int UNEXPECTED_DISCONNECTION = 5;
    }

    private Timer mHeartbeatTimer;
    private static final long HEARTBEAT_INTERVAL = 10000; // 10 seconds

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @SuppressLint({"NewApi", "MissingPermission"})
        @Override
        public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (device.getName() != null) {
                scannedListAdapter.add(device);
            }
        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            Toast.makeText(context, "Scan completed", Toast.LENGTH_SHORT).show();
            unregisterReceiver();
        } else if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)){
            Toast.makeText(context, "Connection lost", Toast.LENGTH_SHORT).show();
            endConnection();
            startAcceptThread();
        }
        }
    };

    public BluetoothService(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        uuid = UUID.fromString(("00001101-0000-1000-8000-00805f9b34fb"));
    }

    private void startHeartbeatTimer() {
        mHeartbeatTimer = new Timer();
        mHeartbeatTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (connectedDevice != null && connectedThread != null && connectedThread.isAlive()) {
                    // Send a heartbeat character to the connected device
                    try {
                        writeMessageToDevice(".".getBytes());
                    } catch (ConnectionException e) {
//                        new Thread(new Runnable() {
//                            @Override
//                            public void run() {
//                                reconnectToDevice();
//                                mHeartbeatTimer.cancel();
//                                mHeartbeatTimer.purge();
//                            }
//                        }).start();
                        Message disconnected = handler.obtainMessage(MessageConstants.UNEXPECTED_DISCONNECTION);
                        disconnected.sendToTarget();
                    }
                }
            }
        }, 0, HEARTBEAT_INTERVAL);
    }

    public void reconnectToDevice(){
        Toast.makeText(context, "Reconnecting to device", Toast.LENGTH_SHORT).show();
        connectToDevice(connectedDevice);
    }

    public void updateConnectedList(){
        connectedListAdapter.clear();
        for (BluetoothDevice device : bluetoothAdapter.getBondedDevices()) {
            connectedListAdapter.add(device);
        }
    }

    public void setConnectedListAdapter(BluetoothDeviceAdapter connectedListAdapter) {
        this.connectedListAdapter = connectedListAdapter;
    }

    public void setScannedListAdapter(BluetoothDeviceAdapter scannedListAdapter) {
        this.scannedListAdapter = scannedListAdapter;
    }

    public static boolean isBluetoothEnabled() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return bluetoothAdapter.isEnabled();
    }

    public void enableBluetooth(){
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ((Activity) context).startActivityForResult(enableBtIntent, 1);
            Toast.makeText(context, "Bluetooth turned on", Toast.LENGTH_SHORT).show();
        }
    }

    public void disableBluetooth(){
        if (bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.disable();
            Toast.makeText(context, "Bluetooth turned off", Toast.LENGTH_SHORT).show();
        }
    }

    public void scanBluetoothDevices() {
        Log.d(TAG, "SCAN");
        intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);

        registerReceiver();

        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }

        bluetoothAdapter.startDiscovery();
    }

    private void registerReceiver() {
        context.registerReceiver(broadcastReceiver, intentFilter);
    }

    public void unregisterReceiver() {
        context.unregisterReceiver(broadcastReceiver);
    }

    private void startConnectedThread(BluetoothSocket socket) {
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }
        connectedThread = new ConnectedThread(socket);
        connectedThread.start();
    }

    public void connectToDevice(BluetoothDevice device) {
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }
        connectThread = new ConnectThread(device);
        connectThread.start();
    }

    public BluetoothDevice getRemoteDevice(String address) {
        return bluetoothAdapter.getRemoteDevice(address);
    }

    public void endConnection() {
        System.out.println("endConnection" + ", " + connectThread + ", " + connectedThread + ", " + acceptThread);
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }
        if (connectedThread != null) {
            connectedThread.cancel();
            connectThread = null;
        }
        if (acceptThread != null) {
            acceptThread.cancel();
            acceptThread = null;
        }
    }

    public void startAcceptThread() {
        if (acceptThread != null) {
            acceptThread.cancel();
            acceptThread = null;
        }
        acceptThread = new AcceptThread();
        Log.d(TAG, "startAcceptThread: Starting AcceptThread");
        acceptThread.start();
        Log.d(TAG, "startAcceptThread: AcceptThread started");
    }

    public void writeMessageToDevice(byte[] bytes) throws ConnectionException {
        if (connectedThread == null) {
            throw new ConnectionException(ConnectionException.ErrorMessage.BLUETOOTH_DISCONNECTED.getErrorMessage());
        }
        synchronized (this) {
            connectedThread.write(bytes);
        }
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket
            // because mmServerSocket is final.
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code.
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord("Controller", uuid);
            } catch (IOException e) {
                Log.e(TAG, "Socket's listen() method failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned.
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket's accept() method failed", e);
                    break;
                }

                if (socket != null) {
                    // A connection was accepted. Perform work associated with
                    // the connection in a separate thread.
                    Message connected = handler.obtainMessage(MessageConstants.MESSAGE_CONNECTED);
                    connected.sendToTarget();
                    startConnectedThread(socket);
                    break;
                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = device.createRfcommSocketToServiceRecord(uuid);
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter.cancelDiscovery();

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                Message writeErrMsg = handler.obtainMessage(MessageConstants.MESSAGE_TOAST);
                Bundle bundle = new Bundle();
                bundle.putString(Integer.toString(MessageConstants.MESSAGE_TOAST), "Could not connect to the device");
                writeErrMsg.setData(bundle);
                handler.sendMessage(writeErrMsg);
            }

            if (mmSocket.isConnected()) {
                Message connected = handler.obtainMessage(MessageConstants.MESSAGE_CONNECTED);
                connected.sendToTarget();
                startConnectedThread(mmSocket);
            }

        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }

    /**
     * Uses the socket passed on by {@link #connectThread connectThread} to establish read and write communications with target device.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket socket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        /**
         * Attempt to get input and output streams.
         * @param socket <a href="https://developer.android.com/reference/android/bluetooth/BluetoothSocket">BluetoothSocket</a>
         */
        public ConnectedThread(BluetoothSocket socket) {
            this.socket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // get the bluetooth device from the socket
            connectedDevice = socket.getRemoteDevice();

            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
                Message message = handler.obtainMessage(MessageConstants.UNEXPECTED_DISCONNECTION);
                message.sendToTarget();
            }

            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);

                Message message = handler.obtainMessage(MessageConstants.UNEXPECTED_DISCONNECTION);
                message.sendToTarget();
            }

            inputStream = tmpIn;
            outputStream = tmpOut;
        }

        /**
         * Maintains the <i>inputStream</i> for reading inputs from target device using a while loop. <br>
         * Converts bitstream into java String format to be processed depending on the message. <br>
         * Send the message to {@link #handler Handler} to be processed elsewhere.
         */
        public void run() {
            // Buffer store for the stream
            byte[] buffer = new byte[1024];
            int numBytes;

            //startHeartbeatTimer();

            while (true) {
                try {
                    int availableBytes = inputStream.available();
                    if (availableBytes > 0) {
                        numBytes = inputStream.read(buffer);
                        String msg = new String(buffer, 0, numBytes, StandardCharsets.UTF_8);
                        Message read = handler.obtainMessage(MessageConstants.MESSAGE_READ, numBytes, -1, msg);
                        Log.d(TAG, msg);
                        read.sendToTarget();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Input stream was disconnected", e);
                    Message writeErrMsg = handler.obtainMessage(MessageConstants.MESSAGE_TOAST);
                    Bundle bundle = new Bundle();
                    bundle.putString(Integer.toString(MessageConstants.MESSAGE_TOAST), "Connection to device lost");
                    writeErrMsg.setData(bundle);
                    handler.sendMessage(writeErrMsg);
                    try {
                        socket.close();
                    } catch (IOException ex) {
                        Log.e(TAG, "Unable to close the socket");
                    }
                    break;
                }
            }
        }

        /**
         * Write bitstream to target device.
         * @param bytes intended output bitstream.
         */
        public synchronized void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
                Message msgWrite = handler.obtainMessage(MessageConstants.MESSAGE_WRITE, bytes.length, -1, bytes);
                msgWrite.sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data to the other device", e);

                Message writeErrMsg = handler.obtainMessage(MessageConstants.MESSAGE_TOAST);
                Bundle bundle = new Bundle();
                bundle.putString(Integer.toString(MessageConstants.MESSAGE_TOAST), "Couldn't send data to the other device");
                writeErrMsg.setData(bundle);
                handler.sendMessage(writeErrMsg);

                Message msg = handler.obtainMessage(MessageConstants.UNEXPECTED_DISCONNECTION);
                msg.sendToTarget();
            }
        }

        /**
         * Closes the socket.
         */
        public void cancel() {
            try {
                socket.close();
                Message msg = handler.obtainMessage(MessageConstants.MESSAGE_DISCONNECT);
                msg.sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connected socket", e);
            }
        }
    }
}
