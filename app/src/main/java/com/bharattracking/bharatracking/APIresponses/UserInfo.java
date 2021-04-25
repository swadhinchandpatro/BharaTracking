package com.bharattracking.bharatracking.APIresponses;

import com.google.gson.annotations.SerializedName;

public class UserInfo {

    @SerializedName("fullname")
    public String fullName;

    @SerializedName("usertype")
    public String usertype;

    @SerializedName("isSuccessful")
    public String isSuccessful;
}
