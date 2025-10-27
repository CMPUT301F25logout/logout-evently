package com.example.evently.ui.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.evently.databinding.FragmentSignoutBinding;

public class SignOutFragment extends Fragment {
    private FragmentSignoutBinding binding;
    private FirebaseLogin firebaseLogin;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentSignoutBinding.inflate(getLayoutInflater(), container, false);
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
        final Button signOutBtn = binding.signOut;

        signOutBtn.setOnClickListener(v -> {
            firebaseLogin.signOut();

            // Signal the parent activity about the sign out being successful so they can handle the
            // rest.
            Bundle res = new Bundle();
            // The bundle doesn't actually need to contain anything. This request is more like a
            // signal than a result.
            getParentFragmentManager().setFragmentResult("signOut", res);
        });
    }
}
