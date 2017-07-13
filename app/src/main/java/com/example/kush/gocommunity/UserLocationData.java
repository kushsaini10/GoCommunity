package com.example.kush.gocommunity;

/**
 * Created by saini on 08-Sep-16.
 */
public class UserLocationData {
    private String country;
    private String state;

//    public UserLocationData(String country, String state) {
//        this.country = country;
//        this.state = state;
//    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
