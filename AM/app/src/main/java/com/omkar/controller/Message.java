package com.omkar.controller;

public class Message {

    public String deviceName;
    public String content;
    public String time;
    public boolean incoming;

    public Message(String deviceName, String content, String time, boolean incoming) {
        this.deviceName = deviceName;
        this.content = content;
        this.time = time;
        this.incoming = incoming;
    }

}
