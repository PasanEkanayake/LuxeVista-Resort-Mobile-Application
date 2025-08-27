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

    public void insertDummyRooms() {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        // Room 1 - Suite
        cv.put("name", "Ocean View Suite");
        cv.put("room_type", "Suite");
        cv.put("description", "Spacious suite with panoramic ocean views and luxury amenities.");
        cv.put("price_per_night", 12000);
        cv.put("capacity", 4);
        cv.put("images", "suite1.jpg,suite2.jpg");
        cv.put("available", 1);
        db.insert("rooms", null, cv);
        cv.clear();

        // Room 2 - Deluxe
        cv.put("name", "Deluxe King Room");
        cv.put("room_type", "Deluxe");
        cv.put("description", "Elegant room with king-sized bed and modern facilities.");
        cv.put("price_per_night", 8000);
        cv.put("capacity", 2);
        cv.put("images", "deluxe1.jpg,deluxe2.jpg");
        cv.put("available", 1);
        db.insert("rooms", null, cv);
        cv.clear();

        // Room 3 - Standard
        cv.put("name", "Standard Twin Room");
        cv.put("room_type", "Standard");
        cv.put("description", "Comfortable room with twin beds and essential amenities.");
        cv.put("price_per_night", 5000);
        cv.put("capacity", 2);
        cv.put("images", "standard1.jpg,standard2.jpg");
        cv.put("available", 1);
        db.insert("rooms", null, cv);
        cv.clear();

        // Room 4 - Suite
        cv.put("name", "Presidential Suite");
        cv.put("room_type", "Suite");
        cv.put("description", "Luxurious suite with private terrace, Jacuzzi, and exclusive services.");
        cv.put("price_per_night", 20000);
        cv.put("capacity", 4);
        cv.put("images", "suite3.jpg,suite4.jpg");
        cv.put("available", 1);
        db.insert("rooms", null, cv);
        cv.clear();

        // Room 5 - Deluxe
        cv.put("name", "Deluxe Ocean Room");
        cv.put("room_type", "Deluxe");
        cv.put("description", "Modern deluxe room with stunning sea views and premium comfort.");
        cv.put("price_per_night", 9000);
        cv.put("capacity", 2);
        cv.put("images", "deluxe3.jpg,deluxe4.jpg");
        cv.put("available", 1);
        db.insert("rooms", null, cv);
        cv.clear();

        // Room 6 - Standard
        cv.put("name", "Standard Queen Room");
        cv.put("room_type", "Standard");
        cv.put("description", "Cozy room with queen bed, ideal for couples or solo travelers.");
        cv.put("price_per_night", 5500);
        cv.put("capacity", 2);
        cv.put("images", "standard3.jpg,standard4.jpg");
        cv.put("available", 1);
        db.insert("rooms", null, cv);
        cv.clear();
    }

    public void insertDummyServices() {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();

        // SPA
        cv.put("name", "Spa");
        cv.put("description", "Relaxing spa experience with massages and aromatherapy.");
        cv.put("duration_minutes", 60);
        cv.put("price", 1500);
        cv.put("images", "spa1.jpg,spa2.jpg");
        db.insert("services", null, cv);
        cv.clear();

        // Dining
        cv.put("name", "Dining");
        cv.put("description", "Exclusive fine dining experience with gourmet meals.");
        cv.put("duration_minutes", 90);
        cv.put("price", 2000);
        cv.put("images", "dining1.jpg,dining2.jpg");
        db.insert("services", null, cv);
        cv.clear();

        // Cabana
        cv.put("name", "Cabana");
        cv.put("description", "Private cabana rental with ocean view and amenities.");
        cv.put("duration_minutes", 180);
        cv.put("price", 5000);
        cv.put("images", "cabana1.jpg,cabana2.jpg");
        db.insert("services", null, cv);
        cv.clear();

        // Tours
        cv.put("name", "Tours");
        cv.put("description", "Guided tours to explore the surrounding attractions.");
        cv.put("duration_minutes", 120);
        cv.put("price", 2500);
        cv.put("images", "tours1.jpg,tours2.jpg");
        db.insert("services", null, cv);
        cv.clear();
    }

    public void insertDummyAttractions() {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        // Attraction 1
        cv.put("title", "Sunset Beach");
        cv.put("description", "Beautiful beach perfect for watching sunsets and relaxing.");
        cv.put("distance", "2 km from resort");
        cv.put("contact", "+94112223344");
        db.insert("attractions", null, cv);
        cv.clear();

        // Attraction 2
        cv.put("title", "Mountain Trail");
        cv.put("description", "Scenic hiking trail with breathtaking mountain views.");
        cv.put("distance", "5 km from resort");
        cv.put("contact", "+94112225566");
        db.insert("attractions", null, cv);
        cv.clear();

        // Attraction 3
        cv.put("title", "City Museum");
        cv.put("description", "Explore local history and cultural artifacts at the city museum.");
        cv.put("distance", "3 km from resort");
        cv.put("contact", "+94112227788");
        db.insert("attractions", null, cv);
        cv.clear();

        // Attraction 4
        cv.put("title", "Botanical Gardens");
        cv.put("description", "Stroll through beautiful gardens with exotic plants and flowers.");
        cv.put("distance", "4 km from resort");
        cv.put("contact", "+94112229900");
        db.insert("attractions", null, cv);
        cv.clear();

        // Attraction 5
        cv.put("title", "Adventure Park");
        cv.put("description", "Fun-filled adventure park with ziplining and obstacle courses.");
        cv.put("distance", "6 km from resort");
        cv.put("contact", "+94112221122");
        db.insert("attractions", null, cv);
        cv.clear();
    }

    public void insertDummyPromotions() {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        // Promotion 1
        cv.put("title", "Summer Special");
        cv.put("description", "Enjoy 20% off on all suite bookings this summer.");
        cv.put("start_date", "2025-06-01");
        cv.put("end_date", "2025-08-31");
        cv.put("active", 1);
        db.insert("promotions", null, cv);
        cv.clear();

        // Promotion 2
        cv.put("title", "Weekend Getaway");
        cv.put("description", "Book a deluxe room and get free spa access on weekends.");
        cv.put("start_date", "2025-01-01");
        cv.put("end_date", "2025-12-31");
        cv.put("active", 1);
        db.insert("promotions", null, cv);
        cv.clear();

        // Promotion 3
        cv.put("title", "Dining Delight");
        cv.put("description", "Complimentary dinner for two with any room booking over 2 nights.");
        cv.put("start_date", "2025-03-01");
        cv.put("end_date", "2025-05-31");
        cv.put("active", 1);
        db.insert("promotions", null, cv);
        cv.clear();
    }

}
