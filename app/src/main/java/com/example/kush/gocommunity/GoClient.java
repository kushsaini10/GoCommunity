package com.example.kush.gocommunity;

import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by saini on 05-Sep-16.
 */
public class GoClient{

    private static GCAPIInterface service;

    public static GCAPIInterface getService(){

        if(service == null) {
            Retrofit r = new Retrofit.Builder().baseUrl("https://maps.googleapis.com").
                    addConverterFactory(GsonConverterFactory.create(
                            new GsonBuilder().create()))
                    .build();
            service = r.create(GCAPIInterface.class);
        }
        return service;
    }
}