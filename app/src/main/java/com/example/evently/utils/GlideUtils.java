package com.example.evently.utils;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.ObjectKey;
import com.google.firebase.storage.StorageReference;

import com.example.evently.data.generic.Promise;

/**
 * The following class contains helper methods for working with Glide.
 * @author alexander-b
 */
public class GlideUtils {

    /**
     * The following function attempts to find the posterRef in Storage, and store it into the event
     * poster holder. android.R.drawable.ic_menu_report_image is used while searching or if the image
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
        Promise.promise(posterReference.getMetadata()).thenRun(metadata -> {
            // Gets updated time
            long lastUpdated = metadata.getUpdatedTimeMillis();

            // Loads image into provided ImageView
            Glide.with(imageView.getContext())
                    .load(posterReference)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(
                                @Nullable GlideException e,
                                Object model,
                                Target<Drawable> target,
                                boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(
                                Drawable resource,
                                Object model,
                                Target<Drawable> target,
                                DataSource dataSource,
                                boolean isFirstResource) {
                            imageView.setImageTintList(null);
                            return false;
                        }
                    })
                    .signature(new ObjectKey(lastUpdated)) // Updates cache if changed
                    .into(imageView);
        });
    }
}
