package com.example.evently.ui.admin;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evently.data.model.Account;
import com.example.evently.databinding.FragmentProfileBinding;

import java.util.ArrayList;
import java.util.List;

public class ProfileRecyclerViewAdapter extends RecyclerView.Adapter<ProfileRecyclerViewAdapter.ProfileViewHolder>
{
	private final List<Account> profiles;
	//private final ProfileOnClickListener onProfileClick;

	public interface ProfileOnClickListener {
		void accept(Account a);
	}

	public ProfileRecyclerViewAdapter()
	{
		this.profiles = new ArrayList<>();
	}

	public ProfileRecyclerViewAdapter(List<Account> profiles, ProfileOnClickListener onProfileClick)
	{
		this.profiles = profiles;
		//this.onProfileClick = onProfileClick;
	}
	@NonNull
	@Override
	public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new ProfileRecyclerViewAdapter.ProfileViewHolder(FragmentProfileBinding.inflate(
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
		// Set the picture of each profile
		// TODO binding.profilePicture

		// Set the profile click listener
		//binding.btnDetails.setOnClickListener(v -> onProfileClick.accept(holder.));
	}

	@Override
	public int getItemCount() {
		return profiles.size();
	}

	public static class ProfileViewHolder extends RecyclerView.ViewHolder {
		public final FragmentProfileBinding binding;
		public Account acc;

		public ProfileViewHolder(FragmentProfileBinding binding) {
			super(binding.getRoot());
			// Can define click listeners here
			this.binding = binding;
		}
	}
}
