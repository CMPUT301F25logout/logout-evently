package com.example.evently.ui.common;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.evently.R;
import com.google.android.material.textfield.TextInputEditText;

public class ConfirmFragmentTextInput extends DialogFragment {
    static final String headerKey = "header";
    static final String messageKey = "message";
    static final String cancelKey = "cancel";
    static final String confirmKey = "confirm";
    static final String hintKey = "hint";
    public static final String requestKey = "confirmResult";
    public static final String inputKey = "input";

    public ConfirmFragmentTextInput() {}

    public static ConfirmFragmentTextInput newInstance(String headerText, String messageText, String hintText, String cancelText, String confirmText) {
        ConfirmFragmentTextInput confirmFragment = new ConfirmFragmentTextInput();
        Bundle args = new Bundle();
        args.putString(headerKey, headerText);
        args.putString(messageKey, messageText);
        args.putString(cancelKey, cancelText);
        args.putString(confirmKey, confirmText);
        args.putString(hintKey, hintText);
        confirmFragment.setArguments(args);
        return confirmFragment;
    }

    public static ConfirmFragmentTextInput newInstance(String headerText, String messageText, String hintText) {
        ConfirmFragmentTextInput confirmFragment = new ConfirmFragmentTextInput();
        Bundle args = new Bundle();
        args.putString(headerKey, headerText);
        args.putString(messageKey, messageText);
        args.putString(hintKey, hintText);
        confirmFragment.setArguments(args);
        return confirmFragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_confirmation_text_input, container, false);
        TextView header = view.findViewById(R.id.confirmation_text_input_header);
        TextView message = view.findViewById(R.id.confirmation_text_input_message);
        TextInputEditText textEdit = view.findViewById(R.id.confirmation_text_input_text_field);
        Button cancel = view.findViewById(R.id.confirmation_text_input_cancel_button);
        Button confirm = view.findViewById(R.id.confirmation_text_input_confirm_button);

        Bundle args = getArguments();

        if (args == null) throw new RuntimeException("Illegal createView Call made - ConfirmFragmentNoInput class");

        header.setText(args.getString(headerKey));
        message.setText(args.getString(messageKey));
        textEdit.setHint(args.getString(hintKey));
        cancel.setText(args.getString(cancelKey, "Cancel"));
        confirm.setText(args.getString(confirmKey, "Confirm"));

        cancel.setOnClickListener(v -> dismiss());
        confirm.setOnClickListener(v -> {
            Bundle input = new Bundle();
            if (textEdit.getText() == null) dismiss();
            input.putString(inputKey, textEdit.getText().toString());
            getParentFragmentManager().setFragmentResult(requestKey, input);
            dismiss();
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() == null || getDialog().getWindow() == null) return;
        int displayWidth = (int) (getResources().getDisplayMetrics().widthPixels * 0.9f);
        int displayHeight = (int)(getResources().getDisplayMetrics().heightPixels * 0.35f);

        getDialog().getWindow().setLayout(displayWidth, displayHeight);
    }
}
