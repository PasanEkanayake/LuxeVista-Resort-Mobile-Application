package com.example.luxevistaresort;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "LuxeVistaPrefs";
    private static final String KEY_USER_ID = "user_id";

    private DBHelper dbHelper;
    private TextView tvWelcome, tvPromoCount, tvAttractionCount;

    private CardView cardRooms, cardServices, cardBookings, cardOffers, cardAttractions, cardProfile, cardLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // If not logged in, Go to Login
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int userId = prefs.getInt(KEY_USER_ID, -1);
        if (userId == -1) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        dbHelper = new DBHelper(this);

        tvWelcome = findViewById(R.id.tvWelcome);
        tvPromoCount = findViewById(R.id.tvPromoCount);
        tvAttractionCount = findViewById(R.id.tvAttractionCount);

        cardRooms = findViewById(R.id.cardRooms);
        cardServices = findViewById(R.id.cardServices);
        cardBookings = findViewById(R.id.cardBookings);
        cardOffers = findViewById(R.id.cardOffers);
        cardAttractions = findViewById(R.id.cardAttractions);
        cardProfile = findViewById(R.id.cardProfile);
        cardLogout = findViewById(R.id.cardLogout);

        // Greet the user
        String userName = getUserFullName(userId);
        tvWelcome.setText(userName == null ? "Welcome to LuxeVista" : "Welcome, " + userName);

        tvPromoCount.setText(String.valueOf(getActivePromotionsCount()));
        tvAttractionCount.setText(String.valueOf(getAttractionsCount()));

        // Navigation Section
        cardRooms.setOnClickListener(v -> startActivity(new Intent(this, RoomsActivity.class)));
        cardServices.setOnClickListener(v -> startActivity(new Intent(this, ServicesActivity.class)));
        cardBookings.setOnClickListener(v -> startActivity(new Intent(this, BookingsActivity.class)));
//        cardOffers.setOnClickListener(v -> startActivity(new Intent(this, ExploreActivity.class)));
//        cardAttractions.setOnClickListener(v -> startActivity(new Intent(this, ExploreActivity.class)));
//        cardProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));

        cardLogout.setOnClickListener(v -> {
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().remove(KEY_USER_ID).apply();
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleExit();
            }
        });
    }

    private void handleExit() {
        new AlertDialog.Builder(this)
                .setTitle("Exit LuxeVista")
                .setMessage("Are you sure you want to exit the app?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    dialog.dismiss();
                    finishAffinity();
                    System.exit(0);
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private String getUserFullName(int userId) {
        String name = null;
        Cursor c = null;
        try {
            c = dbHelper.getReadableDatabase()
                    .rawQuery("SELECT full_name FROM users WHERE id = ?", new String[]{String.valueOf(userId)});
            if (c.moveToFirst()) name = c.getString(0);
        } finally {
            if (c != null) c.close();
        }
        return name;
    }

    private int getActivePromotionsCount() {
        int count = 0;
        Cursor c = null;
        try {
            c = dbHelper.getReadableDatabase()
                    .rawQuery("SELECT COUNT(*) FROM promotions WHERE active = 1", null);
            if (c.moveToFirst()) count = c.getInt(0);
        } finally {
            if (c != null) c.close();
        }
        return count;
    }

    private int getAttractionsCount() {
        int count = 0;
        Cursor c = null;
        try {
            c = dbHelper.getReadableDatabase()
                    .rawQuery("SELECT COUNT(*) FROM attractions", null);
            if (c.moveToFirst()) count = c.getInt(0);
        } finally {
            if (c != null) c.close();
        }
        return count;
    }
}
