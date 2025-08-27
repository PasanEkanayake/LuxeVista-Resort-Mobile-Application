package com.example.luxevistaresort;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class BookRoomActivity extends AppCompatActivity {

    DatePicker startDatePicker, endDatePicker;
    EditText edtGuests;
    Button btnConfirm;
    DBHelper dbHelper;
    int roomId;
    private int userId;
    private static final String PREFS_NAME = "LuxeVistaPrefs";
    private static final String KEY_USER_ID = "user_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_room);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        startDatePicker = findViewById(R.id.startDatePicker);
        endDatePicker = findViewById(R.id.endDatePicker);
        edtGuests = findViewById(R.id.edtGuests);
        btnConfirm = findViewById(R.id.btnConfirmBooking);

        dbHelper = new DBHelper(this);
        roomId = getIntent().getIntExtra("room_id", -1);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        userId = prefs.getInt(KEY_USER_ID, -1);

        if (userId == -1) {
            Toast.makeText(this, "Please log in first.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        btnConfirm.setOnClickListener(v -> confirmBooking());
    }

    private void confirmBooking() {
        String startDate = startDatePicker.getYear() + "-" + (startDatePicker.getMonth()+1) + "-" + startDatePicker.getDayOfMonth();
        String endDate = endDatePicker.getYear() + "-" + (endDatePicker.getMonth()+1) + "-" + endDatePicker.getDayOfMonth();

        double totalPrice = 0;
        Cursor cursor = dbHelper.getAllRooms();
        if (cursor.moveToFirst()) {
            do {
                if (cursor.getInt(cursor.getColumnIndexOrThrow("id")) == roomId) {
                    totalPrice = cursor.getDouble(cursor.getColumnIndexOrThrow("price_per_night"));
                    break;
                }
            } while (cursor.moveToNext());
        }
        cursor.close();

        long bookingId = dbHelper.bookRoom(userId, roomId, startDate, endDate, totalPrice);
        if (bookingId > 0) {
            Toast.makeText(this, "Booking Confirmed!", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(BookRoomActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Failed to book room.", Toast.LENGTH_LONG).show();
        }
    }

}
