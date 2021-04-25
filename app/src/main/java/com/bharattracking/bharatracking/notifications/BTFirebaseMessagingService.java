package com.bharattracking.bharatracking.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.bharattracking.bharatracking.R;
import com.bharattracking.bharatracking.session.SessionManagement;
import com.bharattracking.bharatracking.utilities.Utils;
import com.bharattracking.bharatracking.activities.DashboardActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.TimeZone;

public class BTFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = BTFirebaseMessagingService.class.getSimpleName();
    NotificationManager notificationManager;
    SessionManagement session;
    private NotificationUtils notificationUtils;

    @Override
    public void onNewToken(String refreshedToken) {
        super.onNewToken(refreshedToken);
        Log.d(TAG, "Refreshed token: " + refreshedToken);
        session = new SessionManagement(getApplicationContext());
        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        session.setFirebaseToken(refreshedToken);

        // Notify UI that registration has completed, so the progress indicator can be hidden.
        Intent registrationComplete = new Intent(NotificationConfig.REGISTRATION_COMPLETE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        session = new SessionManagement(getApplicationContext());
        notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //Setting up Notification channels for android O and above
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            setupChannels();
        }

        Log.e(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.e(TAG, "Notification Body: " + remoteMessage.getNotification().getBody());
            handleNotification(remoteMessage.getNotification().getBody(),remoteMessage.getNotification().getTitle());
        }

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.e(TAG, "Data Payload: " + remoteMessage.getData().toString());

            try {
                JSONObject json = new JSONObject(remoteMessage.getData().toString());
                handleDataMessage(json);
            } catch (Exception e) {
                Log.e(TAG, "Exception: " + e.getMessage());
            }
        }
    }

    private void handleNotification(String message,String title) {
        if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
            // app is in foreground, broadcast the push message
            Intent pushNotification = new Intent(NotificationConfig.PUSH_NOTIFICATION);
            pushNotification.putExtra("message", message);
            pushNotification.putExtra("title",title);
            LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

            // play notification sound
            NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
            notificationUtils.playNotificationSound();
        }else{
            //firebase handles it
        }
    }

    private void handleDataMessage(JSONObject json) {
        Log.e(TAG, "push json: " + json.toString());

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        //change the timezone
        calendar.setTimeZone(TimeZone.getTimeZone("Asia/Calcutta"));

        try {
            JSONObject data = json.getJSONObject("data");

            String title = data.getString("message");
            String message = data.getString("location");
            String imageUrl = data.has("bigImageUrl")?data.getString("bigImageUrl"):"";

            if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
                // app is in foreground, broadcast the push message
                Intent pushNotification = new Intent(NotificationConfig.PUSH_NOTIFICATION);
                pushNotification.putExtra("message", message);
                pushNotification.putExtra("title",title);
                LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

                // play notification sound
                NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
                notificationUtils.playNotificationSound();
            } else {
                // app is in background, show the notification in notification tray
                Intent resultIntent = new Intent(getApplicationContext(), DashboardActivity.class);
                resultIntent.putExtra("message", message);
                resultIntent.putExtra("title",title);

                // check for image attachment
                if (TextUtils.isEmpty(imageUrl)) {
                    showNotificationMessage(getApplicationContext(), title, message, calendar.getTimeInMillis(), resultIntent);
                } else {
                    // image is present, show notification with image
                    showNotificationMessageWithBigImage(getApplicationContext(), title, message, calendar.getTimeInMillis(), resultIntent, imageUrl);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Json Exception: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

    /**
     * Showing notification with text only
     */
    private void showNotificationMessage(Context context, String title, String message, long timeStamp, Intent intent) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent);
    }

    /**
     * Showing notification with text and image
     */
    private void showNotificationMessageWithBigImage(Context context, String title, String message, long timeStamp, Intent intent, String imageUrl) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent, imageUrl);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupChannels(){
        CharSequence adminChannelName = getString(R.string.notifications_admin_channel_name);
        String adminChannelDescription = getString(R.string.notifications_admin_channel_description);

        NotificationChannel adminChannel;
        adminChannel = new NotificationChannel(Utils.ADMIN_CHANNEL_ID, adminChannelName, NotificationManager.IMPORTANCE_HIGH);
        adminChannel.setDescription(adminChannelDescription);
        adminChannel.enableLights(true);
        adminChannel.setLightColor(Color.RED);
        adminChannel.enableVibration(true);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(adminChannel);
        }
    }
}
