package com.example.david.bcoffcampus;

import java.io.Serializable;

/**
 * Created by David on 5/10/16.
 */
public class Property implements Serializable {
    private String title;
    private String address;
    private int cost;
    private int id;
    private String price;
    private String imgURL;
    private double lat;
    private double lon;

    public Property(String title, String address, String imgURL, String price){
        this.title = title;
        this.address = address;
        this.imgURL = imgURL;
        this.price = price;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public int getID() {
        return id;
    }

    public void setID(int id) {
        this.id = id;
    }

    public String getImgURL() {
        return imgURL;
    }

    public void setImgURL(String imgURL) {
        this.imgURL = imgURL;
    }

    @Override
    public String toString() {
        return "" + this.getTitle() + ": " + this.getAddress() + " with URL: " + this.getImgURL();
    }
}
