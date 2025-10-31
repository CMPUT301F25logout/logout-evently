package com.example.evently;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evently.data.model.Event;
import com.example.evently.data.model.MockUser;
import com.example.evently.databinding.FragmentEventDetailsBinding;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.Date;


public class EventDetailsFragment extends Fragment {
    private FragmentEventDetailsBinding binding;

    Event event;
    ArrayList<MockUser> entrants;


    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentEventDetailsBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        entrants = new ArrayList<MockUser>();

        addDummyData();

        loadEventInformation(event, entrants);
    }

    public void loadEventInformation(Event event, ArrayList<MockUser> entrants) {
        TextView eventName = binding.eventName;
        TextView image = binding.eventPicture;
        TextView desc = binding.eventDescription;
        TextView entrantCount = binding.entryCount;
        RecyclerView entrantList = binding.entrantList;

        eventName.setText(event.name());
        desc.setText(event.description());
        entrantCount.setText(String.valueOf(entrants.size()));

        entrantList.setLayoutManager(new LinearLayoutManager(this.getContext()));
        entrantList.setAdapter(new EntrantListAdapter(this.getContext(), entrants));
    }

    public void addDummyData() {
        event = new Event(
                "J",
                "Blah Blah Blah Description",
                new Date(),
                new Date(),
                UUID.randomUUID(),
                Optional.of((long) 100),
                10);

        entrants.add(new MockUser("U", "First Image"));
        entrants.add(new MockUser("L", "Second Image"));
    }
}
