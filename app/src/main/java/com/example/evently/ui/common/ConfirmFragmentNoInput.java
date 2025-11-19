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

/**
 * Fragment to query user for confirmation
 * Connected to fragment_confirmation_no_user_input.xml
 */
public class ConfirmFragmentNoInput extends DialogFragment {

    static final String headerKey = "header";
    static final String messageKey = "message";
    static final String cancelKey = "cancel";
    static final String confirmKey = "confirm";
    public static final String requestKey = "confirmResult";
    public static final String inputKey = "input";

    /**
     * Empty constructor for ConfirmFragmentNoInput. Use newInstance when initializing
     */
    public ConfirmFragmentNoInput() {}

    /**
     * Initializes a new instance of a ConfirmFragmentNoInput
     * @param headerText Text for header of fragment
     * @param messageText Text for message of fragment
     * @param cancelText Text for cancel button
     * @param confirmText Text for confirm button
     * @return new ConfirmFragmentNoInput instance
     */
    public static ConfirmFragmentNoInput newInstance(
            String headerText, String messageText, String cancelText, String confirmText) {
        ConfirmFragmentNoInput confirmFragment = new ConfirmFragmentNoInput();
        Bundle args = new Bundle();
        args.putString(headerKey, headerText);
        args.putString(messageKey, messageText);
        args.putString(cancelKey, cancelText);
        args.putString(confirmKey, confirmText);
        confirmFragment.setArguments(args);
        return confirmFragment;
    }

    /**
     * Initializes a new instance of a ConfirmFragmentNoInput
     * @param headerText Text for header of fragment
     * @param messageText Text for message of fragment
     * @return new ConfirmFragmentNoInput instance
     */
    public static ConfirmFragmentNoInput newInstance(String headerText, String messageText) {
        ConfirmFragmentNoInput confirmFragment = new ConfirmFragmentNoInput();
        Bundle args = new Bundle();
        args.putString(headerKey, headerText);
        args.putString(messageKey, messageText);
        confirmFragment.setArguments(args);
        return confirmFragment;
    }

    /**
     * Initializes fragment
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return The view created
     */
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =
                inflater.inflate(R.layout.fragment_confirmation_no_user_input, container, false);
        TextView header = view.findViewById(R.id.header);
        TextView message = view.findViewById(R.id.message);
        Button cancel = view.findViewById(R.id.cancel_button);
        Button confirm = view.findViewById(R.id.confirm_button);
        Bundle args = getArguments();

        if (args == null)
            throw new RuntimeException(
                    "Illegal createView Call made - ConfirmFragmentNoInput class");

        header.setText(args.getString(headerKey));
        message.setText(args.getString(messageKey));
        cancel.setText(args.getString(cancelKey, "Cancel"));
        confirm.setText(args.getString(confirmKey, "Confirm"));

        cancel.setOnClickListener(v -> returnResult(false));
        confirm.setOnClickListener(v -> returnResult(true));

        return view;
    }

    /**
     * Sets the fragment result, allowing parent fragment to handle confirm\cancel logic
     * @param result true if confirmed, false if canceled
     */
    private void returnResult(boolean result) {
        Bundle input = new Bundle();
        input.putBoolean(inputKey, result);
        getParentFragmentManager().setFragmentResult(requestKey, input);
        dismiss();
    }
}
