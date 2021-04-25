package com.bharattracking.bharatracking.utilities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.bharattracking.bharatracking.APIresponses.GeneralResponse;
import com.bharattracking.bharatracking.BuildConfig;
import com.bharattracking.bharatracking.Constants;
import com.bharattracking.bharatracking.CustomToast;
import com.bharattracking.bharatracking.DialogWithInput;
import com.bharattracking.bharatracking.R;
import com.bharattracking.bharatracking.activities.MainActivity;
import com.bharattracking.bharatracking.activities.SettingsActivity;
import com.bharattracking.bharatracking.interfaces.APIInterface;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by swadhin on 9/2/18.
 */

public class Utils {
    private static final String TAG = Utils.class.getSimpleName();
    //Email Validation pattern
    public static final String regExEmail = "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}\\b";

    public static final String regExMobile = "^((\\+|00)(\\d{1,3})[\\s-]?)?(\\d{10})$";

    public static final String regNumber = "[0-9]+";
    //Fragments Tags
    public static final String Login_Fragment = "LoginFragment";
    public static final String SignUp_Fragment = "SignUp_Fragment";
    public static final String ForgotPassword_Fragment = "ForgotPassword_Fragment";
    public static final String STOP_LIST_FRAGMENT = "StopList_Fragment";
    public static final String RUN_LIST_FRAGMENT = "RunList_Fragment";
    public static final String STOPPAGE_REPORT_FRAGMENT = "STOPPAGE_REPORT_FRAGMENT";
    public static final String USERTYPE_CUSTOMER = "customer";
    public static final int CONNECTION_TIMEOUT=180000;
    public static final int READ_TIMEOUT=180000;
    public static final int WRITE_TIMEOUT = 180000;
    public static final String DATA_REQUEST_TOKEN = "1234";
    public static final String DATA_ROUTE_REQUEST_TOKEN = "4321";
    public static final String USERTYPE_ADMIN = "Admin";

    public static final String ADMIN_CHANNEL_ID = "alertInfo";
    /**
     * The number of points allowed per API request. This is a fixed value.
     */
    public static final int PAGE_SIZE_LIMIT = 100;
    /**
     * Define the number of data points to re-send at the start of subsequent requests. This helps
     * to influence the API with prior data, so that paths can be inferred across multiple requests.
     * You should experiment with this value for your use-case.
     */
    public static final int PAGINATION_OVERLAP = 5;
    private static HashMap<String, Object> firebaseDefaultMap;

    static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public static final int vehicleIcon[] = {R.drawable.ic_car_red,R.drawable.ic_truck_green,R.drawable.ic_bike_red};

    public static void CopyStream(InputStream is, OutputStream os)
    {
        final int buffer_size=1024;
        try
        {
            byte[] bytes=new byte[buffer_size];
            for(;;)
            {
                int count=is.read(bytes, 0, buffer_size);
                if(count==-1)
                    break;
                os.write(bytes, 0, count);
            }
        }
        catch(Exception ex){}
    }

    public static long getTimeDifferenceFromNow(String startDateString, TimeUnit unit) throws ParseException {
        Date startDate,endDate;
        if(startDateString == null || startDateString.isEmpty() || startDateString.equals("null")) return 0;
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Calcutta"));
        String currentDate = dateFormat.format(calendar.getTime());

        startDate = dateFormat.parse(startDateString);
        endDate = dateFormat.parse(currentDate);

        return unit.convert(endDate.getTime() - startDate.getTime(),TimeUnit.MILLISECONDS);
    }

    public static long getTimeDifference(String startDateString,String endDateString) throws ParseException {
        Date startDate,endDate;
        if(endDateString==null || endDateString.isEmpty() || endDateString.equals("null")) return 0;
        if(startDateString==null || startDateString.isEmpty() || startDateString.equals("null")) return 0;

        endDate = dateFormat.parse(endDateString);
        startDate = dateFormat.parse(startDateString);

        return endDate.getTime() - startDate.getTime();
    }

    public static Bitmap writeTextOnDrawable(Context mContext, int drawableId, String text,int color) {

        Bitmap bm = BitmapFactory.decodeResource(mContext.getResources(), drawableId)
                .copy(Bitmap.Config.ARGB_8888, true);

        Typeface tf = Typeface.create("Helvetica", Typeface.BOLD);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(mContext.getResources().getColor(color));
        paint.setStrokeWidth(2);
        paint.setTypeface(tf);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(convertToPixels(mContext, 13));

        Rect textRect = new Rect();
        paint.getTextBounds(text, 0, text.length(), textRect);

        Canvas canvas = new Canvas(bm);

        //If the text is bigger than the canvas , reduce the font size
        if(textRect.width() >= (canvas.getWidth() - 4))     //the padding on either sides is considered as 4, so as to appropriately fit in the text
            paint.setTextSize(convertToPixels(mContext, 7));        //Scaling needs to be used for different dpi's

        //Calculate the positions
        int xPos = (canvas.getWidth() / 2) - 2;     //-2 is for regulating the x position offset

        //"- ((paint.descent() + paint.ascent()) / 2)" is the distance from the baseline to the center.
        int yPos = (int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2)) ;

