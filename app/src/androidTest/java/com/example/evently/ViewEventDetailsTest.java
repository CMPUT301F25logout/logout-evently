package com.example.evently;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static com.example.evently.MatcherUtils.assertRecyclerViewItem;
import static com.example.evently.MatcherUtils.p;
import static org.hamcrest.Matchers.allOf;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.rule.GrantPermissionRule;

import com.example.evently.data.AccountDB;
import com.example.evently.data.EventsDB;
import com.example.evently.data.generic.Promise;
import com.example.evently.data.model.Account;
import com.example.evently.data.model.Category;
import com.example.evently.data.model.Event;
import com.example.evently.ui.entrant.BrowseEventsFragment;
import com.example.evently.ui.entrant.EntrantActivity;
import com.google.firebase.Timestamp;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class ViewEventDetailsTest extends EmulatedFragmentTest<BrowseEventsFragment> {
	private static final EventsDB eventsDB = new EventsDB();
	private static final AccountDB accountsDB = new AccountDB();

	private static final Instant now = Instant.now();
	// We can use the same times for these tests.
	private static final Timestamp selectionTime = new Timestamp(now.plus(Duration.ofMillis(2)));
	private static final Timestamp eventTime = new Timestamp(now.plus(Duration.ofMinutes(10)));

	private static final Event mockEvent = new Event(
					"name",
					"description",
					Category.EDUCATIONAL,
					selectionTime,
					eventTime,
					"orgEmail",
					50);

	private static final Account[] mockAccounts = new Account[]{
			new Account(
				"email@gmail.com",
					"User",
					Optional.empty(),
					"email@gmail.com"
			),
			new Account(
					"email1@gmail.com",
					"User1",
					Optional.empty(),
					"email1@gmail.com"
			),
			new Account(
					"email2@gmail.com",
					"User2",
					Optional.empty(),
					"email2@gmail.com"
			),
			new Account(
					"email3@gmail.com",
					"User3",
					Optional.empty(),
					"email3@gmail.com"
			),
			new Account(
					"email5@gmail.com",
					"User5",
					Optional.empty(),
					"email4@gmail.com"
			),
			new Account(
					"email6@gmail.com",
					"User6",
					Optional.empty(),
					"email6@gmail.com"
			)
	};

	@BeforeClass
	public static void storeEventsAndAccounts() throws ExecutionException, InterruptedException {
		final var self = FirebaseEmulatorTest.mockAccount.email();

		eventsDB.storeEvent(mockEvent).await();

		// Store events into DB
		for (int i = 0; i < mockAccounts.length; i++) {
			accountsDB.storeAccount(mockAccounts[i]).await();

			if (i % 2 == 0) {
				eventsDB.enroll(mockEvent.eventID(), mockAccounts[i].email()).await();
			}
		}
	}

	@Rule
	public GrantPermissionRule grantPostNotif =
			GrantPermissionRule.grant(android.Manifest.permission.POST_NOTIFICATIONS);

	@AfterClass
	public static void tearDownEvents() throws ExecutionException, InterruptedException {
		Promise.all(eventsDB.nuke(), accountsDB.nuke()).await();
	}

	@Test
	public void testViewingEventDetails() throws ExecutionException, InterruptedException
	{
		Thread.sleep(2000);

		try (ActivityScenario<EntrantActivity> scenario = ActivityScenario.launch(EntrantActivity.class))
		{
			onView(ViewMatchers.withId(R.id.btnDetails)).perform(ViewActions.click());
			Thread.sleep(2000);
			onView(withText(mockEvent.description())).check(matches(isDisplayed()));

			Account[] expectedAccounts = new Account[] {
					mockAccounts[0],
					mockAccounts[2],
					mockAccounts[4]
			};

			// Test if the account's name show up on the recycler view
			for (int i = 0; i < expectedAccounts.length; i++)
			{
				var expectedAccount = mockAccounts[i*2];
				assertRecyclerViewItem(
						R.id.entrantListContainer,
						p(R.id.entrant_name, expectedAccount.name()));
			}
		};
	}


	@Override
	protected int getGraph() {
		return R.navigation.entrant_graph;
	}


	@Override
	protected Class<BrowseEventsFragment> getFragmentClass() {
		return BrowseEventsFragment.class;
	}
}
