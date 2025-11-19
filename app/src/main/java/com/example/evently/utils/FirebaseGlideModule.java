package com.example.evently.utils;

import java.io.InputStream;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.StorageReference;

/**
 * The following class defines the MyAppGlideModule, which is from the firebaseUI storage docs:
 * https://firebaseopensource.com/projects/firebase/firebaseui-android/storage/readme
 *
 * It allows glide to work for fetching images.
 */
@GlideModule
public class FirebaseGlideModule extends AppGlideModule {

    @Override
    public void registerComponents(Context context, Glide glide, Registry registry) {
        // Register FirebaseImageLoader to handle StorageReference
        registry.append(
                StorageReference.class, InputStream.class, new FirebaseImageLoader.Factory());
    }
}
