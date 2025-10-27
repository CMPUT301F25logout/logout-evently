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

/**
 * Fragment for managing the sign out button.
 * May be attached to any activity that requires exposing a sign out flow.
 * <p>
 * Upon successful sign out, this fragment sets result under the key "SignOutFragment.resultKey".
 * The parent activity should listen for this result, and perform any action needed post sign out.
 * <p>
 * Layout file: fragment_signout.xml
 * <p>
 * The fragment should be included within a fragment container in the parent layout.
 * It is the parent layout's responsibility to set proper width and height to the container
 * for the button within, as the button adjusts to the whole space of the container
 */
public class SignOutFragment extends Fragment {
    public static final String resultKey = "signOut";
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
            getParentFragmentManager().setFragmentResult(resultKey, res);
        });
    }
}
