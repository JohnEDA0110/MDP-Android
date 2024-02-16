package com.omkar.controller.ui.notifications;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.omkar.controller.Message;
import com.omkar.controller.MessageListViewAdapter;
import com.omkar.controller.MessageQueue;
import com.omkar.controller.MessageQueueProvider;

public class NotificationsViewModel extends ViewModel {

    private MutableLiveData<MessageQueue> mutableMessageQueue = MessageQueueProvider.getMutableLiveMessageQueue();

    private MessageQueue messageQueue = new MessageQueue();

    public NotificationsViewModel(MessageListViewAdapter adapter) {
        // update the contents of the messageQueue whenever the mutableMessageQueue changes
        mutableMessageQueue.observeForever(messageQueue -> {
            this.messageQueue = messageQueue;
            for(int i = 0 ; i < messageQueue.getMessages().size() ; i++) {
                Message message = messageQueue.getMessages().get(i);
                // if(isAStatusMessage(message))
                adapter.add(message);
            }
        });
    }

    public boolean isAStatusMessage(Message message){
        if(message.content.startsWith("Status:"))
            return true;
        return false;
    }

}