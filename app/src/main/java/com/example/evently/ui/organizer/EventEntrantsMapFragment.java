package com.example.evently.ui.organizer;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import com.example.evently.ui.model.EventViewModel;

/**
 * Fragment for showing all entrant locations in a map!
 */
public class EventEntrantsMapFragment extends SupportMapFragment implements OnMapReadyCallback {
    private EventViewModel eventViewModel;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        eventViewModel = new ViewModelProvider(requireParentFragment()).get(EventViewModel.class);

        this.getMapAsync(this);
    }

    public void onMapReady(@NonNull GoogleMap gMap) {
        // TODO (chase): Might have to figure out a way to not let the parent scroll view
        //  intercept google map touches.
        gMap.getUiSettings().setZoomControlsEnabled(true);
        eventViewModel.getEventEntrantsLive().observe(getViewLifecycleOwner(), eventEntrants -> {
            final var entrantLocations = eventEntrants.locations();
            if (entrantLocations.isEmpty()) {
                // Nothing to do!
                return;
            }
            // We are going to use a bounds builder to figure out the bounds
            // encompassing all markers.
            LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
            // Add all the locations per entrant as markers.
            entrantLocations.forEach((email, loc) -> {
                final var latLng = new LatLng(loc.getLatitude(), loc.getLongitude());
                final var markerOpts = new MarkerOptions().position(latLng).title(email);
                gMap.addMarker(markerOpts);
                boundsBuilder.include(latLng);
            });
            // Move the camera such that it shows all the markers in view.
            final var cameraUpdate = CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 50);
            gMap.animateCamera(cameraUpdate);
        });
    }
}
