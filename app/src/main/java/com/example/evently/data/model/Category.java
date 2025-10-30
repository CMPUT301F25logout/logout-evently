package com.example.evently.data.model;

/**
 * Represents the category of an event.
 */
public enum Category {
    /** For sporting events */
    SPORTS("Sports"),
    /** For educational events */
    EDUCATIONAL("Educational"),
    /** For social gatherings or networking events */
    SOCIAL("Social"),
    /** For events aimed at raising funds */
    FUNDRAISER("Fundraiser"),
    /** For events celebrating or showcasing culture */
    CULTURAL("Cultural"),
    /** For any other events that don't fit into the above categories */
    OTHERS("Others");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets the display name for the category.
     * @return A string representing the display name.
     */
    public String getDisplayName() {
        return displayName;
    }
}