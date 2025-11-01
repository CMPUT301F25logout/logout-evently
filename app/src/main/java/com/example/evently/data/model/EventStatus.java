package com.example.evently.data.model;

/**
 * Status of an Event shown on the UI.
 * Computed based on the time.
 */
public enum EventStatus {
    OPEN, // Waitlist still open: now < selectionTime
    CLOSED, // Waitlist closed:   now >= selectionTime
}
