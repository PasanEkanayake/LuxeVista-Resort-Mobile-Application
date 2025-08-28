package com.example.luxevistaresort;

public class BookingItem {
    private String title;
    private String date;
    private String time;
    private String status;

    public BookingItem(String title, String date, String time, String status) {
        this.title = title;
        this.date = date;
        this.time = time;
        this.status = status;
    }

    public String getTitle() { return title; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getStatus() { return status; }
}
