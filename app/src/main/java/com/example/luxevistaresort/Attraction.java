package com.example.luxevistaresort;

public class Attraction {
    private int id;
    private String title;
    private String description;
    private String distance;
    private String contact;
    private String images;

    public Attraction(int id, String title, String description, String distance, String contact, String images) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.distance = distance;
        this.contact = contact;
        this.images = images;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getDistance() { return distance; }
    public String getContact() { return contact; }
    public String getImages() { return images; }
}
