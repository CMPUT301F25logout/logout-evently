package com.example.evently.ui.organizer;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Optional;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.Timestamp;

import com.example.evently.R;
import com.example.evently.data.model.Category;
import com.example.evently.data.model.Event;
import com.example.evently.utils.FirebaseAuthUtils;

/**
 * Fragment that collects input to create a new organizer-owned {@link Event}
 * <p>
 * It inflates {@code R.layout.fragment_create_event}, performs simple client-side
 * validation, and on success constructs an {@link Event} object and returns it to
 * the previous fragment via the Navigation component's {@code SavedStateHandle}
 * under the key {@code "new_event"}, then calls {@code navigateUp()} to return.
 * Persistence is handled by the receiving screen.
 */
public class CreateEventFragment extends Fragment {

    /**
     * Inflates the "Create Event" form
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return the view of the inflated form
     */
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_event, container, false);
    }

    /**
     * Builds up a form, validates input, builds an {@link Event}, returns it, and navigates up
     *
     * @param v The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here
     */
    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        EditText etName = v.findViewById(R.id.etEventName);
        EditText etDesc = v.findViewById(R.id.etDescription);
        EditText etWaitLimit = v.findViewById(R.id.etWaitLimit);
        EditText etWinners = v.findViewById(R.id.etWinners);
        EditText etRegDate = v.findViewById(R.id.etRegDate);
        EditText etRegTime = v.findViewById(R.id.etRegTime);

        v.findViewById(R.id.btnCancel)
                .setOnClickListener(
                        _x -> NavHostFragment.findNavController(this).navigateUp());

        v.findViewById(R.id.btnCreate).setOnClickListener(_x -> {
            String name = etName.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();
            String winnersStr = etWinners.getText().toString().trim();
            String dateStr = etRegDate.getText().toString().trim();
            String timeStr = etRegTime.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                toast("Please enter an event name.");
                return;
            }
            if (TextUtils.isEmpty(winnersStr)) {
                toast("Please enter number of winners.");
                return;
            }
            if (TextUtils.isEmpty(dateStr) || TextUtils.isEmpty(timeStr)) {
                toast("Please enter deadline date and time.");
                return;
            }

            long winners;
            try {
                winners = Long.parseLong(winnersStr);
            } catch (NumberFormatException e) {
                toast("Winners must be an integer.");
                return;
            }

            Optional<Long> wait = Optional.empty();
            String w = etWaitLimit.getText().toString().trim();
            if (!TextUtils.isEmpty(w)) {
                try {
                    wait = Optional.of(Long.parseLong(w));
                } catch (NumberFormatException e) {
                    toast("Waitlist limit must be an integer.");
                    return;
                }
            }

            Instant selectionTime;
            try {
                LocalDate d = LocalDate.parse(dateStr); // YYYY-MM-DD
                LocalTime t = LocalTime.parse(timeStr); // HH:mm:ss
                selectionTime = LocalDateTime.of(d, t).toInstant(ZoneOffset.UTC);
            } catch (Exception ex) {
                toast("Invalid date/time. Use YYYY-MM-DD and HH:mm:ss");
                return;
            }

            // For now, eventTime == selectionTime + 2 days (until organizer add event date/time
            // fields)
            Event created = new Event(
                    name,
                    desc,
                    Category.SPORTS,
                    new Timestamp(selectionTime),
                    new Timestamp(selectionTime.plus(Duration.ofDays(2))),
                    FirebaseAuthUtils.getCurrentEmail(),
                    winners,
                    wait.orElse(null));

            var nav = NavHostFragment.findNavController(this);
            // Send result back to the previous fragment (OwnEventsFragment)
            nav.getPreviousBackStackEntry().getSavedStateHandle().set("new_event", created);

            nav.navigateUp();
        });
    }

    /**
     * Shows a short-length {@link Toast} with the given message in this Fragment's context
     * @param msg message to display to the user
     */
    private void toast(String msg) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
