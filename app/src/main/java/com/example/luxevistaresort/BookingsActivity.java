package com.example.luxevistaresort;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class BookingsActivity extends AppCompatActivity {

    private static final String TAG = "BookingsActivity";

    private ListView bookingsListView;
    private Spinner bookingsFilter, statusFilter;
    private ImageView btnBack, btnHome;
    private DBHelper dbHelper;
    private ArrayList<BookingItem> bookingList;
    private ArrayList<Integer> bookingIds;
    private ArrayList<String> bookingTypes; // "room" or "service"
    private static final String PREFS_NAME = "LuxeVistaPrefs";
    private static final String KEY_USER_ID = "user_id";
    private int userId;

    private BookingAdapter adapter; // custom card-style adapter

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookings);

        // Initialize views
        btnBack = findViewById(R.id.btnBack);
        btnHome = findViewById(R.id.btnHome);
        bookingsFilter = findViewById(R.id.bookingsFilter);
        statusFilter = findViewById(R.id.statusFilter);
        bookingsListView = findViewById(R.id.bookingsListView);

        btnBack.setOnClickListener(v -> finish());
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

        dbHelper = new DBHelper(this);
        bookingList = new ArrayList<>();
        bookingIds = new ArrayList<>();
        bookingTypes = new ArrayList<>();

        // Setup adapter for the ListView
        adapter = new BookingAdapter(this, bookingList);
        bookingsListView.setAdapter(adapter);

        // Item click: open details
        bookingsListView.setOnItemClickListener((parent, view, position, id) -> {
            Intent detailsIntent = new Intent(BookingsActivity.this, BookingDetailsActivity.class);
            detailsIntent.putExtra("BOOKING_ID", bookingIds.get(position));
            detailsIntent.putExtra("BOOKING_TYPE", bookingTypes.get(position));
            startActivity(detailsIntent);
        });

        // --- Bookings type spinner (All / Rooms / Services) ---
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item,
                new String[]{"All", "Rooms", "Services"});
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bookingsFilter.setAdapter(filterAdapter);

        // --- Status spinner (Confirmed / Cancelled) with Confirmed default ---
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item,
                new String[]{"Confirmed", "Cancelled"});
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusFilter.setAdapter(statusAdapter);
        statusFilter.setSelection(0); // default = Confirmed

        // Helper that maps spinner values and applies filters
        Runnable applyFilters = () -> {
            // Map bookingsFilter value to "all"/"room"/"service"
            String typeSelected = bookingsFilter.getSelectedItem().toString();
            String mappedType = "all";
            if ("Rooms".equalsIgnoreCase(typeSelected) || "Room".equalsIgnoreCase(typeSelected)) mappedType = "room";
            else if ("Services".equalsIgnoreCase(typeSelected) || "Service".equalsIgnoreCase(typeSelected)) mappedType = "service";

            // Map statusFilter value to "CONFIRMED"/"CANCELLED"
            String statusSelected = statusFilter.getSelectedItem().toString();
            String mappedStatus = "CONFIRMED";
            if ("Cancelled".equalsIgnoreCase(statusSelected) || "CANCELLED".equalsIgnoreCase(statusSelected)) mappedStatus = "CANCELLED";

            loadBookings(mappedType, mappedStatus);
            adapter.notifyDataSetChanged();
        };

        // Spinner listeners call applyFilters when selection changes
        bookingsFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilters.run();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                applyFilters.run();
            }
        });

        statusFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilters.run();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                applyFilters.run();
            }
        });

        // Initial load: Confirmed default
        loadBookings("all", "CONFIRMED");
        adapter.notifyDataSetChanged();
    }

    /**
     * Load bookings from DB
     * @param filter "all", "room", or "service"
     * @param status "CONFIRMED" or "CANCELLED"
     */
    private void loadBookings(String filter, String status) {
        bookingList.clear();
        bookingIds.clear();
        bookingTypes.clear();

        Log.d(TAG, "Loading bookings: filter=" + filter + " status=" + status);

        // Load room bookings
        if (filter.equals("all") || filter.equals("room")) {
            Cursor roomCursor = dbHelper.getRoomBookingsByUserDetailed(userId);
            if (roomCursor != null) {
                if (roomCursor.moveToFirst()) {
                    do {
                        String bookingStatus = roomCursor.getString(roomCursor.getColumnIndexOrThrow("status"));
                        if (!bookingStatus.equalsIgnoreCase(status)) continue;

                        int id = roomCursor.getInt(roomCursor.getColumnIndexOrThrow("id"));
                        String roomName = roomCursor.getString(roomCursor.getColumnIndexOrThrow("room_name"));
                        String roomType = roomCursor.getString(roomCursor.getColumnIndexOrThrow("room_type"));
                        String startDate = roomCursor.getString(roomCursor.getColumnIndexOrThrow("start_date"));
                        String endDate = roomCursor.getString(roomCursor.getColumnIndexOrThrow("end_date"));

                        bookingIds.add(id);
                        bookingTypes.add("room");

                        bookingList.add(new BookingItem(
                                roomType + " - " + roomName,
                                startDate,
                                endDate,
                                bookingStatus
                        ));
                    } while (roomCursor.moveToNext());
                }
                roomCursor.close();
            }
        }

        // Load service bookings
        if (filter.equals("all") || filter.equals("service")) {
            Cursor serviceCursor = dbHelper.getServiceBookingsByUserDetailed(userId);
            if (serviceCursor != null) {
                if (serviceCursor.moveToFirst()) {
                    do {
                        String bookingStatus = serviceCursor.getString(serviceCursor.getColumnIndexOrThrow("status"));
                        if (!bookingStatus.equalsIgnoreCase(status)) continue;

                        int id = serviceCursor.getInt(serviceCursor.getColumnIndexOrThrow("id"));
                        String serviceName = serviceCursor.getString(serviceCursor.getColumnIndexOrThrow("service_name"));
                        String date = serviceCursor.getString(serviceCursor.getColumnIndexOrThrow("booking_date"));
                        String time = serviceCursor.getString(serviceCursor.getColumnIndexOrThrow("booking_time"));

                        bookingIds.add(id);
                        bookingTypes.add("service");

                        bookingList.add(new BookingItem(
                                serviceName,
                                date,
                                time,
                                bookingStatus
                        ));
                    } while (serviceCursor.moveToNext());
                }
                serviceCursor.close();
            }
        }

        Log.d(TAG, "Loaded bookings count: " + bookingList.size());
    }
}
