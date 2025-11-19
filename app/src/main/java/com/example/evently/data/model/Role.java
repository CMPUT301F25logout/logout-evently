package com.example.evently.data.model;

import androidx.annotation.DrawableRes;

import com.example.evently.R;

/**
 * App viewer role. This is used in the spinner for role selection.
 * @param icon An icon for the role.
 * @param name A name for the role.
 */
public record Role(@DrawableRes int icon, String name) {
    public static Role EntrantRole = new Role(R.drawable.ic_entrant_role, "Entrant");
    public static Role OrganizerRole = new Role(R.drawable.ic_organizer_role, "Organizer");
    public static Role AdminRole = new Role(R.drawable.ic_admin_role, "Admin");
}
