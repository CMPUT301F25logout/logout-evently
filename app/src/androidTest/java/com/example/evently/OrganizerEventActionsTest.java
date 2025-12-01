package com.example.evently;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasType;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import android.app.Activity;
import android.app.Instrumentation.ActivityResult;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.core.content.FileProvider;
import androidx.navigation.NavGraph;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.Timestamp;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.example.evently.data.EventsDB;
import com.example.evently.data.generic.Promise;
import com.example.evently.data.model.Account;
import com.example.evently.data.model.Category;
import com.example.evently.data.model.Event;
import com.example.evently.ui.organizer.EditEventDetailsFragment;

@RunWith(AndroidJUnit4.class)
/**
 * Tests for the organizer event actions fragment
 */
public class OrganizerEventActionsTest extends EmulatedFragmentTest<EditEventDetailsFragment> {
    private static final EventsDB eventsDB = new EventsDB();
    private static final Event mockEvent = new Event(
            "name",
            "description",
            Category.Educational,
            false,
            new Timestamp(Instant.now().plus(Duration.ofHours(1))),
            new Timestamp(Instant.now().plus(Duration.ofDays(2))),
            "orgEmail",
            50);

    private static final Account[] extraAccounts = new Account[] {
        new Account("email@gmail.com", "User", Optional.empty(), "email@gmail.com"),
        new Account("email1@gmail.com", "User1", Optional.empty(), "email1@gmail.com"),
        new Account("email2@gmail.com", "User2", Optional.empty(), "email2@gmail.com"),
    };

    private static final String expectedCSV = "email@gmail.com,email1@gmail.com,email2@gmail.com";

    @BeforeClass
    public static void setUpEventEnroll() throws ExecutionException, InterruptedException {
        eventsDB.storeEvent(mockEvent).await();

        for (Account account : extraAccounts) {
            eventsDB.addAccepted(mockEvent.eventID(), account.email());
        }
    }

    @AfterClass
    public static void tearDownEvent() throws ExecutionException, InterruptedException {
        Promise.all(eventsDB.nuke()).await();
    }

    @Override
    protected int getGraph() {
        return R.navigation.organizer_graph;
    }

    @Override
    protected Class<EditEventDetailsFragment> getFragmentClass() {
        return EditEventDetailsFragment.class;
    }

    @Override
    protected int getSelfDestination(NavGraph graph) {
        return R.id.event_details;
    }

    @Override
    protected Bundle getSelfDestinationArgs() {
        final var bundle = new Bundle();
        bundle.putSerializable("eventID", mockEvent.eventID());
        return bundle;
    }

    /**
     * Tests select channel selects
     */
    @Test
    public void selectChannelTest() {
        onView(withId(R.id.select_channel)).perform(click());
        onView(withText("Cancelled")).perform(click());
        onView(withText("Notify Cancelled")).check(matches(isDisplayed()));
    }

    /**
     * Tests CSV export makes correct file
     */
    @Test
    public void exportCSVTest() throws IOException {
        Intents.init();
        File qrCacheDir = new File(ApplicationProvider.getApplicationContext().getCacheDir(), "qr");
        if (!qrCacheDir.exists()) qrCacheDir.mkdirs();

        File tempFile = File.createTempFile("test_csv", ".csv", qrCacheDir);
        Uri fakeUri = FileProvider.getUriForFile(
                ApplicationProvider.getApplicationContext(),
                "com.example.evently.qrprovider",
                tempFile);

        Intent resultData = new Intent();

        resultData.setData(fakeUri);
        ActivityResult result = new ActivityResult(Activity.RESULT_OK, resultData);
        intending(hasAction(Intent.ACTION_CREATE_DOCUMENT)).respondWith(result);

        onView(withId(R.id.export_entrants)).perform(click());

        intended(hasAction(Intent.ACTION_CREATE_DOCUMENT));
        intended(hasType("text/csv"));
        intended(hasExtra(Intent.EXTRA_TITLE, "entrants.csv"));

        String writtenContent;
        try (FileInputStream input = new FileInputStream(tempFile);
                Scanner scanner = new Scanner(input, "UTF-8")) {
            writtenContent = scanner.useDelimiter("\\A").next();
        }

        assertEquals(expectedCSV, writtenContent);
        tempFile.delete();
        Intents.release();
    }
}
