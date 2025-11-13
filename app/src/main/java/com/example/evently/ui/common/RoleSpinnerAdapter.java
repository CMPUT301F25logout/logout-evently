package com.example.evently.ui.common;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;

import com.example.evently.R;
import com.example.evently.data.model.Role;

/**
 * Adapter for the role selector dropdown. Provide it with the select-able roles.
 * @see ArchitectureActivity
 */
public class RoleSpinnerAdapter extends ArrayAdapter<Role> {

    private final LayoutInflater inflater;

    public RoleSpinnerAdapter(Context context, List<Role> items) {
        super(context, 0, items);
        inflater = LayoutInflater.from(context);
    }

    @NonNull @Override
    public View getView(int position, View existingView, @NonNull ViewGroup parent) {
        final var item = getItem(position);
        assert item != null;

        // Populate the icon and name depending on the layout.
        final View targetView;
        // This could literally use Object.requireNonNullElseGet but static analysis in Java sucks.
        if (existingView == null) {
            // No existing view implies we are displaying just the spinner, not dropdown.
            targetView = inflater.inflate(R.layout.role_item_display, parent, false);
        } else {
            // Existing view, we just need to update the image and the name.
            targetView = existingView;
        }
        final ImageView icon = targetView.findViewById(R.id.roleImage);
        icon.setImageResource(item.icon());
        final TextView name = targetView.findViewById(R.id.roleName);
        if (name != null) name.setText(item.name());
        return targetView;
    }

    @Override
    public View getDropDownView(int position, View existingView, @NonNull ViewGroup parent) {
        // Dropdown view, always uses role_item
        final var item = getItem(position);
        assert item != null;

        final View targetView;
        if (existingView == null) {
            targetView = inflater.inflate(R.layout.role_dropdown_item, parent, false);
        } else {
            targetView = existingView;
        }
        ImageView icon = targetView.findViewById(R.id.roleImage);
        TextView text = targetView.findViewById(R.id.roleName);
        icon.setImageResource(item.icon());
        text.setText(item.name());
        return targetView;
    }
}
