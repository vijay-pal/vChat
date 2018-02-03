package com.android.pal.chat.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.android.pal.chat.R;
import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Created by admirar on 12/25/17.
 */

public class ImageViewActivity extends AppCompatActivity {

    private FirebaseStorage storage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);
        ImageView imageView =  findViewById(R.id.image_view);
        storage = FirebaseStorage.getInstance();
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.getBooleanExtra("is_local", false)) {
                imageView.setImageURI(Uri.parse(intent.getStringExtra("uri")));
            } else {
                final StorageReference httpsReference = storage.getReferenceFromUrl(intent.getStringExtra("downloadUri"));
                Glide.with(this)
                        .using(new FirebaseImageLoader())
                        .load(httpsReference)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .into(imageView);
            }
        }
    }
}
