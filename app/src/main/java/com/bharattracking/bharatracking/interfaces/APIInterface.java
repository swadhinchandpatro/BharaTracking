package com.bharattracking.bharatracking.interfaces;

import com.bharattracking.bharatracking.APIRequests.VehicleData;
import com.bharattracking.bharatracking.APIresponses.GeneralResponse;
import com.bharattracking.bharatracking.APIresponses.RouteWithStops;
import com.bharattracking.bharatracking.APIresponses.UserInfo;
import com.bharattracking.bharatracking.APIresponses.VehicleLiveData;
import com.bharattracking.bharatracking.APIresponses.VehicleReportDetails;
import com.bharattracking.bharatracking.notifications.AlertData;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface APIInterface {

    @FormUrlEncoded
    @POST("/login.php")
    Call<UserInfo> getLoginResult(@Field("mobileno") String mobileno, @Field("pass") String pass);

    @FormUrlEncoded
    @POST("/reports/route/getRoute1.php")
    Call<RouteWithStops> getRouteWithStops(@Field("unitid") String unitid, @Field("start") String start, @Field("end") String end);

    @FormUrlEncoded
    @POST("/login/html/fixed-menu/dataRequest.php")
    Call<ArrayList<VehicleLiveData>> getLiveData(@Field("username") String user, @Field("token") String token);

    @GET("apis/livetrack.php")
    Call<ArrayList<VehicleLiveData>> getLiveRouteData(@Body ArrayList<VehicleData> vehicles);

    @FormUrlEncoded
    @POST("pushnotify/getAlertDetails1.php")
    Call<ArrayList<AlertData>> getAlertData(@Field("username") String user, @Field("start") String start, @Field("end") String end);

    @FormUrlEncoded
    @POST("/reports/getVehicleStopReport1.php")
    Call<VehicleReportDetails> getVehicleStopReport(@Field("unitid") String unitid, @Field("start") String start, @Field("end") String end);

    @FormUrlEncoded
    @POST("/reports/getVehicleStopMarks.php")
    Call<ArrayList<RouteWithStops.VehicleStop>> getVehicleStopMarks(@Field("unitid") String unitid, @Field("start") String start, @Field("end") String end);

    @FormUrlEncoded
    @POST("/reports/getStopReport.php")
    Call<ArrayList<VehicleReportDetails>> getAllStopsReport(@Field("username") String username, @Field("start") String start, @Field("end") String end);

    @FormUrlEncoded
    @POST("/reports/getTravelReport.php")
    Call<ArrayList<VehicleReportDetails>> getTravelReport(@Field("username") String username, @Field("start") String start, @Field("end") String end);

    @GET("/pushnotify/removeFirebaseToken.php")
    Call<String> removeFirebaseToken(@Query("mobileno") String mobileNumber,@Query("fcmtoken") String fcmToken);

    @FormUrlEncoded
    @POST("/pushnotify/update_fcm_token.php")
    Call<String> updateFirebaseToken(@Field("username") String mobileNumber, @Field("fcmtoken") String fcmToken);

    @GET("/reports/driverDetail.php")
    Call<String> getDriverNumber(@Query("vehicleno") String vehicleNo);

    @FormUrlEncoded
    @POST("/reports/driverDetail.php")
    Call<String> updateDriverNumber(@Field("vehicleno") String vehicleNo,@Field("mobileno") String mobileno);

    @FormUrlEncoded
    @POST("/apis/storeShareLink.php")
    Call<GeneralResponse> storeLiveLocationLink(@Field("vehicleno") String selectedVehicleNo,@Field("token") String randomCode);
}
