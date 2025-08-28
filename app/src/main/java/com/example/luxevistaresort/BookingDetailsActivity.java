package com.example.luxevistaresort;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class BookingDetailsActivity extends AppCompatActivity {

    private TextView bookingDetailsText;
    private Button cancelBtn, reminderBtn;
    private DBHelper dbHelper;
    private int bookingId;
    private String bookingType;
    private static final String PREFS_NAME = "LuxeVistaPrefs";
    private static final String KEY_USER_ID = "user_id";
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_details);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        ImageView btnHome = findViewById(R.id.btnHome);
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(BookingDetailsActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        userId = prefs.getInt(KEY_USER_ID, -1);

        bookingDetailsText = findViewById(R.id.bookingDetailsText);
        cancelBtn = findViewById(R.id.cancelBtn);
        reminderBtn = findViewById(R.id.reminderBtn);
        dbHelper = new DBHelper(this);

        bookingId = getIntent().getIntExtra("BOOKING_ID", -1);
        bookingType = getIntent().getStringExtra("BOOKING_TYPE");

        loadBookingDetails();

        cancelBtn.setOnClickListener(v -> cancelBooking());
        reminderBtn.setOnClickListener(v -> setReminder());
    }

    private void loadBookingDetails() {
        StringBuilder details = new StringBuilder();

        if ("room".equals(bookingType)) {
            Cursor c = dbHelper.getReadableDatabase().rawQuery(
                    "SELECT rb.*, r.name AS room_name, r.room_type " +
                            "FROM room_bookings rb JOIN rooms r ON rb.room_id = r.id " +
                            "WHERE rb.id = ?",
                    new String[]{String.valueOf(bookingId)});
            if (c.moveToFirst()) {
                details.append("Room Booking\n\n")
                        .append("Room: ").append(c.getString(c.getColumnIndexOrThrow("room_name"))).append("\n")
                        .append("Type: ").append(c.getString(c.getColumnIndexOrThrow("room_type"))).append("\n")
                        .append("Start: ").append(c.getString(c.getColumnIndexOrThrow("start_date"))).append("\n")
                        .append("End: ").append(c.getString(c.getColumnIndexOrThrow("end_date"))).append("\n")
                        .append("Total: Rs.").append(c.getDouble(c.getColumnIndexOrThrow("total_price"))).append("\n")
                        .append("Status: ").append(c.getString(c.getColumnIndexOrThrow("status")));
            }
            c.close();
        } else if ("service".equals(bookingType)) {
            Cursor c = dbHelper.getReadableDatabase().rawQuery(
                    "SELECT sb.*, s.name AS service_name " +
                            "FROM service_bookings sb JOIN services s ON sb.service_id = s.id " +
                            "WHERE sb.id = ?",
                    new String[]{String.valueOf(bookingId)});
            if (c.moveToFirst()) {
                details.append("Service Booking\n\n")
                        .append("Service: ").append(c.getString(c.getColumnIndexOrThrow("service_name"))).append("\n")
                        .append("Date: ").append(c.getString(c.getColumnIndexOrThrow("booking_date"))).append("\n")
                        .append("Time: ").append(c.getString(c.getColumnIndexOrThrow("booking_time"))).append("\n")
                        .append("Total: Rs.").append(c.getDouble(c.getColumnIndexOrThrow("total_price"))).append("\n")
                        .append("Status: ").append(c.getString(c.getColumnIndexOrThrow("status")));
            }
            c.close();
        }

        bookingDetailsText.setText(details.toString());
    }

    private void cancelBooking() {
        int rows;
        if ("room".equals(bookingType)) {
            rows = dbHelper.cancelRoomBooking(bookingId);
        } else {
            rows = dbHelper.getWritableDatabase().update("service_bookings",
                    getCancelledValues(), "id = ?", new String[]{String.valueOf(bookingId)});
        }

        if (rows > 0) {
            Toast.makeText(this, "Booking cancelled!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, BookingsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Failed to cancel booking.", Toast.LENGTH_SHORT).show();
        }
    }

    private android.content.ContentValues getCancelledValues() {
        android.content.ContentValues cv = new android.content.ContentValues();
        cv.put("status", "CANCELLED");
        return cv;
    }

    private void setReminder() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

        try {
            if ("room".equals(bookingType)) {
                Cursor c = dbHelper.getReadableDatabase().rawQuery(
                        "SELECT start_date FROM room_bookings WHERE id = ?", new String[]{String.valueOf(bookingId)});
                if (c.moveToFirst()) {
                    String start = c.getString(c.getColumnIndexOrThrow("start_date")) + " 12:00";
                    cal.setTime(sdf.parse(start));
                    cal.add(Calendar.HOUR, -1);
                }
                c.close();
            } else {
                Cursor c = dbHelper.getReadableDatabase().rawQuery(
                        "SELECT booking_date, booking_time FROM service_bookings WHERE id = ?",
                        new String[]{String.valueOf(bookingId)});
                if (c.moveToFirst()) {
                    String dateTime = c.getString(c.getColumnIndexOrThrow("booking_date")) + " " +
                            c.getString(c.getColumnIndexOrThrow("booking_time"));
                    cal.setTime(sdf.parse(dateTime));
                    cal.add(Calendar.HOUR, -1);
                }
                c.close();
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(this, "Could not set reminder.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("BOOKING_TYPE", bookingType);
        PendingIntent pi = PendingIntent.getBroadcast(this, bookingId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        am.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);

        Toast.makeText(this, "Reminder set 1 hour before booking!", Toast.LENGTH_SHORT).show();
    }
}
