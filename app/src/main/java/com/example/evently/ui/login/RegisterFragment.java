package com.example.evently.ui.login;

import java.util.Objects;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.credentials.exceptions.GetCredentialCancellationException;
import androidx.credentials.exceptions.GetCredentialInterruptedException;
import androidx.credentials.exceptions.GetCredentialUnsupportedException;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.SignInButton;
import com.google.firebase.auth.AuthResult;

import com.example.evently.MainActivity;
import com.example.evently.databinding.FragmentRegisterBinding;
import com.example.evently.utils.AuthConstants;
import com.example.evently.utils.validation.EmailValidator;

/**
 * This fragment manages the register form. It should solely be used in AuthActivity.
 * <p>
 * Upon successful registration, this fragment sets result under the key "RegisterFragment.resultKey".
 * The result bundle contains data from the register form and should be persisted by the parent activity
 * by listening on the result. Afterwards, the parent activity may transition into other activities.
 * <p>
 * Layout: fragment_register.xml
 * @see AuthActivity
 */
public class RegisterFragment extends Fragment {
    public static final String resultKey = "register";
    private FragmentRegisterBinding binding;
    private FirebaseLogin firebaseLogin;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
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
        final SignInButton registerBtn = binding.register;
        final ProgressBar loadingProgressBar = binding.loading;

        // Setting it in XML doesn't work for some reason. Must set it programmatically.
        registerBtn.setEnabled(false);

        // Responsive validation using text changed listeners.
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
                registerBtn.setEnabled(validateInputs());
            }
        };
        emailEditText.addTextChangedListener(afterTextChangedListener);
        nameEditText.addTextChangedListener(afterTextChangedListener);
        phoneEditText.addTextChangedListener(afterTextChangedListener);
        phoneEditText.setOnEditorActionListener((TextView v, int actionId, KeyEvent event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE && validateInputs()) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                tryRegistering(0);
            }
            return false;
        });

        registerBtn.setOnClickListener(v -> {
            loadingProgressBar.setVisibility(View.VISIBLE);
            tryRegistering(0);
        });
    }

    private boolean validateInputs() {
        // TODO (chase): Should add name and phone number validation too.
        var emailInp = binding.email.getText().toString();
        if (!EmailValidator.validate(emailInp)) {
            binding.email.setError("Please enter a valid email");
            return false;
        }
        var nameInp = binding.name.getText().toString();
        if (nameInp.strip().isBlank()) {
            binding.name.setError("Please enter your name");
            return false;
        }
        return true;
    }

    private void tryRegistering(int retryCount) {
        // Register flow activated, sign up the user with google.
        firebaseLogin.launchLogin(
                true,
                this::successfulLogin,
                e -> {
                    switch (e) {
                        case GetCredentialCancellationException ce -> {
                            // The user cancelled the sign in request...
                            // Let them try again (do nothing).
                        }
                        case GetCredentialInterruptedException ie -> {
                            // Retry (unless we retried too many times already).
                            if (retryCount > AuthConstants.MAX_RETRY) {
                                unrecoverableError(ie);
                                return;
                            }
                            tryRegistering(retryCount + 1);
                        }
                        case GetCredentialUnsupportedException ue ->
                            // This device does not support credential manager.
                            // Our app simply cannot work on this device.
                            // TODO (chase): Might be worth showing an alert dialog here.
                            Toast.makeText(
                                            requireActivity(),
                                            "Device unsupported; Sorry!",
                                            Toast.LENGTH_SHORT)
                                    .show();
                        default ->
                            Log.e(
                                    "RegisterFragment.GetCredentialCustomException",
                                    Objects.requireNonNullElse(
                                            e.getLocalizedMessage(), e.toString()));
                    }
                },
                this::unrecoverableError);
    }

    private void successfulLogin(AuthResult res) {
        // Login successful - let the parent activity know.
        Bundle dbData = new Bundle();
        // TODO (chase): Need to persist the name, email, phone
        //  into the DB linked with the firebase user ID!s
        getParentFragmentManager().setFragmentResult(resultKey, dbData);
    }

    private void unrecoverableError(Exception e) {
        Log.e(
                "LoginActivity.unrecoverableError",
                Objects.requireNonNullElse(e.getLocalizedMessage(), e.toString()));
        Toast.makeText(
                        requireContext(),
                        "Something went catastrophically wrong...",
                        Toast.LENGTH_SHORT)
                .show();
    }
}
