package com.example.evently.ui.admin;

import java.util.List;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evently.data.model.Account;
import com.example.evently.databinding.FragmentProfileRowBinding;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Account}.
 * <p>
 */
public class ProfileRecyclerViewAdapter
        extends RecyclerView.Adapter<ProfileRecyclerViewAdapter.ProfileViewHolder> {

    /**
     * Interface for the callback of the details button click
     */
    public interface ProfileOnClickListener {
        void accept(Account a);
    }

    private final List<Account> profiles;
    private final ProfileOnClickListener onProfileClick;

    /**
     * Constructor for the profile recycler adapter
     * @param profiles An array of accounts representing the profiles to be displayed
     * @param onProfileClick Listener method for the delete account button
     */
    public ProfileRecyclerViewAdapter(
            List<Account> profiles, ProfileOnClickListener onProfileClick) {
        this.profiles = profiles;
        this.onProfileClick = onProfileClick;
    }

    @NonNull @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ProfileRecyclerViewAdapter.ProfileViewHolder(FragmentProfileRowBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
        // Only set the names if the profile list is not empty
        if (profiles.isEmpty()) {
            return;
        }

        holder.acc = profiles.get(position);
        var binding = holder.binding;

        // Set the name of each profile
        binding.profileName.setText(holder.acc.name());

        // Set the profile details button click listener
        binding.btnDetails.setOnClickListener(v -> onProfileClick.accept(holder.acc));
    }

    @Override
    public int getItemCount() {
        return profiles.size();
    }

    /**
     * ViewHolder that holds the layout binding and an Account
     */
    public static class ProfileViewHolder extends RecyclerView.ViewHolder {
        public final FragmentProfileRowBinding binding;
        public Account acc;

        public ProfileViewHolder(FragmentProfileRowBinding binding) {
            super(binding.getRoot());
            // Can define click listeners here
            this.binding = binding;
        }
    }
}
