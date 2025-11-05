package com.example.evently.data.model;

import java.util.List;
import java.util.UUID;

/**
 * Lists of entrants associated with an event.
 * @param eventID The ID of the event in question.
 * @param all List of all the entrants enrolled in said event.
 * @param selected List of the entrants selected via lottery to participate in the event.
 * @param accepted List of selected entrants who have accepted the invitation to participate.
 * @param cancelled List of selected entrants who have declined the invitation to participate.
 */
public record EventEntrants(
        UUID eventID,
        List<String> all,
        List<String> selected,
        List<String> accepted,
        List<String> cancelled) {}
