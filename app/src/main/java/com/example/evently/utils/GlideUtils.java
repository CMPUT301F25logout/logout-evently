package com.example.evently.utils;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;
import com.google.firebase.storage.StorageReference;

import com.example.evently.R;
import com.example.evently.data.generic.Promise;

/**
 * The following class contains helper methods for working with Glide.
 * @author alexander-b
 */
public class GlideUtils {

    /**
     * The following function attempts to find the posterRef in Storage, and store it into the event
     * poster holder. android.R.drawable.rsvp_icon is used while searching or if the image
     * is not found in the DB.
     * <p>
     * Additionally, the following question was asked to Google, Gemini 3 Pro:
     * "I am using Glide for showing images from firebase storage in my Java android app, but my
     * images are not updating when the image in firebase storage are changed. Do you know how
     * to change this?", 2025-11-24
     * This resulted in adding a signature.
     * @param posterReference A storage reference to the poster
     * @param imageView an image view being stored into.
     */
    public static void loadPosterIntoImageView(
            StorageReference posterReference, ImageView imageView) {

        // Gets the metadata for the poster
        Promise.promise(posterReference.getMetadata())
                .thenRun(metadata -> {
                    // Gets updated time
                    long lastUpdated = metadata.getUpdatedTimeMillis();

                    // Loads image into provided ImageView
                    Glide.with(imageView.getContext())
                            .load(posterReference)
                            .signature(new ObjectKey(lastUpdated)) // Updates cache if changed
                            .into(imageView);
                })
                .catchE(e ->
                        // Just set the placeholder
                        imageView.setImageResource(R.drawable.rsvp_icon));
    }
}
