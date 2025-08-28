package com.example.luxevistaresort;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class BookingsActivity extends AppCompatActivity {

    private ListView bookingsListView;
    private Spinner bookingsFilter;
    private ImageView btnBack;
    private DBHelper dbHelper;
    private ArrayList<BookingItem> bookingList;
    private ArrayList<Integer> bookingIds;
    private ArrayList<String> bookingTypes; // "room" or "service"
    private static final String PREFS_NAME = "LuxeVistaPrefs";
    private static final String KEY_USER_ID = "user_id";
    private int userId;

    private BookingAdapter adapter; // custom adapter for card-style items

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookings);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        ImageView btnHome = findViewById(R.id.btnHome);
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(BookingsActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        // Get logged-in user ID
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        userId = prefs.getInt(KEY_USER_ID, -1);
        if (userId == -1) {
            Toast.makeText(this, "Please log in first.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Initialize views
        btnBack = findViewById(R.id.btnBack);
        bookingsFilter = findViewById(R.id.bookingsFilter);
        bookingsListView = findViewById(R.id.bookingsListView);

        dbHelper = new DBHelper(this);
        bookingList = new ArrayList<>();
        bookingIds = new ArrayList<>();
        bookingTypes = new ArrayList<>();

        // Load all bookings initially
        loadBookings("all");

        // Set custom adapter
        adapter = new BookingAdapter(this, bookingList);
        bookingsListView.setAdapter(adapter);

        // Handle item click
        bookingsListView.setOnItemClickListener((parent, view, position, id) -> {
            Intent detailsIntent = new Intent(BookingsActivity.this, BookingDetailsActivity.class);
            detailsIntent.putExtra("BOOKING_ID", bookingIds.get(position));
            detailsIntent.putExtra("BOOKING_TYPE", bookingTypes.get(position));
            startActivity(detailsIntent);
        });

        // Back button click
        btnBack.setOnClickListener(v -> finish());

        // Setup filter spinner
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item,
                new String[]{"All", "Rooms", "Services"});
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bookingsFilter.setAdapter(filterAdapter);

        bookingsFilter.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                switch (selected) {
                    case "Rooms":
                        loadBookings("room");
                        break;
                    case "Services":
                        loadBookings("service");
                        break;
                    default:
                        loadBookings("all");
                        break;
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                loadBookings("all");
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void loadBookings(String filter) {
        bookingList.clear();
        bookingIds.clear();
        bookingTypes.clear();

        // Load room bookings
        if (filter.equals("all") || filter.equals("room")) {
            Cursor roomCursor = dbHelper.getRoomBookingsByUserDetailed(userId);
            if (roomCursor.moveToFirst()) {
                do {
                    int id = roomCursor.getInt(roomCursor.getColumnIndexOrThrow("id"));
                    String roomName = roomCursor.getString(roomCursor.getColumnIndexOrThrow("room_name"));
                    String roomType = roomCursor.getString(roomCursor.getColumnIndexOrThrow("room_type"));
                    String startDate = roomCursor.getString(roomCursor.getColumnIndexOrThrow("start_date"));
                    String endDate = roomCursor.getString(roomCursor.getColumnIndexOrThrow("end_date"));
                    String status = roomCursor.getString(roomCursor.getColumnIndexOrThrow("status"));

                    bookingIds.add(id);
                    bookingTypes.add("room");

                    // Title: Room type + name
                    // Date: Start date
                    // Time: End date (optional)
                    // Status: Status
                    bookingList.add(new BookingItem(
                            roomType + " - " + roomName,
                            startDate,
                            endDate,
                            status
                    ));
                } while (roomCursor.moveToNext());
            }
            roomCursor.close();
        }

        // Load service bookings
        if (filter.equals("all") || filter.equals("service")) {
            Cursor serviceCursor = dbHelper.getServiceBookingsByUserDetailed(userId);
            if (serviceCursor.moveToFirst()) {
                do {
                    int id = serviceCursor.getInt(serviceCursor.getColumnIndexOrThrow("id"));
                    String serviceName = serviceCursor.getString(serviceCursor.getColumnIndexOrThrow("service_name"));
                    String date = serviceCursor.getString(serviceCursor.getColumnIndexOrThrow("booking_date"));
                    String time = serviceCursor.getString(serviceCursor.getColumnIndexOrThrow("booking_time"));
                    String status = serviceCursor.getString(serviceCursor.getColumnIndexOrThrow("status"));

                    bookingIds.add(id);
                    bookingTypes.add("service");

                    bookingList.add(new BookingItem(
                            serviceName,
                            date,
                            time,
                            status
                    ));
                } while (serviceCursor.moveToNext());
            }
            serviceCursor.close();
        }
    }
}
