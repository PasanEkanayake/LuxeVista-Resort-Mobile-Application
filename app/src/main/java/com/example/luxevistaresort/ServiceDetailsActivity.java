package com.example.luxevistaresort;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ServiceDetailsActivity extends AppCompatActivity {

    private TextView serviceName, serviceDesc, servicePrice;
    private DatePicker datePicker;
    private TimePicker timePicker;
    private Button btnReserve;

    private DBHelper dbHelper;
    private int serviceId;
    private int userId = 1; // TODO: replace with logged-in user session

    private double servicePriceVal = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_details);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        serviceName = findViewById(R.id.serviceName);
        serviceDesc = findViewById(R.id.serviceDesc);
        servicePrice = findViewById(R.id.servicePrice);
        datePicker = findViewById(R.id.datePicker);
        timePicker = findViewById(R.id.timePicker);
        btnReserve = findViewById(R.id.btnReserve);

        timePicker.setIs24HourView(true);

        dbHelper = new DBHelper(this);
        serviceId = getIntent().getIntExtra("service_id", -1);

        loadServiceDetails();

        btnReserve.setOnClickListener(v -> bookService());
    }

    private void loadServiceDetails() {
        Cursor cursor = dbHelper.getAllServices();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                if (id == serviceId) {
                    String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                    String desc = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                    servicePriceVal = cursor.getDouble(cursor.getColumnIndexOrThrow("price"));

                    serviceName.setText(name);
                    serviceDesc.setText(desc);
                    servicePrice.setText("Price: Rs." + servicePriceVal);
                    break;
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
    }

    private void bookService() {
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth() + 1;
        int year = datePicker.getYear();
        String date = year + "-" + month + "-" + day;

        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();
        String time = hour + ":" + (minute < 10 ? "0" + minute : minute);

        long result = dbHelper.bookService(userId, serviceId, date, time, servicePriceVal);

        if (result > 0) {
            Toast.makeText(this, "Service reserved successfully!", Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to reserve service.", Toast.LENGTH_LONG).show();
        }
    }
}
