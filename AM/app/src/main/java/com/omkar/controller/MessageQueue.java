package com.omkar.controller;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class MessageQueue {

    private ArrayList<Message> messages = new ArrayList<>();

    public MessageQueue() {}

    public void add(Message message) {
        messages.add(message);
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

}
