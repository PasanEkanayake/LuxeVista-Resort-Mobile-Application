package com.example.luxevistaresort;

public class BookingItem {
    public String title;
    public String date;
    public String time; // can be null
    public String status;

    public BookingItem(String title, String date, String time, String status) {
        this.title = title;
        this.date = date;
        this.time = time;
        this.status = status;
    }
}
