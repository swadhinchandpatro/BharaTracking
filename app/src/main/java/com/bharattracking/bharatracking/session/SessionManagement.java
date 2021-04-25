package com.bharattracking.bharatracking.session;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import com.bharattracking.bharatracking.APIresponses.UserInfo;
import com.bharattracking.bharatracking.APIresponses.VehicleLiveData;
import com.bharattracking.bharatracking.Constants;
import com.bharattracking.bharatracking.activities.MainActivity;
import com.bharattracking.bharatracking.interfaces.APIInterface;
import com.bharattracking.bharatracking.utilities.APIClient;
import com.bharattracking.bharatracking.utilities.AlertDialogManager;
import com.bharattracking.bharatracking.utilities.Utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by swadhin on 6/2/18.
 */

public class SessionManagement {

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    private Context _context;
    AlertDialogManager alert = new AlertDialogManager();

    private int PRIVATE_MODE = 0;
    private static final String PREF_NAME = "BtrackLoginPref";
    private static final String IS_LOGIN = "IsLoggedIn";
    public static final String KEY_MOBILE_NUMBER = "mobileno";
    public static final String KEY_USERTYPE = "usertype";
    public static final String KEY_USERNAME = "username";
    private static final String KEY_PASS = "password";
    public static final String KEY_CREATION_DATE = "creationdt";
    public static final String KEY_FIREBASE_TOKEN = "firebasetoken";
    private static final String SHA512_SALT = "BharatTracking";
    //constructor
    public SessionManagement(Context context) {
        _context = context;
        pref = _context.getSharedPreferences(PREF_NAME,PRIVATE_MODE);
        editor = pref.edit();
    }
    //SHA-512 hash

    private String get_SHA_512_SecurePassword(String passwordToHash, String salt){
        String generatedPassword = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(salt.getBytes(StandardCharsets.UTF_8));
            byte[] bytes = md.digest(passwordToHash.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for(int i=0; i< bytes.length ;i++){
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            generatedPassword = sb.toString();
        }
        catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        return generatedPassword;
    }
    //create login session when user logs in first time
    public void createLoginSession(String mobileno,String password,String username, String usertype) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        //change the timezone
        calendar.setTimeZone(TimeZone.getTimeZone("Asia/Calcutta"));
        Date date = calendar.getTime();
        //get encrypted password
//        String pass = get_SHA_512_SecurePassword(password,SHA512_SALT);
        editor.putBoolean(IS_LOGIN,true);
        editor.putString(KEY_MOBILE_NUMBER,mobileno);
        editor.putString(KEY_USERTYPE,usertype);
        editor.putString(KEY_PASS,password);
        editor.putString(KEY_USERNAME,username);
        editor.putString(KEY_CREATION_DATE,dateFormat.format(date));
        editor.commit();
    }
    //check if user is logged in
    public boolean checkLogin(final Activity activity){
        if (this.isLoggedIn()){
            APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
            Call<UserInfo> loginResult = apiInterface.getLoginResult(getMobileNumber(),pref.getString(KEY_PASS,"1234"));

            loginResult.enqueue(new Callback<UserInfo> () {
                @Override
                public void onResponse(Call<UserInfo> call, Response<UserInfo> response) {
                    UserInfo result = response.body();
                    if (response.isSuccessful() && result.isSuccessful.equals("false")){
                        logoutUser(activity);
                    }
                }

                @Override
                public void onFailure(Call<UserInfo> call, Throwable t) {

                }
            });
        }
        return this.isLoggedIn();
    }

    public String getMobileNumber(){
        return pref.getString(KEY_MOBILE_NUMBER, null);
    }
    //get User Details
    public HashMap<String, String> getUserDetails(){
        HashMap<String, String> user = new HashMap<String, String>();
        // user mobileno
        user.put(KEY_MOBILE_NUMBER, pref.getString(KEY_MOBILE_NUMBER, null));

        // user password
        user.put(KEY_PASS, pref.getString(KEY_PASS, null));

        //get creation date
        user.put(KEY_CREATION_DATE,pref.getString(KEY_CREATION_DATE,null));
        //get fcm token
        user.put(KEY_FIREBASE_TOKEN,getFirebaseToken());
        // return user
        return user;
    }

    public String getUserName() {
        return pref.getString(KEY_USERNAME,pref.getString(KEY_MOBILE_NUMBER,null));
    }

    //logout user clear session
    public void logoutUser(Activity activity) {
        clearAllCache();
        // After logout redirect user to LoginActivity
        Intent intent = new Intent(_context, MainActivity.class);
        // Closing all the Activities
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add new Flag to start new Activity
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constants.LOGGED_OUT,true);

        // Staring Login Activity
        activity.startActivity(intent);
        activity.finish();
    }

    public void clearAllCache() {
        // Clearing all data from Shared Preferences
        editor.remove(IS_LOGIN)
                .remove(KEY_MOBILE_NUMBER)
                .remove(KEY_CREATION_DATE)
                .remove(KEY_FIREBASE_TOKEN)
                .remove(KEY_USERTYPE)
                .remove(KEY_PASS);
        editor.commit();
    }

    //Set Firebase token
    public void setFirebaseToken(String token){
        editor.putString(KEY_FIREBASE_TOKEN,token);
        editor.commit();
    }
    //Get the Firebase token
    public String getFirebaseToken(){
        return pref.getString(KEY_FIREBASE_TOKEN,null);
    }
    // Get Login State
    public boolean isLoggedIn(){
        return pref.getBoolean(IS_LOGIN, false);
    }
}
