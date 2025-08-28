package com.example.luxevistaresort;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class RoomDetailsActivity extends AppCompatActivity {

    TextView txtName, txtDescription, txtPrice;
    ImageView imageView;
    Button btnBook;
    DBHelper dbHelper;
    int roomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_details);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        ImageView btnHome = findViewById(R.id.btnHome);
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(RoomDetailsActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        txtName = findViewById(R.id.txtRoomName);
        txtDescription = findViewById(R.id.txtRoomDescription);
        txtPrice = findViewById(R.id.txtRoomPrice);
        imageView = findViewById(R.id.imgRoom);
        btnBook = findViewById(R.id.btnBookRoom);

        dbHelper = new DBHelper(this);
        roomId = getIntent().getIntExtra("room_id", -1);

        loadRoomDetails();

        btnBook.setOnClickListener(v -> {
            Intent i = new Intent(RoomDetailsActivity.this, BookRoomActivity.class);
            i.putExtra("room_id", roomId);
            startActivity(i);
        });
    }

    private void loadRoomDetails() {
        Cursor cursor = dbHelper.getAllRooms();
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                if (id == roomId) {
                    String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                    String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                    double price = cursor.getDouble(cursor.getColumnIndexOrThrow("price_per_night"));

                    txtName.setText(name);
                    txtDescription.setText(description);
                    txtPrice.setText("Rs. " + price + " per night");
                    break;
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
    }
}
