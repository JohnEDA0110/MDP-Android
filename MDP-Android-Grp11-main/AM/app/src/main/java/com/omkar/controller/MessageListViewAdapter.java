package com.omkar.controller;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MessageListViewAdapter extends BaseAdapter {

    private ArrayList<Message> messages = new ArrayList<>();
    private Context context;
    private LayoutInflater inflater;

    public MessageListViewAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int position) {
        if (position < messages.size()) {
            return messages.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        if (position < messages.size()) {
            return position;
        }
        return 0;
    }

    public void add(Message message) {
        if(!messages.contains(message)) messages.add(message);
        notifyDataSetChanged();
    }

    // Clear the list and notify the adapter
    public void clear() {
        messages.clear();
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.message_list_view, null);
        TextView textView = convertView.findViewById(R.id.communicationDirection);
        textView.setText(messages.get(position).deviceName);
        TextView address = convertView.findViewById(R.id.messageContent);
        String messageContent = messages.get(position).content;
        try{
            JSONObject obj = new JSONObject(messageContent);
            String cat = obj.getString("cat");
            if(cat.equals("image-rec")){
                JSONObject value = obj.getJSONObject("value");
                String imageId = value.getString("image_id");
                String obstacleId = value.getString("obstacle_id");
                address.setText("OBSTACLE:" + obstacleId + " RECOGNIZED. IDENTIFIED IMAGE ID: " + imageId);
            } else if(cat.equals("location")){
                JSONObject value = obj.getJSONObject("value");
                String x = value.getString("x");
                String y = value.getString("y");
                String direction = value.getString("d");
                address.setText("ROBOT LOCATION UPDATED TO: (" + x + ", " + y + ") DIRECTION: " + direction);
            } else {
                String value = obj.getString("value");
                address.setText(value);
            }
        } catch (JSONException e){
            address.setText(messageContent);
        }
        //address.setText(messages.get(position).content);
        return convertView;
    }
}
