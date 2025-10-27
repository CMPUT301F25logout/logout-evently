package com.example.evently.ui.login;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.evently.databinding.FragmentRegisterBinding;
import com.example.evently.utils.validation.EmailValidator;
import com.google.android.gms.common.SignInButton;

public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    private FirebaseLogin firebaseLogin;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupListeners();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseLogin = new FirebaseLogin(requireActivity());
    }

    private void setupListeners() {
        final EditText nameEditText = binding.name;
        final EditText emailEditText = binding.email;
        final EditText phoneEditText = binding.phone;
        final SignInButton loginButton = binding.register;
        final ProgressBar loadingProgressBar = binding.loading;

        loginButton.setEnabled(false);

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                var emailInp = emailEditText.getText().toString();
                if (!EmailValidator.validate(emailInp)) {
                    emailEditText.setError("Please enter a valid email");
                }
                var nameInp = nameEditText.getText().toString();
                if (!nameInp.strip().isBlank()) {
                    loginButton.setEnabled(true);
                } else {
                    nameEditText.setError("Please enter your name");
                }
            }
        };
        emailEditText.addTextChangedListener(afterTextChangedListener);
        nameEditText.addTextChangedListener(afterTextChangedListener);
        phoneEditText.addTextChangedListener(afterTextChangedListener);
        phoneEditText.setOnEditorActionListener((TextView v, int actionId, KeyEvent event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // TODO: Register.
                }
                return false;
        });

        loginButton.setOnClickListener(v -> {
            if (!loginButton.isEnabled()) {
                return;
            }
            loadingProgressBar.setVisibility(View.VISIBLE);
            // TODO: Register.
        });
    }
}