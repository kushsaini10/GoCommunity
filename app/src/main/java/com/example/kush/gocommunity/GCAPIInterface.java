package com.example.kush.gocommunity;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by saini on 05-Sep-16.
 */
public interface GCAPIInterface {
    //Todo Add Google Geocoding API_KEY
    @GET("/maps/api/geocode/json?result_type=administrative_area_level_1&key=API_KEY")
    Call<GCResponse> getAddress(@Query("latlng") String latlng);
}