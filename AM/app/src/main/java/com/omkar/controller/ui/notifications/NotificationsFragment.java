package com.omkar.controller.ui.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.omkar.controller.MessageListViewAdapter;
import com.omkar.controller.databinding.FragmentNotificationsBinding;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        ListView messageList = binding.messageList;
        MessageListViewAdapter adapter = new MessageListViewAdapter(getContext());
        messageList.setAdapter(adapter);

        NotificationsViewModel notificationsViewModel = new NotificationsViewModel(adapter);
                //new ViewModelProvider(this).get(NotificationsViewModel.class);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}