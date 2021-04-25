package com.bharattracking.bharatracking.utilities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.view.ContextThemeWrapper;
import android.view.View;

import com.bharattracking.bharatracking.R;

/**
 * Created by swadhin on 6/2/18.
 */

public class AlertDialogManager {
    /**
     * Function to display simple Alert Dialog
     * @param context - application context
     * @param title - alert dialog title
     * @param message - alert message
     * @param type - success/failure (used to set icon)
     *               - pass null if you don't want icon
     * */
    public AlertDialog.Builder showAlertDialog(Context context, String title, String message,
                                String type) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

        // Setting Dialog Title
        alertDialog.setTitle(title);

        // Setting Dialog Message
        alertDialog.setMessage(message);

        // Setting alert dialog icon
        switch (type){
            case "success":
                alertDialog.setIcon(R.drawable.login_success);
                break;
            case "fail":
                alertDialog.setIcon(R.drawable.login_fail);
                break;
            case "update":
                alertDialog.setIcon(R.drawable.ic_cloud_download_primary_24dp);
                break;
            default:
                alertDialog.setIcon(R.drawable.error);
        }
        // return Alert Message
        return alertDialog;
    }
}

// Setting OK Button
//alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
//public void onClick(DialogInterface dialog, int which) {
//        }
//        });