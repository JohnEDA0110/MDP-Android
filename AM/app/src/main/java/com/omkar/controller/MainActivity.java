package com.omkar.controller;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.omkar.controller.databinding.ActivityMainBinding;
import com.omkar.controller.ui.bluetooth.services.BluetoothService;
import com.omkar.controller.ui.dashboard.DashboardViewModel;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private final String TAG= "MainActivity";

    private MutableLiveData<MessageQueue> mutableLiveMessageQueue = new MutableLiveData<>();
    private MessageQueue messageQueue = new MessageQueue();
    private MutableLiveData<TextToSpeech> textToSpeech = new MutableLiveData<>();
    private TextToSpeech speechAssistant;

    private final Handler handler = new Handler(msg -> {
        switch (msg.what) {
            case BluetoothService.MessageConstants.MESSAGE_READ:
                String read = (String) msg.obj;
                Log.d(TAG, "MESSAGE_READ");
                Log.d(TAG, read);

                messageQueue.add(new Message("RPI > Android", read, Calendar.getInstance().getTime().toString(), true));
                mutableLiveMessageQueue.setValue(messageQueue);
                mutableLiveMessageQueue.postValue(messageQueue);

                String messageContent = read;
                try {
                    JSONObject obj = new JSONObject(messageContent);
                    String cat = obj.getString("cat");
                    if(cat.equals("image-rec")){
                        JSONObject value = obj.getJSONObject("value");
                        String imageId = value.getString("image_id");
                        String obstacleId = value.getString("obstacle_id");
                        Toast.makeText(this, "Obstacle " + obstacleId + " identified as image id " + imageId, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    //throw new RuntimeException(e);
                }
                break;
            case BluetoothService.MessageConstants.MESSAGE_WRITE:
                byte[] buffer = (byte[]) msg.obj;
                String writeMsg = new String(buffer);
                Log.d(TAG, "MESSAGE_WRITE");
                Log.d(TAG, writeMsg);
                break;
            case BluetoothService.MessageConstants.MESSAGE_CONNECTED:
                //AppTransitions.fade(availableDevicesList, 200);
                //availableDevicesList.setVisibility(View.GONE);
                //game.setBluetoothConnected(true);
                //mutableLiveGame.setValue(game);
                if (this == null) {
                    break;
                }
                Toast.makeText(this, "Bluetooth device connection is successful", Toast.LENGTH_SHORT).show();
                speechAssistant.speak("Bluetooth device connection is successful", TextToSpeech.QUEUE_FLUSH, null);
                break;
            case BluetoothService.MessageConstants.MESSAGE_DISCONNECT:
                break;
            case BluetoothService.MessageConstants.MESSAGE_TOAST:
                String message = msg.getData().getString(Integer.toString(BluetoothService.MessageConstants.MESSAGE_TOAST));
                if (this == null) {
                    break;
                }
                Log.e(TAG, message);
                break;
            case BluetoothService.MessageConstants.UNEXPECTED_DISCONNECTION:
                BluetoothServiceProvider.getBluetoothService().reconnectToDevice();
                break;
        }
        return false;
    });

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        askRelevantPermissions();

        BluetoothService bluetoothService = new BluetoothService(this, handler);
        BluetoothServiceProvider.setBluetoothService(bluetoothService);
        bluetoothService.endConnection();
        bluetoothService.startAcceptThread();

        mutableLiveMessageQueue.setValue(messageQueue);
        MessageQueueProvider.setMutableLiveMessageQueue(mutableLiveMessageQueue);

        speechAssistant = new TextToSpeech(this, status -> {
            if(status != TextToSpeech.ERROR) {
                speechAssistant.setLanguage(Locale.UK);
                speechAssistant.speak("App is ready!", TextToSpeech.QUEUE_FLUSH, null);
                textToSpeech.setValue(speechAssistant);
                SpeechProvider.setSpeechAssistant(textToSpeech);
            }
        });

        int requestCode = 1;
        Intent discoverableIntent =
                new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivityForResult(discoverableIntent, requestCode);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_bluetooth, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        MaterialSwitch toggleButton = findViewById(R.id.toggleButton);
        if(BluetoothService.isBluetoothEnabled()) toggleButton.setChecked(true);
        else toggleButton.setChecked(false);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // Show visibility of toggle button whenever user clicks on the bluetooth tab
        navView.setOnItemSelectedListener(item -> {
            if(item.getItemId() == R.id.navigation_bluetooth) {
                getSupportActionBar().setTitle("Bluetooth");
                navController.navigate(R.id.navigation_bluetooth);
                toggleButton.setVisibility(ToggleButton.VISIBLE);
            } else {
                if(item.getItemId() == R.id.navigation_dashboard){
                    getSupportActionBar().setTitle("Dashboard");
                    navController.navigate(R.id.navigation_dashboard);
                } else {
                    getSupportActionBar().setTitle("Notifications");
                    navController.navigate(R.id.navigation_notifications);
                }
                toggleButton.setVisibility(ToggleButton.INVISIBLE);
            }
            return true;
        });

    }

    private void askRelevantPermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            System.out.println("Requesting ANDROID_11_BLE_PERMISSIONS permissions");
            requestPermissions(ANDROID_11_BLE_PERMISSIONS, 1);
        } else {
            System.out.println("Requesting BLE_PERMISSIONS permissions");
            requestPermissions(BLE_PERMISSIONS, 1);
        }
    }

    private final String[] BLE_PERMISSIONS = new String[]{
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_ADMIN,
    };

    private final String[] ANDROID_11_BLE_PERMISSIONS = new String[]{
            android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_ADMIN,
            android.Manifest.permission.BLUETOOTH_SCAN,
            android.Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION,
    };

}