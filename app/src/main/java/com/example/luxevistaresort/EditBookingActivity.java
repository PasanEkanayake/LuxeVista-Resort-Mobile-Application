package com.example.luxevistaresort;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EditBookingActivity extends AppCompatActivity {

    private DatePicker datePicker;
    private TimePicker timePicker;
    private Button saveBtn;
    private DBHelper dbHelper;
    private int bookingId;
    private String bookingType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_booking);

        timePicker = findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);

        datePicker = findViewById(R.id.datePicker);
        saveBtn = findViewById(R.id.saveBtn);
        dbHelper = new DBHelper(this);

        bookingId = getIntent().getIntExtra("BOOKING_ID", -1);
        bookingType = getIntent().getStringExtra("BOOKING_TYPE");

        loadBooking();

        saveBtn.setOnClickListener(v -> saveBooking());
    }

    private void loadBooking() {
        if ("service".equals(bookingType)) {
            Cursor c = dbHelper.getReadableDatabase().rawQuery(
                    "SELECT booking_date, booking_time FROM service_bookings WHERE id = ?",
                    new String[]{String.valueOf(bookingId)});
            if (c.moveToFirst()) {
                // Could parse and set date/time
            }
            c.close();
        }
        // Room editing could allow new dates if you want
    }

    private void saveBooking() {
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth() + 1;
        int year = datePicker.getYear();
        String newDate = year + "-" + month + "-" + day;

        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();
        String newTime = hour + ":" + (minute < 10 ? "0" + minute : minute);

        ContentValues cv = new ContentValues();
        if ("service".equals(bookingType)) {
            cv.put("booking_date", newDate);
            cv.put("booking_time", newTime);
            dbHelper.getWritableDatabase().update("service_bookings", cv, "id = ?", new String[]{String.valueOf(bookingId)});
        } else {
            cv.put("start_date", newDate);
            // Could add end_date picker too
            dbHelper.getWritableDatabase().update("room_bookings", cv, "id = ?", new String[]{String.valueOf(bookingId)});
        }

        Toast.makeText(this, "Booking updated!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
