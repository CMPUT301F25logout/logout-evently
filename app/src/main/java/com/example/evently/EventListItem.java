package com.example.evently;

public class EventListItem {
    public final String title;
    public final String status;      // Confirmed / Open / Closed
    public final String selectionDate;   // e.g., "Selection on 2026-02-25"
    public final String eventDate;   // e.g., "2026-03-09"
    public final int posterResId;

    public EventListItem(String title, String status, String selectionDate, String eventDate, int posterResId) {
        this.title = title;
        this.status = status;
        this.selectionDate = selectionDate;
        this.eventDate = eventDate;
        this.posterResId = posterResId;
    }
}