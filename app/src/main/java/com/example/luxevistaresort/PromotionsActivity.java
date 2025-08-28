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

public class PromotionsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PromotionAdapter adapter;
    private ArrayList<Promotion> promotions;
    private DBHelper dbHelper;
    private TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promotions);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        ImageView btnHome = findViewById(R.id.btnHome);
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(PromotionsActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        recyclerView = findViewById(R.id.recyclerPromotions);
        emptyView = findViewById(R.id.emptyPromotions);
        dbHelper = new DBHelper(this);
        promotions = new ArrayList<>();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PromotionAdapter(this, promotions);
        recyclerView.setAdapter(adapter);

        loadPromotions();
    }

    private void loadPromotions() {
        promotions.clear();

        // Show only active promotions (active = 1)
        Cursor c = dbHelper.getReadableDatabase().rawQuery("SELECT * FROM promotions WHERE active = 1 ORDER BY start_date DESC", null);
        if (c != null && c.moveToFirst()) {
            do {
                int id = c.getInt(c.getColumnIndexOrThrow("id"));
                String title = c.getString(c.getColumnIndexOrThrow("title"));
                String description = c.getString(c.getColumnIndexOrThrow("description"));
                String start = c.getString(c.getColumnIndexOrThrow("start_date"));
                String end = c.getString(c.getColumnIndexOrThrow("end_date"));
                int active = c.getInt(c.getColumnIndexOrThrow("active"));
                String images = c.getString(c.getColumnIndexOrThrow("images"));

                promotions.add(new Promotion(id, title, description, start, end, active, images));
            } while (c.moveToNext());
            c.close();
        }

        if (promotions.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
        }
    }
}
