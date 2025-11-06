package com.example.evently.ui.organizer;

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

import com.example.evently.R;
import com.example.evently.data.model.Category;
import com.example.evently.data.model.Event;

public class CreateEventFragment extends Fragment {

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_event, container, false);
    }

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
            Instant eventTime = selectionTime.plusSeconds(60);

            com.google.firebase.Timestamp selectionTs =
                    new com.google.firebase.Timestamp(java.util.Date.from(selectionTime));
            com.google.firebase.Timestamp eventTs =
                    new com.google.firebase.Timestamp(java.util.Date.from(eventTime));

            // For now, eventTime == selectionTime (until organizer add event date/time fields)
            Event created = new Event(
                    name,
                    desc,
                    Category.SPORTS,
                    selectionTs,
                    eventTs,
                    "orgEmail",
                    winners,
                    wait.orElse(null));

            var nav = NavHostFragment.findNavController(this);
            // Send result back to the previous fragment (OwnEventsFragment)
            nav.getPreviousBackStackEntry().getSavedStateHandle().set("new_event", created);

            nav.navigateUp();
        });
    }

    private void toast(String msg) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
