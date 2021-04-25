package com.bharattracking.bharatracking.interfaces;

import android.widget.Button;
import android.widget.EditText;

import java.util.List;

/**
 * Created by swadhin on 9/2/18.
 */

public interface LoginManager {
    //connect to database and verify user credential
    void doLogin(String getMobileNumber,String getPassword);
    //connect to database and add new user credential
    void doSignUp(String getMobileNumber, String getPassword, String getEmailId, String getLocation, String getFullName);
}
