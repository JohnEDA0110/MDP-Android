package com.omkar.controller.ui.bluetooth;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.omkar.controller.R;
import com.omkar.controller.databinding.FragmentBluetoothBinding;
import com.omkar.controller.ui.bluetooth.adapters.BluetoothDeviceAdapter;
import com.omkar.controller.ui.bluetooth.services.BluetoothService;

public class BluetoothFragment extends Fragment {

    private FragmentBluetoothBinding binding;
    private final String TAG= "BluetoothFragment";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentBluetoothBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        ListView connectedDevicesList = binding.connectedDevicesList;
        ListView scannedDevicesList = binding.scannedDevicesList;
        Button scanButton = binding.startScanButton;
        Button refreshButton = binding.refreshBondedDevicesButton;
        Button testButton = binding.testButton;
        MaterialSwitch toggleButton = getActivity().findViewById(R.id.toggleButton);

        BluetoothDeviceAdapter connectedListAdapter = new BluetoothDeviceAdapter(getContext());
        BluetoothDeviceAdapter scannedListAdapter = new BluetoothDeviceAdapter(getContext());

        connectedDevicesList.setAdapter(connectedListAdapter);
        scannedDevicesList.setAdapter(scannedListAdapter);

        BluetoothViewModel bluetoothViewModel = new BluetoothViewModel(getContext(), connectedListAdapter, scannedListAdapter);


        scanButton.setOnClickListener(v -> {
            bluetoothViewModel.scanForDevices();
        });

        refreshButton.setOnClickListener(v -> {
            bluetoothViewModel.updateConnectedList();
        });

        toggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked) {
                bluetoothViewModel.enableBluetooth();
            } else {
                bluetoothViewModel.disableBluetooth();
            }
        });

        scannedDevicesList.setOnItemClickListener((parent, view1, position, id) -> {
            if (bluetoothViewModel.isBluetoothAdapterEnabled()) {
                LinearLayout linearLayout = (LinearLayout) view1;
                TextView deviceNameTextView = (TextView) linearLayout.getChildAt(0);
                TextView deviceAddressTextView = (TextView) linearLayout.getChildAt(1);
                String deviceName = deviceNameTextView.getText().toString();
                String deviceAddress = deviceAddressTextView.getText().toString();
                bluetoothViewModel.connectToDevice(deviceAddress);
            }
        });

        connectedDevicesList.setOnItemClickListener((parent, view1, position, id) -> {
            if (bluetoothViewModel.isBluetoothAdapterEnabled()) {
                LinearLayout linearLayout = (LinearLayout) view1;
                TextView deviceNameTextView = (TextView) linearLayout.getChildAt(0);
                TextView deviceAddressTextView = (TextView) linearLayout.getChildAt(1);
                String deviceName = deviceNameTextView.getText().toString();
                String deviceAddress = deviceAddressTextView.getText().toString();
                bluetoothViewModel.connectToDevice(deviceAddress);
            }
        });

        testButton.setOnClickListener(v -> {
            openDialog();
        });

        bluetoothViewModel.updateConnectedList();
        bluetoothViewModel.scanForDevices();

        return root;
    }

    public void openDialog(){
        MessageDialog messageDialog = new MessageDialog();
        messageDialog.show(getFragmentManager(), "message dialog");

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}