package com.example.luxevistaresort;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class ProfileActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "LuxeVistaPrefs";
    private static final String KEY_USER_ID = "user_id";
    private EditText etFullName, etPhone, etPreferences;
    private TextView tvTravelStart, tvTravelEnd;
    private Button btnPickStart, btnPickEnd, btnSave, btnLogout;
    private DBHelper dbHelper;
    private int userId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        ImageView btnHome = findViewById(R.id.btnHome);
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        etFullName = findViewById(R.id.etFullNameProfile);
        etPhone = findViewById(R.id.etPhoneProfile);
        etPreferences = findViewById(R.id.etPreferences);
        tvTravelStart = findViewById(R.id.tvTravelStart);
        tvTravelEnd = findViewById(R.id.tvTravelEnd);
        btnPickStart = findViewById(R.id.btnPickStart);
        btnPickEnd = findViewById(R.id.btnPickEnd);
        btnSave = findViewById(R.id.btnSaveProfile);
        btnLogout = findViewById(R.id.btnLogoutProfile);

        dbHelper = new DBHelper(this);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        userId = prefs.getInt(KEY_USER_ID, -1);
        if (userId == -1) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        loadUser();

        btnPickStart.setOnClickListener(v -> showDatePicker(tvTravelStart));
        btnPickEnd.setOnClickListener(v -> showDatePicker(tvTravelEnd));

        btnSave.setOnClickListener(v -> saveProfile());

        btnLogout.setOnClickListener(v -> {
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().remove(KEY_USER_ID).apply();
            Intent i = new Intent(ProfileActivity.this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        });

    }

    private void loadUser() {
        Cursor c = dbHelper.getUserById(userId);
        if (c != null && c.moveToFirst()) {
            String fullName = c.getString(c.getColumnIndexOrThrow("full_name"));
            String phone = c.getString(c.getColumnIndexOrThrow("phone"));
            String prefs = c.getString(c.getColumnIndexOrThrow("preferences"));

            etFullName.setText(fullName != null ? fullName : "");
            etPhone.setText(phone != null ? phone : "");
            String prefsText = "";
            String travelStart = "";
            String travelEnd = "";
            if (!TextUtils.isEmpty(prefs)) {
                String[] parts = prefs.split(";");
                for (String p : parts) {
                    if (p.startsWith("PREFS=")) prefsText = p.replaceFirst("PREFS=", "");
                    else if (p.startsWith("TRAVEL_START=")) travelStart = p.replaceFirst("TRAVEL_START=", "");
                    else if (p.startsWith("TRAVEL_END=")) travelEnd = p.replaceFirst("TRAVEL_END=", "");
                }
            }
            etPreferences.setText(prefsText);
            tvTravelStart.setText(TextUtils.isEmpty(travelStart) ? "Not set" : travelStart);
            tvTravelEnd.setText(TextUtils.isEmpty(travelEnd) ? "Not set" : travelEnd);
            c.close();
        }
    }

    private void showDatePicker(final TextView target) {
        final Calendar cal = Calendar.getInstance();
        int y = cal.get(Calendar.YEAR);
        int m = cal.get(Calendar.MONTH);
        int d = cal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dpd = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String val = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
            target.setText(val);
        }, y, m, d);

        dpd.show();
    }

    private void saveProfile() {
        String fullName = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String prefsText = etPreferences.getText().toString().trim();
        String travelStart = tvTravelStart.getText().toString();
        String travelEnd = tvTravelEnd.getText().toString();

        if (fullName.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Name and phone are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("PREFS=").append(prefsText.replace(";", " "));
        sb.append(";TRAVEL_START=").append(tvTravelStart.getText().toString().equals("Not set") ? "" : travelStart);
        sb.append(";TRAVEL_END=").append(tvTravelEnd.getText().toString().equals("Not set") ? "" : travelEnd);
        String finalPrefs = sb.toString();

        int rows = dbHelper.updateUser(userId, fullName, phone, finalPrefs);
        if (rows > 0) {
            Toast.makeText(this, "Profile updated.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Update failed.", Toast.LENGTH_SHORT).show();
        }
    }

}
