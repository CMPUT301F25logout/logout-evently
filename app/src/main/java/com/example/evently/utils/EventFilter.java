package com.example.evently.utils;

import com.example.evently.data.model.Category;
import com.example.evently.data.model.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to help with filtering lists of events.
 */
public class EventFilter {

    /**
     * Filters a list of events and returns a new list containing only the events
     * that match the specified category.
     *
     * @param events   The original list of events to filter.
     * @param category The category to filter by.
     * @return A new ArrayList of events that match the category.
     *         Returns an empty list if no events match.
     */
    public static ArrayList<Event> filterEvents(List<Event> events, Category category) {
        ArrayList<Event> filteredList = new ArrayList<>();
        // go through each event in the original list
        for (Event event : events) {
            if (event.category() == category) {
                filteredList.add(event);
            }
        }
        // return the new list with only the matching events
        return filteredList;
    }
}