package com.example.evently.ui.event;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.evently.R;

/**
 * Container class that contains the 3 fragments for the different people tabs.
 * Not meant to be called itself directly.
 */
public class AllEntrantFragment {
    // TODO: Add EntrantListAdapter functionality for all the fragments.
    
    /**
     * Fragment to display list of all enrolled people in an event
     */
    public static class EnrolledPeopleFragment extends Fragment {
        // TODO: Enable switching from all people who enrolled to selected once selection is finished.
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater,
                                 @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_event_people_all_list, container, false);
        }
    }

    /**
     * Fragment to display list of all people who cancelled in an event
     */
    public static class CancelledPeopleFragment extends Fragment {
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater,
                                 @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_event_people_all_list, container, false);
        }
    }

    /**
     * Fragment to display list of all selected people in an event
     */
    public static class SelectedPeopleFragment extends Fragment {
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater,
                                 @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_event_people_all_list, container, false);
        }
    }
}
