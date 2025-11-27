package com.example.kolonnawabarbellgym;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class FullScreenImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        ImageView ivFullScreen = findViewById(R.id.ivFullScreen);
        ImageView ivClose = findViewById(R.id.ivClose);

        // Get image bytes from intent
        byte[] imageBytes = getIntent().getByteArrayExtra("image_bytes");

        if (imageBytes != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            ivFullScreen.setImageBitmap(bitmap);
        }

        // Close button
        ivClose.setOnClickListener(v -> finish());

        // Also close when tapping the image
        ivFullScreen.setOnClickListener(v -> finish());
    }
}