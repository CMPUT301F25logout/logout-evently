package com.example.evently.ui.common;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
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
    static final String fragmentKey = "fragmentClass";

    public ConfirmFragmentNoInput() {}

    public static ConfirmFragmentNoInput newInstance(String headerText, String messageText, String cancelText, Class<? extends Fragment> fragmentClass) {
        ConfirmFragmentNoInput confirmFragment = new ConfirmFragmentNoInput();
        Bundle args = new Bundle();
        args.putString(headerKey, headerText);
        args.putString(messageKey, messageText);
        args.putString(cancelKey, cancelText);
        args.putString(fragmentKey, fragmentClass.getCanonicalName());
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

        header.setText(args.getString(headerKey));
        message.setText(args.getString(messageKey));
        cancel.setText(args.getString(cancelKey, "Cancel"));

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
            actionFragment = (Fragment) Class.forName(args.getString(fragmentKey)).newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | java.lang.InstantiationException e) {
            throw new RuntimeException(e);
        }

        getChildFragmentManager().registerFragmentLifecycleCallbacks(
                new FragmentManager.FragmentLifecycleCallbacks() {
                    @Override
                    public void onFragmentViewCreated(@NonNull FragmentManager fm,
                                                      @NonNull Fragment f,
                                                      @NonNull View v,
                                                      Bundle savedInstanceState) {
                        if (f == actionFragment) {
                            View target = v.findViewWithTag("selectButton");

                            if (target instanceof Button) {
                                GradientDrawable buttonShape = new GradientDrawable();
                                buttonShape.setCornerRadius(12f);
                                target.setBackground(buttonShape);
                                target.setPadding(0, 0, 0 ,0);
                            } else {
                                Log.w("ConfirmFragmentNoInput", "No 'selectButton' found in fragment " + f.getClass().getSimpleName());
                            }

                            // Cleanup to prevent repeated callbacks
                            fm.unregisterFragmentLifecycleCallbacks(this);
                        }
                    }
                },
                false
        );

        getChildFragmentManager().beginTransaction().add(R.id.confirmation_no_user_input_action_frame, actionFragment).commit();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);

    }
}
