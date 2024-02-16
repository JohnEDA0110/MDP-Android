package com.omkar.controller;

import androidx.lifecycle.MutableLiveData;

public class MessageQueueProvider {

    private static MutableLiveData<MessageQueue> mutableLiveMessageQueue = new MutableLiveData<>();

    public static MutableLiveData<MessageQueue> getMutableLiveMessageQueue() {
        return mutableLiveMessageQueue;
    }

    public static void setMutableLiveMessageQueue(MutableLiveData<MessageQueue> mutableLiveMessageQueue) {
        MessageQueueProvider.mutableLiveMessageQueue = mutableLiveMessageQueue;
    }

}
