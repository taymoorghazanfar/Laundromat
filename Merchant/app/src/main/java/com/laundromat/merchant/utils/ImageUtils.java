package com.laundromat.merchant.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.laundromat.merchant.R;
import com.laundromat.merchant.prefs.Session;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ImageUtils {

    public static String uriToBase64(Context context, Uri uri) {

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] imageBytes = stream.toByteArray();

            return Base64.encodeToString(imageBytes, Base64.DEFAULT);

        } catch (IOException e) {

            e.printStackTrace();
            return null;
        }
    }

    public static void showImage(Context context, String imageUri) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater layoutInflater = ((Activity) context).getLayoutInflater();

        final View dialogView = layoutInflater
                .inflate(R.layout.dialog_show_image, null);
        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();
        dialog.setCancelable(true);

        ImageView imageView = dialogView.findViewById(R.id.image_view);

        Picasso.get()
                .load(imageUri)
                .into(imageView);

        dialog.show();
    }
}
