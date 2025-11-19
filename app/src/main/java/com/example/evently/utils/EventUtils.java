package com.example.evently.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import com.example.evently.data.model.Category;
import com.example.evently.data.model.Event;

/**
 * A utility class to help with anything related to events.
 */
public final class EventUtils {

    /**
     * Filters a list of events and returns a new list containing only the events
     * that match the specified category.
     *
     * @param events   The original list of events to filter.
     * @param category The category to filter by.
     * @return A new ArrayList of events that match the category.
     *         Returns an empty list if no events match.
     */
    public static ArrayList<Event> filterByCategory(List<Event> events, Category category) {
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

    /**
     * Generate a QR code that, when scanned, redirects to the event details page.
     * @param eventID ID of the event to redirect to.
     * @return A bitmap that can be shown in an ImageView.
     */
    public static Bitmap generateQR(UUID eventID) {
        final var barcodeEncoder = new BarcodeEncoder();
        final var targetURI = eventDetailsURI(eventID);
        try {
            return barcodeEncoder.encodeBitmap(targetURI, BarcodeFormat.QR_CODE, 600, 600);
        } catch (WriterException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Generate the URI (deep link) for a particular event's details page.
     * Based on AndroidManifest.xml QR intent filter
     * @param eventID ID of the event to redirect to
     */
    private static String eventDetailsURI(UUID eventID) {
        try {
            // NOTE (chase): Can't use the encode method that takes a StandardCharset argument
            // because
            // its restricted to higher API level.
            final var encoding = StandardCharsets.UTF_8.toString();
            final String uuidParam = URLEncoder.encode(eventID.toString(), encoding);
            return "http://logout.app/evently?" + IntentConstants.QR_EVENT_INTENT_ID_KEY + "="
                    + uuidParam;
        } catch (UnsupportedEncodingException e) {
            // Literally impossible.
            throw new RuntimeException(e);
        }
    }
}
