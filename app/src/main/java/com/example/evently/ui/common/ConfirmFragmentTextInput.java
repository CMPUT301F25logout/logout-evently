package com.example.evently.ui.common;

import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;

import com.example.evently.R;
import com.example.evently.utils.TextInputValidator;

/**
 * Fragment to query user for confirmation with user text input
 * Connected to fragment_confirmation_text_input.xml
 */
public class ConfirmFragmentTextInput extends DialogFragment {
    static final String headerKey = "header";
    static final String messageKey = "message";
    static final String cancelKey = "cancel";
    static final String confirmKey = "confirm";
    static final String hintKey = "hint";
    static final String validateKey = "validate";
    public static final String requestKey = "confirmResult";
    public static final String inputKey = "input";

    /**
     * Initializes a ConfirmFragmentTextInput with bundled arguments
     * @param headerText Text for header of fragment
     * @param messageText Text for message of fragment
     * @param cancelText Text for cancel button
     * @param confirmText Text for confirm button
     * @param validateType Type of validation for input string
     *
     * @return new ConfirmFragmentTextInput instance
     */
    public static ConfirmFragmentTextInput newInstance(
            String headerText,
            String messageText,
            String hintText,
            String cancelText,
            String confirmText,
            TextInputValidator validateType) {
        var fragment = new ConfirmFragmentTextInput();
        Bundle args = new Bundle();
        args.putString(headerKey, headerText);
        args.putString(messageKey, messageText);
        args.putString(cancelKey, cancelText);
        args.putString(confirmKey, confirmText);
        args.putString(hintKey, hintText);
        args.putString(validateKey, validateType.name());
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Initializes a ConfirmFragmentTextInput with bundled arguments
     * @param headerText Text for header of fragment
     * @param messageText Text for message of fragment
     * @param validateType Type of validation for input string
     *
     * @return new ConfirmFragmentTextInput instance
     */
    public static ConfirmFragmentTextInput newInstance(
            String headerText,
            String messageText,
            String hintText,
            TextInputValidator validateType) {
        return newInstance(headerText, messageText, hintText, null, null, validateType);
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
     * @return the created fragment
     */
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_confirmation_text_input, container, false);
        Bundle args = getArguments();

        if (args == null)
            throw new RuntimeException(
                    "Illegal createView Call made - ConfirmFragmentNoInput class");

        connectUI(view, args);
        return view;
    }

    /**
     * Resize the display to fit elements on start
     */
    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() == null || getDialog().getWindow() == null) return;
        int displayWidth = (int) (getResources().getDisplayMetrics().widthPixels * 0.9f);
        int displayHeight = (int) (getResources().getDisplayMetrics().heightPixels * 0.35f);

        getDialog().getWindow().setLayout(displayWidth, displayHeight);
    }

    /**
     * Attach logic and text to ui elements
     * @param view View of the fragment
     * @param args Bundle arguments of the fragment
     */
    private void connectUI(View view, Bundle args) {
        TextView header = view.findViewById(R.id.header);
        TextView message = view.findViewById(R.id.message);
        TextInputEditText textEdit = view.findViewById(R.id.text_field);
        Button cancel = view.findViewById(R.id.cancel_button);
        Button confirm = view.findViewById(R.id.confirm_button);
        var validateType = TextInputValidator.valueOf(args.getString(validateKey));

        header.setText(args.getString(headerKey));
        message.setText(args.getString(messageKey));
        textEdit.setHint(args.getString(hintKey));
        cancel.setText(args.getString(cancelKey, "Cancel"));
        confirm.setText(args.getString(confirmKey, "Confirm"));

        cancel.setOnClickListener(v -> dismiss());
        confirm.setOnClickListener(v -> {
            Editable editText = textEdit.getText();
            if (editText == null) {
                dismiss();
                return;
            }
            Bundle input = new Bundle();
            String inputText = editText.toString();

            if (validateType.toPattern().matcher(inputText).matches()) {
                input.putString(inputKey, textEdit.getText().toString());
                getParentFragmentManager().setFragmentResult(requestKey, input);
                dismiss();
            }
            message.setText(args.getString(messageKey)); // Stops from repeat invalid appending
            message.append("\nINVALID INPUT");
        });
    }
}