        canvas.drawText(text, xPos, yPos, paint);

        return  bm;
    }

    private static int convertToPixels(Context context, int nDP) {
        final float conversionScale = context.getResources().getDisplayMetrics().density;

        return (int) ((nDP * conversionScale) + 0.5f) ;

    }

    public static void updateFirebaseToken(final Context _context , String username, String firebaseToken) {
        final AlertDialogManager alertDialogManager = new AlertDialogManager();
        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<String> updateFBTokenResult = apiInterface.updateFirebaseToken(username,firebaseToken);

        updateFBTokenResult.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                String result = response.body();
                if (result != null && response.isSuccessful()){
                    if (result.equalsIgnoreCase("device_overflow")) {
                        Toast.makeText(_context,"Please logout from any other Devices !!",Toast.LENGTH_LONG).show();
                    }
                }else {
                    call.cancel();
                    Toast.makeText(_context,"OOPs! Check your Network Connection !!",Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                call.cancel();
                Toast.makeText(_context,"OOPs! Check your Network Connection !!",Toast.LENGTH_LONG).show();
            }
        });
    }

    public static void initializeFirebase(final Context mContext) {
        if (FirebaseApp.getApps(mContext).isEmpty()) {
            FirebaseApp.initializeApp(mContext, FirebaseOptions.fromResource(mContext));
        }
        final FirebaseRemoteConfig config = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(6000)
                .build();
        config.setConfigSettingsAsync(configSettings);
        firebaseDefaultMap = new HashMap<>();
        firebaseDefaultMap.put(Constants.KEY_VERSION_CODE, getCurrentVersionCode(mContext));
        config.setDefaults(firebaseDefaultMap);

        config.fetchAndActivate()
                .addOnCompleteListener((Activity) mContext, new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if (task.isSuccessful()) {
                            boolean updated = task.getResult();
                            Log.d(TAG, "Config params updated: " + updated);
                            Toast.makeText(mContext, "Fetch and activate succeeded",
                                    Toast.LENGTH_SHORT).show();
                            checkForAppUpdate(config,mContext);
                        } else {
                            Toast.makeText(mContext, "Fetch failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private static int getCurrentVersionCode(Context mContext) {
        PackageInfo pinfo = null;
        try {
            pinfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            return pinfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return 1;
        }
    }

    private static void checkForAppUpdate(FirebaseRemoteConfig config, final Context mContext) {
        int latestAppVersionCode = (int) config.getLong(Constants.KEY_VERSION_CODE);
        if (latestAppVersionCode > getCurrentVersionCode(mContext)) {
            AlertDialogManager alert = new AlertDialogManager();
            AlertDialog.Builder dialog = alert.showAlertDialog(mContext, "Please Update the App", "A new version of this app is available. Please update it", "update");
            dialog.setPositiveButton(
                    "OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final String appPackageName = mContext.getPackageName(); // getPackageName() from Context or Activity object
                            try {
                                mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                } catch (android.content.ActivityNotFoundException anfe) {
                                mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                            }
                        }
                    }).setCancelable(false).show();
        } else {
            Toast.makeText(mContext,"This app is already upto date", Toast.LENGTH_SHORT).show();
        }
    }

    public static void removeFirebaseToken(final Context _context , String mobileNo, String firebaseToken) {
        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        final Call<String> removeFbTokenCall = apiInterface.removeFirebaseToken(mobileNo,firebaseToken);

        removeFbTokenCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() && response.body().contains("success")){
                    Toast.makeText(_context,"You will no longer receive Alerts!", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(_context,"Please try Again!!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(_context,"Please try Again!!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void callDriver(final View view, final FragmentManager fragmentManager, final Activity activity, final String selectedVehicleNo) {
        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        final Call<String> getDriverNumberCall = apiInterface.getDriverNumber(selectedVehicleNo);

        getDriverNumberCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                String result = response.body();
                if (response.isSuccessful() && !result.equals("none")){
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:"+ result));
                    if (callIntent.resolveActivity(activity.getPackageManager())!=null){
                        activity.startActivity(callIntent);
                    }else {
                        Snackbar.make(view, "No app to Support this action", Snackbar.LENGTH_LONG).show();
                    }
                }else if (!response.isSuccessful()){
                    Toast.makeText(activity,"Please Check Network Connection!!", Toast.LENGTH_LONG).show();
                } else {
                    Bundle args = new Bundle();
                    args.putString(Constants.KEY_VEHICLE_NO, selectedVehicleNo);
                    DialogWithInput dialog = new DialogWithInput();
                    dialog.setArguments(args);
                    dialog.showNow(fragmentManager,"Driver Phone");
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(activity,"Please Check Network Connection!!", Toast.LENGTH_LONG).show();
            }
        });
    }

    public static void updateDriverNumber(final String input, String vehicleNo, final View view, final Activity activity) {
        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        final Call<String> updateDriverNumberCall = apiInterface.updateDriverNumber(vehicleNo,input);

        updateDriverNumberCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                String result = response.body();
                if (response.isSuccessful() && result.contains("success")){
                    Snackbar.make(view, "Driver Number Updated!!", Snackbar.LENGTH_SHORT).show();
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:"+ input));
                    if (callIntent.resolveActivity(activity.getPackageManager())!=null){
                        activity.startActivity(callIntent);
                    }else {
                        Snackbar.make(view, "No app to Support this action", Snackbar.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                new CustomToast(activity).Show_Toast(activity,null,"Update Failed!! Try Again");
                call.cancel();
            }
        });
    }

    public static void shareLocation(final View view, FragmentManager supportFragmentManager, final Activity activity, final String shareMessage, final String selectedVehicleNo, final String duration) {
        String expireDateTime = null;
        int timeUnit = Calendar.MINUTE;
        if (duration.contains("h")){
            timeUnit = Calendar.HOUR;
        }else if (duration.contains("d")) {
            timeUnit = Calendar.DATE;
        }
        try {
            expireDateTime = Utils.getResultTimeFromNow(Integer.parseInt(duration.replaceAll("[^0-9]","")),timeUnit);
        } catch (ParseException e) {
            Snackbar.make(view,"Date Parsing Failed to Calculate Expiry DateTime", Snackbar.LENGTH_LONG);
            Log.e(TAG,e.getLocalizedMessage());
        }

        String randomCode = randomAlphaNumeric(6);
        final URL url = new HttpUrl.Builder()
                .scheme("http")
                .host("bharattracking.com")
                .addPathSegment(selectedVehicleNo)
                .addPathSegment("location")
                .addPathSegment(randomCode)
                .build().url();

        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        final Call<GeneralResponse> storeLiveLocationLinkCall = apiInterface.storeLiveLocationLink(selectedVehicleNo,randomCode + "_" + expireDateTime);

        final String finalExpireDateTime = expireDateTime;
        storeLiveLocationLinkCall.enqueue(new Callback<GeneralResponse>() {
            @Override
            public void onResponse(Call<GeneralResponse> call, Response<GeneralResponse> response) {
                GeneralResponse result = response.body();
                if (response.isSuccessful() && result.error == null){
//                    Snackbar.make(view, "Driver Number Updated!!", Snackbar.LENGTH_SHORT).show();
                    Uri imageUri = Uri.parse("android.resource://" + activity.getPackageName()
                            + "/drawable/" + "bt_logo_large");
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Live Location for " + selectedVehicleNo + "\r\n");
                    shareIntent.putExtra(Intent.EXTRA_TEXT,"Visit " + url + " to track your vehicle. Link valid till " + finalExpireDateTime + "\r\n" + shareMessage);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                    shareIntent.setType("image/jpeg");
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    if (shareIntent.resolveActivity(activity.getPackageManager())!=null){
                        activity.startActivity(Intent.createChooser(shareIntent, activity.getResources().getString(R.string.choose_share_using)));
                    }else {
                        Snackbar.make(view, "No app to Support this action", Snackbar.LENGTH_LONG).show();
                    }
                }else {
                    new CustomToast(activity).Show_Toast(activity,null,result.error);
                    call.cancel();
                }
            }

            @Override
            public void onFailure(Call<GeneralResponse> call, Throwable t) {
                new CustomToast(activity).Show_Toast(activity,null,"Link Creation Failed!! Please Try Again");
                call.cancel();
            }
        });


    }

    public static String getResultTimeFrom(String datetime,int duration,int timeUnit) throws ParseException {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Calcutta"));

        Date date = dateFormat.parse(datetime);

        calendar.setTime(date);
        calendar.add(timeUnit,duration);

        return dateFormat.format(calendar.getTime());
    }

    public static String getResultTimeFromNow(int duration,int timeUnit) throws ParseException {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Calcutta"));
        return getResultTimeFrom(dateFormat.format(calendar.getTime()),duration,timeUnit);
    }

    public static String randomAlphaNumeric(int count) {
        final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder builder = new StringBuilder();

        while (count-- != 0) {

            int character = (int)(Math.random()*ALPHA_NUMERIC_STRING.length());

            builder.append(ALPHA_NUMERIC_STRING.charAt(character));

        }

        return builder.toString();

    }

    public static int getPixelFromDp(Context mContext,int dps) {
        final float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) (dps * scale + 0.5f);
    }
}
