package com.example.luxevistaresort;

public class Room {
    private int id;
    private String name;
    private String type;
    private String description;
    private double price;
    private int capacity;
    private String images;

    public Room(int id, String name, String type, String description, double price, int capacity, String images) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.description = description;
        this.price = price;
        this.capacity = capacity;
        this.images = images;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public int getCapacity() { return capacity; }
    public String getImages() { return images; }
}
