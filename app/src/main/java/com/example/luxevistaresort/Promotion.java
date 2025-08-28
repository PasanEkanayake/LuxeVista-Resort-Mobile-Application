package com.example.luxevistaresort;

public class Promotion {
    private int id;
    private String title;
    private String description;
    private String startDate;
    private String endDate;
    private int active;
    private String images;

    public Promotion(int id, String title, String description, String startDate, String endDate, int active, String images) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.active = active;
        this.images = images;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public int getActive() { return active; }
    public String getImages() { return images; }
}
