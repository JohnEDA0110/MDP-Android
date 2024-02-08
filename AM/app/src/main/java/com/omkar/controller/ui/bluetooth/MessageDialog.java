package com.omkar.controller.ui.bluetooth;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatDialogFragment;

import com.omkar.controller.BluetoothServiceProvider;
import com.omkar.controller.R;

public class MessageDialog extends AppCompatDialogFragment {

    private EditText message;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.test_meesage_dialog, null);
        builder.setView(view)
                .setTitle("Send Message")
                .setNegativeButton("Cancel", (dialog, which) -> {
                })
                .setPositiveButton("Send", (dialog, which) -> {
                    String messageText = message.getText().toString();
                    try{
                        BluetoothServiceProvider.getBluetoothService().writeMessageToDevice(messageText.getBytes());
                    } catch (ConnectionException e) {
                        Log.e("MessageDialog", "ConnectionException: " + e.getMessage());
                    }
                });

        message = view.findViewById(R.id.messageEditText);

        return builder.create();
    }
}
