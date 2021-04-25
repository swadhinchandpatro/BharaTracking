package com.bharattracking.bharatracking.APIresponses;

import com.bharattracking.bharatracking.Constants;
import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.Nullable;

public class VehicleReportDetails {

    @Nullable
    @SerializedName(Constants.KEY_UNIT_ID)
    public String unitid;

    @Nullable
    @SerializedName(Constants.KEY_VEHICLE_NO)
    public String vehicleno;

    @Nullable
    @SerializedName(Constants.KEY_THUMB_URL)
    public String vtype;

    @SerializedName(Constants.CATEGORY_RUNNING)
    public String running;

    @SerializedName(Constants.CATEGORY_STOPPED)
    public String stopped;

    @SerializedName(Constants.CATEGORY_DORMANT)
    public String dormant;

    @SerializedName(Constants.CATEGORY_NWORKING)
    public String nworking;

    @Nullable
    @SerializedName("totalstops")
    public String totalstops;

    @Nullable
    @SerializedName("totalalerts")
    public String totalalerts;

    @SerializedName("distance")
    public String distance;

    @SerializedName("avgspeed")
    public String avgspeed;

    @SerializedName("maxspeed")
    public String maxspeed;

}
