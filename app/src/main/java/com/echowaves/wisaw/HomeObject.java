package com.echowaves.wisaw;

/**
 * Created by dmitry on 10/25/17.
 */

public class HomeObject {
    private int image;
    private String name;
    private double price;
    private String description;
    public HomeObject(int image, String name, double price, String description) {
        this.image = image;
        this.name = name;
        this.price = price;
        this.description = description;
    }
    public int getImage() {
        return image;
    }
    public String getName() {
        return name;
    }
    public double getPrice() {
        return price;
    }
    public String getDescription() {
        return description;
    }
}
