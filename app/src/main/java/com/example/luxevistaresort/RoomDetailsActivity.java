package com.example.luxevistaresort;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.File;

public class RoomDetailsActivity extends AppCompatActivity {

    private static final String TAG = "RoomDetailsActivity";

    TextView txtName, txtDescription, txtPrice;
    ImageView imageView;
    Button btnBook;
    DBHelper dbHelper;
    int roomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_details);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        ImageView btnHome = findViewById(R.id.btnHome);
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(RoomDetailsActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        txtName = findViewById(R.id.txtRoomName);
        txtDescription = findViewById(R.id.txtRoomDescription);
        txtPrice = findViewById(R.id.txtRoomPrice);
        imageView = findViewById(R.id.imgRoom);
        btnBook = findViewById(R.id.btnBookRoom);

        dbHelper = new DBHelper(this);
        roomId = getIntent().getIntExtra("room_id", -1);

        loadRoomDetails();

        btnBook.setOnClickListener(v -> {
            Intent i = new Intent(RoomDetailsActivity.this, BookRoomActivity.class);
            i.putExtra("room_id", roomId);
            startActivity(i);
        });
    }

    private void loadRoomDetails() {
        if (roomId == -1) {
            Log.w(TAG, "Invalid room id");
            return;
        }

        Cursor cursor = dbHelper.getReadableDatabase()
                .rawQuery("SELECT * FROM rooms WHERE id = ?", new String[]{String.valueOf(roomId)});

        if (cursor != null && cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
            double price = cursor.getDouble(cursor.getColumnIndexOrThrow("price_per_night"));
            String images = "";
            try {
                images = cursor.getString(cursor.getColumnIndexOrThrow("images"));
            } catch (Exception e) {
                Log.d(TAG, "images column missing or null", e);
            }

            txtName.setText(name);
            txtDescription.setText(description);
            txtPrice.setText("Rs. " + price + " per night");

            // --- image loading logic ---
            String firstImage = null;
            if (images != null && !images.trim().isEmpty()) {
                firstImage = images.split(",")[0].trim();
            }

            if (firstImage != null && !firstImage.isEmpty()) {
                // 1) try drawable resource (strip extension if present)
                String resourceName = firstImage.contains(".") ? firstImage.substring(0, firstImage.lastIndexOf('.')) : firstImage;
                int resId = getResources().getIdentifier(resourceName, "drawable", getPackageName());

                if (resId != 0) {
                    Log.d(TAG, "Loading drawable resource: " + resourceName + " (resId=" + resId + ")");
                    Glide.with(this)
                            .load(resId)
                            .placeholder(R.drawable.ic_image_placeholder)
                            .listener(glideListener(firstImage))
                            .into(imageView);
                }
                // 2) else if it's an http/https url
                else if (firstImage.startsWith("http://") || firstImage.startsWith("https://")) {
                    Log.d(TAG, "Loading remote URL: " + firstImage);
                    Glide.with(this)
                            .load(firstImage)
                            .placeholder(R.drawable.ic_image_placeholder)
                            .listener(glideListener(firstImage))
                            .into(imageView);
                }
                // 3) else try assets folder (file:///android_asset/...)
                else {
                    String assetPath = "file:///android_asset/" + firstImage;
                    Log.d(TAG, "Attempting asset load: " + assetPath);
                    Glide.with(this)
                            .load(assetPath)
                            .placeholder(R.drawable.ic_image_placeholder)
                            .listener(glideListener(firstImage))
                            .into(imageView);

                    // 4) also attempt internal files dir fallback if exists
                    File f = new File(getFilesDir(), firstImage);
                    if (f.exists()) {
                        Log.d(TAG, "Found file in internal storage: " + f.getAbsolutePath());
                        Glide.with(this)
                                .load(f)
                                .placeholder(R.drawable.ic_image_placeholder)
                                .listener(glideListener(firstImage))
                                .into(imageView);
                    }
                }
            } else {
                // no image => placeholder
                imageView.setImageResource(R.drawable.ic_image_placeholder);
            }
        } else {
            Log.w(TAG, "Room not found for id: " + roomId);
        }

        if (cursor != null) cursor.close();
    }

    private RequestListener<Drawable> glideListener(final String modelId) {
        return new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                Log.e(TAG, "Glide load failed for: " + model + " (original: " + modelId + ")", e);
                // return false so placeholder is set by Glide
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                Log.d(TAG, "Glide loaded: " + model);
                return false;
            }
        };
    }
}
