package com.example.evently.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evently.R;
import com.example.evently.data.AccountDB;



public class ViewProfilesFragment extends Fragment
{
	private AccountDB accountDB;

	private ProfileRecyclerViewAdapter adapter;

	private RecyclerView recyclerView;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		recyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_admin_profiles, container, false);

		return recyclerView;
	}
}
