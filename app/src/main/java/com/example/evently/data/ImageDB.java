package com.example.evently.data;

import static com.example.evently.data.generic.Promise.promise;

import java.util.UUID;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import com.example.evently.data.generic.Promise;

public class ImageDB {

    private final StorageReference posterRef =
            FirebaseStorage.getInstance().getReference("posters");

    /**
     * Uploads a selected poster to firebase
     * @param eventID the eventID of the poster.
     * @param uri the uri of the image
     * @return a promise of the upload task
     */
    private Promise<UploadTask.TaskSnapshot> uploadPoster(UUID eventID, Uri uri) {
        StorageReference imageRef = posterRef.child(eventID.toString());
        return promise(imageRef.putFile(uri));
    }

    /**
     * The code below loads a poster into an image view, if the poster is found
     * @param eventID the eventID of the poster
     * @param context the context of the image view
     * @param imageView the imageView
     */
    private void loadIntoImageView(UUID eventID, Context context, ImageView imageView) {

        // The following code is based on the downloading files section from the firebase docs:
        // https://firebase.google.com/docs/storage/android/download-files?_gl=1
        StorageReference imageRef = posterRef.child(eventID.toString());

        Glide.with(context).load(imageRef).into(imageView);
    }
}
