package com.example.luxevistaresort;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ServicesActivity extends AppCompatActivity {

    private LinearLayout servicesContainer;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_services);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        servicesContainer = findViewById(R.id.servicesContainer);
        dbHelper = new DBHelper(this);

        loadServices();
    }

    private void loadServices() {
        Cursor cursor = dbHelper.getAllServices();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String desc = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow("price"));

                TextView tv = new TextView(this);
                tv.setText(name + "\n" + desc + "\nPrice: Rs." + price);
                tv.setPadding(20, 30, 20, 30);
                tv.setTextSize(16f);
                tv.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);

                tv.setOnClickListener(v -> {
                    Intent intent = new Intent(ServicesActivity.this, ServiceDetailsActivity.class);
                    intent.putExtra("service_id", id);
                    startActivity(intent);
                });

                servicesContainer.addView(tv);

            } while (cursor.moveToNext());
            cursor.close();
        } else {
            Toast.makeText(this, "No services available.", Toast.LENGTH_SHORT).show();
        }
    }
}
