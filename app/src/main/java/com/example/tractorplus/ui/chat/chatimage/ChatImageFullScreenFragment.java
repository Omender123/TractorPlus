package com.example.tractorplus.ui.chat.chatimage;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.databinding.DataBindingUtil;

import com.example.tractorplus.R;
import com.example.tractorplus.binding.FragmentDataBindingComponent;
import com.example.tractorplus.databinding.FragmentChatImageFullScreenBinding;
import com.example.tractorplus.ui.common.PSFragment;
import com.example.tractorplus.utils.AutoClearedValue;
import com.example.tractorplus.utils.Constants;

public class ChatImageFullScreenFragment extends PSFragment {

    private final androidx.databinding.DataBindingComponent dataBindingComponent = new FragmentDataBindingComponent(this);

    @VisibleForTesting
    private
    AutoClearedValue<FragmentChatImageFullScreenBinding> binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentChatImageFullScreenBinding dataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat_image_full_screen, container, false, dataBindingComponent);

        binding = new AutoClearedValue<>(this, dataBinding);

        return binding.get().getRoot();
    }

    @Override
    protected void initUIAndActions() {

    }

    @Override
    protected void initViewModels() {

    }

    @Override
    protected void initAdapters() {

    }

    @Override
    protected void initData() {

        if (getActivity() != null) {
            binding.get().setImage(getActivity().getIntent().getStringExtra(Constants.IMAGE));
        }

    }

}