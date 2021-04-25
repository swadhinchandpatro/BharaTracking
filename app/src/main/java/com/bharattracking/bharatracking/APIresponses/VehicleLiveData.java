package com.bharattracking.bharatracking.APIresponses;

import com.bharattracking.bharatracking.Constants;
import com.google.gson.annotations.SerializedName;

public class VehicleLiveData {

    @SerializedName(Constants.KEY_UNIT_ID)
    public String unitid;

    @SerializedName(Constants.KEY_VEHICLE_NO)
    public String vehicleno;

    @SerializedName(Constants.KEY_LAST_UPDATED_TIME)
    public String dt;

    @SerializedName(Constants.KEY_IGNITION)
    public String ign;

    @SerializedName(Constants.KEY_THUMB_URL)
    public String vtype;

    @SerializedName(Constants.KEY_LOCATION_INFO)
    public String location;

    @SerializedName(Constants.KEY_LAT)
    public String lat;

    @SerializedName(Constants.KEY_LNG)
    public String lng;

    @SerializedName(Constants.KEY_DIR)
    public String dir;

    @SerializedName(Constants.KEY_ODOM)
    public String odom;

    @SerializedName(Constants.KEY_IGN_DURATION)
    public String igntime;

    @SerializedName(Constants.KEY_CATEGORY)
    public String category;

    @SerializedName(Constants.KEY_SPEED_INFO)
    public String speed;

    @SerializedName(Constants.KEY_GPS_SIGNAL_STRENGTH)
    public String signal;

}
