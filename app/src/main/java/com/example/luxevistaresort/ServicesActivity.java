package com.example.luxevistaresort;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

public class ServicesActivity extends AppCompatActivity {

    private LinearLayout servicesContainer;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_services);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        ImageView btnHome = findViewById(R.id.btnHome);
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(ServicesActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        servicesContainer = findViewById(R.id.servicesContainer);
        dbHelper = new DBHelper(this);

        loadServices();
    }

    private void loadServices() {
        Cursor cursor = dbHelper.getAllServices();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));

                // Load the 4 main services
                if (!name.equals("Spa") && !name.equals("Dining") &&
                        !name.equals("Cabana") && !name.equals("Tours")) {
                    continue;
                }

                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String images = cursor.getString(cursor.getColumnIndexOrThrow("images"));

                // Create CardView
                CardView card = new CardView(this);
                LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dpToPx(250)
                );
                cardParams.setMargins(0, 0, 0, dpToPx(16));
                card.setLayoutParams(cardParams);
                card.setRadius(dpToPx(16));
                card.setCardElevation(dpToPx(4));

                // FrameLayout to match XML
                FrameLayout frameLayout = new FrameLayout(this);
                frameLayout.setLayoutParams(new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                ));

                // Background ImageView
                ImageView imageView = new ImageView(this);
                FrameLayout.LayoutParams imgParams = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                );
                imageView.setLayoutParams(imgParams);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setImageResource(getImageResource(images));

                // Semi-transparent overlay
                LinearLayout overlay = new LinearLayout(this);
                FrameLayout.LayoutParams overlayParams = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                );
                overlay.setLayoutParams(overlayParams);
                overlay.setOrientation(LinearLayout.VERTICAL);
                overlay.setPadding(dpToPx(24), dpToPx(24), dpToPx(24), dpToPx(24));
                overlay.setGravity(android.view.Gravity.CENTER_VERTICAL);
                overlay.setBackgroundColor(0x88000000);

                // Service name only
                TextView tvName = new TextView(this);
                tvName.setText(name);
                tvName.setTextSize(48f);
                tvName.setTextColor(ContextCompat.getColor(this, android.R.color.white));
                tvName.setTypeface(tvName.getTypeface(), android.graphics.Typeface.BOLD);

                overlay.addView(tvName);

                // Add views to frameLayout
                frameLayout.addView(imageView);
                frameLayout.addView(overlay);

                // Add frameLayout to card
                card.addView(frameLayout);

                card.setOnClickListener(v -> {
                    Intent intent = new Intent(ServicesActivity.this, ServiceDetailsActivity.class);
                    intent.putExtra("service_id", id);
                    startActivity(intent);
                });

                servicesContainer.addView(card);

            } while (cursor.moveToNext());
            cursor.close();
        } else {
            Toast.makeText(this, "No services available.", Toast.LENGTH_SHORT).show();
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    private int getImageResource(String images) {
        if (images != null && !images.isEmpty()) {
            String firstImg = images.split(",")[0];
            int resId = getResources().getIdentifier(firstImg.replace(".jpg", ""), "drawable", getPackageName());
            if (resId != 0) return resId;
        }
        return R.drawable.ic_image_placeholder;
    }
}
