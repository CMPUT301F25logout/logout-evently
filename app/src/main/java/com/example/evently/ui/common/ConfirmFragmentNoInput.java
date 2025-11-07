package com.example.evently.ui.common;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.evently.R;

public class ConfirmFragmentNoInput extends DialogFragment {

    public ConfirmFragmentNoInput() {

    }

    public static ConfirmFragmentNoInput newInstance(String headerText, String messageText, String cancelText, Class<? extends Fragment> fragmentClass) {
        ConfirmFragmentNoInput confirmFragment = new ConfirmFragmentNoInput();
        Bundle args = new Bundle();
        args.putString("header", headerText);
        args.putString("message", messageText);
        args.putString("cancel", cancelText);
        args.putString("fragmentClass", fragmentClass.getCanonicalName());
        confirmFragment.setArguments(args);
        return confirmFragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_confirmation_no_user_input, container, false);
        TextView header = view.findViewById(R.id.confirmation_no_user_input_header);
        TextView message = view.findViewById(R.id.confirmation_no_user_input_message);
        Button cancel = view.findViewById(R.id.confirmation_no_user_input_cancel_button);
        Bundle args = getArguments();

        if (args == null) throw new RuntimeException("Illegal createView Call made - ConfirmFragmentNoInput class");

        header.setText(args.getString("header"));
        message.setText(args.getString("message"));
        cancel.setText(args.getString("cancel", "Cancel"));

        cancel.setOnClickListener(v -> dismiss());

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Bundle args = getArguments();

        if (args == null) throw new RuntimeException("Illegal createView Call made - ConfirmFragmentNoInput class");
        Fragment actionFragment;
        try {
            actionFragment = (Fragment) Class.forName(args.getString("fragmentClass")).newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | java.lang.InstantiationException e) {
            throw new RuntimeException(e);
        }
        if (actionFragment == null) throw new RuntimeException("Illegal null fragment passed to confirm fragment - ConfirmationFragmentNoInput");
        getChildFragmentManager().beginTransaction().replace(R.id.confirmation_no_user_input_action_frame, actionFragment).commit();

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);

    }
}
