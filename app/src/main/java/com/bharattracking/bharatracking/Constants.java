package com.bharattracking.bharatracking;

public class Constants {
    // XML node keys
    public static final String KEY_UNIT_ID = "unitid";
    public static final String KEY_VEHICLE_NO = "vehicleno";
    public static final String KEY_LOCATION_INFO = "location";
    public static final String KEY_SPEED_INFO = "speed";
    public static final String KEY_IGNITION = "ign";
    public static final String KEY_LAST_UPDATED_TIME = "dt";
    public static final String KEY_THUMB_URL = "vtype";
    public static final String KEY_CATEGORY = "category";
    public static final String KEY_LAT = "lat";
    public static final String KEY_LNG = "lng";
    public static final String KEY_DIR = "dir";
    public static final String KEY_ODOM = "odom";
    public static final String KEY_GPS_SIGNAL_STRENGTH = "signal";

    public static final String KEY_IMAGE_URL = "image_url";
    public static final String KEY_ALERT_MSG = "notification_msg";
    public static final String KEY_IS_READ = "seen";
    public static final String TRACK_LIVE = "liveTrack";

    //    public static final String KEY_GPS_FIX = "gpsfix";
    public static final String KEY_IGN_DURATION = "igntime";
    public static final String KEY_DURATION = "duration";
    public static final String KEY_START_TIME = "starttime";
    public static final String KEY_END_TIME = "endtime";
    // tags used to attach the fragments
    public static final String TAG_DASHBOARD = "home";
    public static final String TAG_STOPPAGE_REPORT = "stopreport";
    public static final String TAG_STOP_LIST = "stopListItem";
    public static final String TAG_RUN_LIST = "runListItem";

    //categories
    public static final String CATEGORY_RUNNING = "running";
    public static final String CATEGORY_STOPPED = "stopped";
    public static final String CATEGORY_DORMANT = "dormant";
    public static final String CATEGORY_NWORKING = "nworking";
    //firebase remote config
    public static final String KEY_VERSION_CODE = "android_latest_version_code";
    public static final String KEY_VERSION_NAME = "android_latest_version_name";
    public static final String LOGGED_OUT = "logged_out";
    public static final int SESSION_TIMEOUT_DELAY = 5000;
    public static final int SIGNAL_LEVELS = 4;
}
