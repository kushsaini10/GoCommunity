package com.example.kush.gocommunity;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by saini on 05-Aug-16.
 */
public class Location {
    private double latitude;
    private double longitude;
    private String numLikes;
    private String numDislikes;
    private String key;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getNumDislikes() {

        return numDislikes;
    }

    public void setNumDislikes(String numDislikes) {
        this.numDislikes = numDislikes;
    }

    public String getNumLikes() {
        return numLikes;
    }

    public void setNumLikes(String  numLikes) {
        this.numLikes = numLikes;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Location(double latitude, double longitude, String numLikes, String numDislikes, String key) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.numLikes = numLikes;
        this.numDislikes = numDislikes;
        this.key = key;
    }
}
