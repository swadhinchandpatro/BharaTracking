package com.bharattracking.bharatracking.APIresponses;

import com.google.gson.annotations.SerializedName;

public class GeneralResponse {
    @SerializedName("message")
    public String message;

    @SerializedName("error")
    public String error;
}
