package com.example.evently.ui.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import com.example.evently.databinding.BottomSheetQrBinding;
import com.example.evently.utils.EventUtils;

public class EventQRDialogFragment extends BottomSheetDialogFragment {
    private BottomSheetQrBinding binding;
    private UUID eventID;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = BottomSheetQrBinding.inflate(getLayoutInflater(), container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // We should have been passed eventID as argument.
        final var args = getArguments();
        assert args != null;
        eventID = (UUID) args.getSerializable("eventID");
        assert eventID != null;

        // Use the eventID to generate the QR code.
        final var bitmap = EventUtils.generateQR(eventID);
        binding.qrImage.setImageBitmap(bitmap);

        // Get the shareable URI to this image.
        final var shareURI = getShareableURI(bitmap);

        // The share button should pop up a "share sheet"
        binding.qrShareBtn.setOnClickListener(v -> {
            final var shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, shareURI);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            shareIntent.setType("image/jpeg");
            startActivity(Intent.createChooser(shareIntent, null));
            dismiss();
        });
    }

    private Uri getShareableURI(Bitmap bitmap) {
        final var ctx = requireContext();
        // Save the bitmap to cache.
        final var imagesFolder = new File(ctx.getCacheDir(), "qr");
        if (!imagesFolder.exists() && !imagesFolder.mkdirs()) {
            // Directory creation failed.
            throw new RuntimeException("Unable to save QR code to filesystem");
        }
        // Write it as JPEG.
        final var file = new File(imagesFolder, eventID + ".jpg");
        try {
            final var fs = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fs);
            fs.flush();
            fs.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // The URI to access this file.
        return FileProvider.getUriForFile(ctx, ctx.getPackageName() + ".qrprovider", file);
    }
}
