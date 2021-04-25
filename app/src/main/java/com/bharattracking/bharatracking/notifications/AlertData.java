package com.bharattracking.bharatracking.notifications;

import com.bharattracking.bharatracking.Constants;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

public class AlertData {

    @SerializedName(Constants.KEY_IMAGE_URL)
    public String iconUrl;

    @SerializedName(Constants.KEY_VEHICLE_NO)
    public String vehicleNo;

    @SerializedName(Constants.KEY_LAST_UPDATED_TIME)
    public String updatedTime;

    @SerializedName(Constants.KEY_LOCATION_INFO)
    public String location;

    @SerializedName(Constants.KEY_ALERT_MSG)
    public String notificationMsg;

    @SerializedName(Constants.KEY_LAT)
    public String lat;

    @SerializedName(Constants.KEY_LNG)
    public String lng;

    @SerializedName(Constants.KEY_DIR)
    public String dir;

    @SerializedName(Constants.KEY_IS_READ)
    public boolean isSeen;

    public LatLng getLatlng() {
        return new LatLng(Double.parseDouble(this.lat),Double.parseDouble(this.lng));
    }
}
