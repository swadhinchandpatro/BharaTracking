package com.bharattracking.bharatracking.APIresponses;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class RouteWithStops {

    @SerializedName("stops")
    public ArrayList<VehicleStop> vehicleStops;

    @SerializedName("points")
    public ArrayList<LocationPoints> locationPoints;

    public class LocationPoints {

        @SerializedName("lat")
        public String lat;

        @SerializedName("lng")
        public String lng;

        @Nullable
        @SerializedName("dt")
        public String dt;

        @SerializedName("trip_distance")
        public String trip_distance;


    }

    public class VehicleStop {

        @SerializedName("dt")
        public String dt;

        @SerializedName("end_dt")
        public String end_dt;

        @SerializedName("lat")
        public String lat;

        @SerializedName("lng")
        public String lng;

        @SerializedName("trip_distance")
        public String trip_distance;

        @SerializedName("location")
        public String location;

        @SerializedName("duration")
        public String duration;
    }

}
