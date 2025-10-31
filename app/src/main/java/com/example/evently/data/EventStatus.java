package com.example.evently.data;

public enum EventStatus {
    OPEN,       // Waitlist still open: now < selectionTime
    CLOSED,     // Waitlist closed:   now >= selectionTime
}
