package com.example.evently.ui.common;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.evently.R;

public class ConfirmFragmentNoInput extends DialogFragment {

    static final String headerKey = "header";
    static final String messageKey = "message";
    static final String cancelKey = "cancel";
    static final String confirmKey = "confirm";
    public static final String requestKey = "confirmResult";
    public static final String inputKey = "input";

    public ConfirmFragmentNoInput() {}

    public static ConfirmFragmentNoInput newInstance(String headerText, String messageText, String cancelText, String confirmText) {
        ConfirmFragmentNoInput confirmFragment = new ConfirmFragmentNoInput();
        Bundle args = new Bundle();
        args.putString(headerKey, headerText);
        args.putString(messageKey, messageText);
        args.putString(cancelKey, cancelText);
        args.putString(confirmKey, confirmText);
        confirmFragment.setArguments(args);
        return confirmFragment;
    }
    public static ConfirmFragmentNoInput newInstance(String headerText, String messageText) {
        ConfirmFragmentNoInput confirmFragment = new ConfirmFragmentNoInput();
        Bundle args = new Bundle();
        args.putString(headerKey, headerText);
        args.putString(messageKey, messageText);
        confirmFragment.setArguments(args);
        return confirmFragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_confirmation_no_user_input, container, false);
        TextView header = view.findViewById(R.id.confirmation_no_user_input_header);
        TextView message = view.findViewById(R.id.confirmation_no_user_input_message);
        Button cancel = view.findViewById(R.id.confirmation_no_user_input_cancel_button);
        Button confirm = view.findViewById(R.id.confirmation_no_user_input_confirm_button);
        Bundle args = getArguments();

        if (args == null) throw new RuntimeException("Illegal createView Call made - ConfirmFragmentNoInput class");

        header.setText(args.getString(headerKey));
        message.setText(args.getString(messageKey));
        cancel.setText(args.getString(cancelKey, "Cancel"));
        confirm.setText(args.getString(confirmKey, "Confirm"));

        cancel.setOnClickListener(v -> returnResult(false));
        confirm.setOnClickListener(v -> returnResult(true));

        return view;
    }

    private void returnResult(boolean result) {
        Bundle input = new Bundle();
        input.putBoolean(inputKey, result);
        getParentFragmentManager().setFragmentResult(requestKey, input);
        dismiss();
    }

}
