package com.example.luxevistaresort;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "luxevista.db";
    private static final int DB_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.execSQL("PRAGMA foreign_keys = ON;");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // USERS
        db.execSQL("CREATE TABLE users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "full_name TEXT NOT NULL," +
                "email TEXT UNIQUE NOT NULL," +
                "password_hash TEXT NOT NULL," +
                "phone TEXT," +
                "preferences TEXT," +
                "created_at TEXT)");

        // ROOMS
        db.execSQL("CREATE TABLE rooms (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "room_type TEXT," +
                "description TEXT," +
                "price_per_night REAL," +
                "capacity INTEGER," +
                "images TEXT," +
                "available INTEGER DEFAULT 1)");

        // SERVICES
        db.execSQL("CREATE TABLE services (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT," +
                "description TEXT," +
                "duration_minutes INTEGER," +
                "price REAL," +
                "images TEXT)");

        // ROOM BOOKINGS
        db.execSQL("CREATE TABLE room_bookings (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER," +
                "room_id INTEGER," +
                "start_date TEXT," +
                "end_date TEXT," +
                "total_price REAL," +
                "status TEXT," +
                "created_at TEXT," +
                "FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE," +
                "FOREIGN KEY(room_id) REFERENCES rooms(id) ON DELETE CASCADE)");

        // SERVICE BOOKINGS
        db.execSQL("CREATE TABLE service_bookings (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER," +
                "service_id INTEGER," +
                "booking_date TEXT," +
                "booking_time TEXT," +
                "total_price REAL," +
                "status TEXT," +
                "created_at TEXT," +
                "FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE," +
                "FOREIGN KEY(service_id) REFERENCES services(id) ON DELETE CASCADE)");

        // ATTRACTIONS
        db.execSQL("CREATE TABLE attractions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "title TEXT," +
                "description TEXT," +
                "distance TEXT," +
                "contact TEXT)");

        // PROMOTIONS
        db.execSQL("CREATE TABLE promotions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "title TEXT," +
                "description TEXT," +
                "start_date TEXT," +
                "end_date TEXT," +
                "active INTEGER DEFAULT 1)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL("DROP TABLE IF EXISTS room_bookings");
        db.execSQL("DROP TABLE IF EXISTS service_bookings");
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS rooms");
        db.execSQL("DROP TABLE IF EXISTS services");
        db.execSQL("DROP TABLE IF EXISTS attractions");
        db.execSQL("DROP TABLE IF EXISTS promotions");
        onCreate(db);
    }

    public long registerUser(String fullName, String email, String passwordHash, String phone) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("full_name", fullName);
        cv.put("email", email);
        cv.put("password_hash", passwordHash);
        cv.put("phone", phone);
        cv.put("created_at", currentTime());
        return db.insert("users", null, cv);
    }

    public Cursor getUserByEmail(String email) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT * FROM users WHERE email = ?", new String[]{email});
    }

    public boolean validateUser(String email, String passwordHash) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM users WHERE email = ? AND password_hash = ?",
                new String[]{email, passwordHash});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    public Cursor getAllRooms() {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT * FROM rooms WHERE available = 1", null);
    }

    public long addRoom(String name, String type, String description, double price, int capacity, String images) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("room_type", type);
        cv.put("description", description);
        cv.put("price_per_night", price);
        cv.put("capacity", capacity);
        cv.put("images", images);
        return db.insert("rooms", null, cv);
    }

    public long bookRoom(int userId, int roomId, String startDate, String endDate, double totalPrice) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("user_id", userId);
        cv.put("room_id", roomId);
        cv.put("start_date", startDate);
        cv.put("end_date", endDate);
        cv.put("total_price", totalPrice);
        cv.put("status", "CONFIRMED");
        cv.put("created_at", currentTime());
        return db.insert("room_bookings", null, cv);
    }

    public Cursor getBookingsByUser(int userId) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT * FROM room_bookings WHERE user_id = ? ORDER BY start_date DESC",
                new String[]{String.valueOf(userId)});
    }

    public int cancelRoomBooking(int bookingId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("status", "CANCELLED");
        return db.update("room_bookings", cv, "id = ?", new String[]{String.valueOf(bookingId)});
    }

    public Cursor getAllServices() {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT * FROM services", null);
    }

    public long addService(String name, String description, int duration, double price, String images) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("description", description);
        cv.put("duration_minutes", duration);
        cv.put("price", price);
        cv.put("images", images);
        return db.insert("services", null, cv);
    }

    public long bookService(int userId, int serviceId, String date, String time, double totalPrice) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("user_id", userId);
        cv.put("service_id", serviceId);
        cv.put("booking_date", date);
        cv.put("booking_time", time);
        cv.put("total_price", totalPrice);
        cv.put("status", "CONFIRMED");
        cv.put("created_at", currentTime());
        return db.insert("service_bookings", null, cv);
    }

    public Cursor getServiceBookingsByUser(int userId) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT * FROM service_bookings WHERE user_id = ? ORDER BY booking_date DESC",
                new String[]{String.valueOf(userId)});
    }

    private String currentTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    public void insertDummyData() {
        insertDummyRooms();
        insertDummyServices();
        insertDummyAttractions();
        insertDummyPromotions();
    }

    private void insertDummyRooms() {
        for (int i = 1; i <= 5; i++) {
            addRoom(
                    "Room " + i,
                    "Deluxe",
                    "This is the description of Room " + i,
                    100 + i * 20,
                    2 + (i % 3),
                    "room" + i + ".jpg"
            );
        }
        for (int i = 6; i <= 10; i++) {
            addRoom(
                    "Room " + i,
                    "Suite",
                    "This is the description of Room " + i,
                    100 + i * 20,
                    2 + (i % 3),
                    "room" + i + ".jpg"
            );
        }
        for (int i = 11; i <= 15; i++) {
            addRoom(
                    "Room " + i,
                    "Standard",
                    "This is the description of Room " + i,
                    100 + i * 20,
                    2 + (i % 3),
                    "room" + i + ".jpg"
            );
        }
    }

    private void insertDummyServices() {
        for (int i = 1; i <= 10; i++) {
            addService(
                    "Service " + i,
                    "Description of Service " + i,
                    30 + i * 10,
                    50 + i * 15,
                    "service" + i + ".jpg"
            );
        }
    }

    private void insertDummyAttractions() {
        SQLiteDatabase db = getWritableDatabase();
        for (int i = 1; i <= 10; i++) {
            ContentValues cv = new ContentValues();
            cv.put("title", "Attraction " + i);
            cv.put("description", "Description of Attraction " + i);
            cv.put("distance", (i * 2) + " km");
            cv.put("contact", "contact" + i + "@example.com");
            db.insert("attractions", null, cv);
        }
    }

    private void insertDummyPromotions() {
        SQLiteDatabase db = getWritableDatabase();
        String today = currentTime().split(" ")[0];
        for (int i = 1; i <= 10; i++) {
            ContentValues cv = new ContentValues();
            cv.put("title", "Promotion " + i);
            cv.put("description", "Description of Promotion " + i);
            cv.put("start_date", today);
            cv.put("end_date", today);
            cv.put("active", 1);
            db.insert("promotions", null, cv);
        }
    }

}
