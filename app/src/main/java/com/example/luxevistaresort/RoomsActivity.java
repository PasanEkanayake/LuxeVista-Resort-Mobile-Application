package com.example.luxevistaresort;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RoomsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    RoomAdapter adapter;
    DBHelper dbHelper;
    ArrayList<Room> roomList;
    Spinner filterSpinner, sortSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rooms);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerViewRooms);
        filterSpinner = findViewById(R.id.spinnerFilter);
        sortSpinner = findViewById(R.id.spinnerSort);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dbHelper = new DBHelper(this);
        roomList = new ArrayList<>();
        loadRooms(null, null);

        adapter = new RoomAdapter(roomList, room -> {
            Intent i = new Intent(RoomsActivity.this, RoomDetailsActivity.class);
            i.putExtra("room_id", room.getId());
            startActivity(i);
        });
        recyclerView.setAdapter(adapter);

        // Filter
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_item,
                new String[]{"Room Type", "All", "Suite", "Deluxe", "Standard"});
        filterSpinner.setAdapter(filterAdapter);

        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String filter = parent.getItemAtPosition(pos).toString();
                loadRooms(filter.equals("All") ? null : filter, null);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Sort
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_item,
                new String[]{"Price", "Default", "Low->High", "High->Low"});
        sortSpinner.setAdapter(sortAdapter);

        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String sort = parent.getItemAtPosition(pos).toString();
                loadRooms(null, sort);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadRooms(String filterType, String sortOrder) {
        roomList.clear();
        Cursor cursor = dbHelper.getAllRooms();
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String type = cursor.getString(cursor.getColumnIndexOrThrow("room_type"));
                String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow("price_per_night"));
                int capacity = cursor.getInt(cursor.getColumnIndexOrThrow("capacity"));
                String images = cursor.getString(cursor.getColumnIndexOrThrow("images"));

                if (filterType == null || type.equalsIgnoreCase(filterType)) {
                    roomList.add(new Room(id, name, type, description, price, capacity, images));
                }
            } while (cursor.moveToNext());
        }
        cursor.close();

        // Apply sorting
        if (sortOrder != null) {
            if (sortOrder.equals("Low->High")) {
                roomList.sort((r1, r2) -> Double.compare(r1.getPrice(), r2.getPrice()));
            } else if (sortOrder.equals("High->Low")) {
                roomList.sort((r1, r2) -> Double.compare(r2.getPrice(), r1.getPrice()));
            }
        }

        if (adapter != null) adapter.notifyDataSetChanged();
    }
}
