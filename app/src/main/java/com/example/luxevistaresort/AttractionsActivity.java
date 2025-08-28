package com.example.luxevistaresort;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AttractionsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView emptyView;
    private AttractionAdapter adapter;
    private ArrayList<Attraction> attractions;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attractions);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        ImageView btnHome = findViewById(R.id.btnHome);
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(AttractionsActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        recyclerView = findViewById(R.id.recyclerAttractions);
        emptyView = findViewById(R.id.emptyAttractions);
        dbHelper = new DBHelper(this);
        attractions = new ArrayList<>();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AttractionAdapter(this, attractions);
        recyclerView.setAdapter(adapter);

        loadAttractions();
    }

    private void loadAttractions() {
        attractions.clear();
        Cursor c = dbHelper.getReadableDatabase().rawQuery("SELECT * FROM attractions ORDER BY id ASC", null);
        if (c != null && c.moveToFirst()) {
            do {
                int id = c.getInt(c.getColumnIndexOrThrow("id"));
                String title = c.getString(c.getColumnIndexOrThrow("title"));
                String desc = c.getString(c.getColumnIndexOrThrow("description"));
                String distance = c.getString(c.getColumnIndexOrThrow("distance"));
                String contact = c.getString(c.getColumnIndexOrThrow("contact"));
                String images = "";
                int idx = c.getColumnIndex("images");
                if (idx != -1) images = c.getString(idx);

                attractions.add(new Attraction(id, title, desc, distance, contact, images));
            } while (c.moveToNext());
            c.close();
        }

        if (attractions.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
        }
    }
}
