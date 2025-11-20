package com.example.evently.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.evently.databinding.FragmentAdminHomeBinding;

/**
 * This is used as the home screen for the Admin activity.
 */
public class HomeFragment extends Fragment
{
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		final var binding =
				FragmentAdminHomeBinding.inflate(getLayoutInflater(), container, false);

		return binding.getRoot();
	}
}
