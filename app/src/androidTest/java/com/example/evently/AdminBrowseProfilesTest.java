package com.example.evently;

import static com.example.evently.MatcherUtils.assertRecyclerViewItem;
import static com.example.evently.MatcherUtils.p;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import androidx.navigation.NavGraph;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.example.evently.data.AccountDB;
import com.example.evently.data.generic.Promise;
import com.example.evently.data.model.Account;
import com.example.evently.ui.admin.BrowseProfilesFragment;

@RunWith(AndroidJUnit4.class)
public class AdminBrowseProfilesTest extends EmulatedFragmentTest<BrowseProfilesFragment> {
    private static final AccountDB accountsDB = new AccountDB();

    private static final Account[] extraAccounts = new Account[] {
        new Account("email@gmail.com", "User", Optional.empty(), "email@gmail.com"),
        new Account("email1@gmail.com", "User1", Optional.empty(), "email1@gmail.com"),
        new Account("email2@gmail.com", "User2", Optional.empty(), "email2@gmail.com"),
        new Account("email3@gmail.com", "User3", Optional.empty(), "email3@gmail.com"),
        new Account("email5@gmail.com", "User5", Optional.empty(), "email4@gmail.com"),
        new Account("email6@gmail.com", "User6", Optional.empty(), "email6@gmail.com")
    };

    @BeforeClass
    public static void setUpAccounts() throws ExecutionException, InterruptedException {
        // Store the accounts into the database
        for (int i = 0; i < extraAccounts.length; i++) {
            accountsDB.storeAccount(extraAccounts[i]).await();
        }
    }

    @Test
    public void testViewingProfileList() throws InterruptedException {
        Thread.sleep(2000);

        for (final var account : extraAccounts) {
            assertRecyclerViewItem(R.id.profile_list, p(R.id.profile_name, account.name()));
        }
    }

    @AfterClass
    public static void tearDownAccounts() throws ExecutionException, InterruptedException {
        Promise.all(accountsDB.nuke()).await();
    }

    @Override
    protected int getGraph() {
        return R.navigation.admin_graph;
    }

    @Override
    protected Class<BrowseProfilesFragment> getFragmentClass() {
        return BrowseProfilesFragment.class;
    }

    @Override
    protected int getSelfDestination(NavGraph graph) {
        return R.id.nav_accounts;
    }
}
