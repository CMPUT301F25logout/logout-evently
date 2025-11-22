package com.example.evently.ui.admin;

import java.util.List;
import java.util.function.Consumer;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evently.R;
import com.example.evently.data.AccountDB;
import com.example.evently.data.model.Account;

/**
 * A fragment representing a list of accounts that the admin can browse and interact with.
 */
public class BrowseProfilesFragment extends Fragment {
    private ProfileRecyclerViewAdapter adapter;

    private RecyclerView recyclerView;

    /**
     * Handles clicks on a profile row in the Admin Browse list.
     * <p>
     * Uses the Navigation Component to navigate to the Admin view of the Profile screen,
     * passing the clicked account’s ID as a String argument.
     * @param account The structural representation of the Account view that was clicked.
     */
    protected void onProfileClick(Account account) {
        // The action for clicking on the account, pass the account ID to the next profile viewer
        // fragment
        var action = BrowseProfilesFragmentDirections.actionNavAccountsToProfileDetails(account.email());
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(action);
    }

    /**
     * Supplies the Browse list with all accounts opened.
     * @param callback Callback that will be passed the accounts into.
     */
    protected void initAccounts(Consumer<List<Account>> callback) {
        new AccountDB().fetchAllAccounts().thenRun(callback).catchE(e -> {
            Log.e("ViewProfiles", e.toString());
            Toast.makeText(requireContext(), "Something went wrong...", Toast.LENGTH_SHORT)
                    .show();
        });
    }

    @Nullable @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        recyclerView =
                (RecyclerView) inflater.inflate(R.layout.fragment_admin_profiles, container, false);

        if (recyclerView == null) {
            throw new AssertionError("ViewProfilesFragment called with non recyclerview.");
        }

        Context context = recyclerView.getContext();
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        // Set up the recycler view adapter with all accounts in the database
        initAccounts(accounts -> {
            adapter = new ProfileRecyclerViewAdapter(accounts, this::onProfileClick);
            recyclerView.setAdapter(adapter);
        });

        return recyclerView;
    }
}
