package com.bharattracking.bharatracking.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.bharattracking.bharatracking.APIresponses.UserInfo;
import com.bharattracking.bharatracking.Constants;
import com.bharattracking.bharatracking.interfaces.APIInterface;
import com.bharattracking.bharatracking.interfaces.LoginManager;
import com.bharattracking.bharatracking.R;
import com.bharattracking.bharatracking.session.SessionManagement;
import com.bharattracking.bharatracking.utilities.APIClient;
import com.bharattracking.bharatracking.utilities.Utils;
import com.bharattracking.bharatracking.fragments.LoginFragment;
import com.bharattracking.bharatracking.utilities.AlertDialogManager;

public class MainActivity extends AppCompatActivity implements LoginManager {
    private static FragmentManager fragmentManager;

    private static boolean REQUEST_SIGNUP = false;

    AlertDialogManager alert = new AlertDialogManager();
    SessionManagement session;
    private String mobileno = "",password = "";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        session = new SessionManagement(getApplicationContext());

        if (getIntent() != null && getIntent().getBooleanExtra(Constants.LOGGED_OUT,false)){
            Utils.removeFirebaseToken(MainActivity.this,session.getMobileNumber(),session.getFirebaseToken());

            MainActivity.this.setContentView(R.layout.activity_main);

            fragmentManager = getSupportFragmentManager();

            // If savedinstnacestate is null then replace login fragment
            fragmentManager
                    .beginTransaction()
                    .replace(R.id.frameContainer, new LoginFragment(),
                            Utils.Login_Fragment).commitAllowingStateLoss();
        }else {
            new CountDownTimer(2000,1000){
                @Override
                public void onTick(long l) {}

                @Override
                public void onFinish() {
                    /**
                     * Call this function whenever you want to check user login
                     * This will redirect user to DashboardActivity is he is already
                     * logged in
                     * */
                    if (session.checkLogin(MainActivity.this)) {
                        //move to dashBoardActivity
                        Intent intent = new Intent(MainActivity.this,DashboardActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                        overridePendingTransition(R.anim.left_enter, R.anim.right_out);
                    } else {
                        MainActivity.this.setContentView(R.layout.activity_main);

                        fragmentManager = getSupportFragmentManager();

                        // If savedinstnacestate is null then replace login fragment
                        fragmentManager
                                .beginTransaction()
                                .replace(R.id.frameContainer, new LoginFragment(),
                                        Utils.Login_Fragment).commitAllowingStateLoss();
                    }

                }
            }.start();
        }
    }

    // Replace Login Fragment with animation
    public void replaceLoginFragment() {
        fragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.left_enter, R.anim.right_out)
                .replace(R.id.frameContainer, new LoginFragment(),
                        Utils.Login_Fragment).commitAllowingStateLoss();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {

        // Find the tag of signup and forgot password fragment
        Fragment SignUp_Fragment = fragmentManager
                .findFragmentByTag(Utils.SignUp_Fragment);
        Fragment ForgotPassword_Fragment = fragmentManager
                .findFragmentByTag(Utils.ForgotPassword_Fragment);

        // Check if both are null or not
        // If both are not null then replace login fragment else do backpressed
        // task

        if (SignUp_Fragment != null)
            replaceLoginFragment();
        else if (ForgotPassword_Fragment != null)
            replaceLoginFragment();
        else {
            AlertDialog.Builder dialog = alert.showAlertDialog(MainActivity.this,"Closing Activity","Do you want to Exit?","fail");
            // Setting EXIT Button
            dialog.setPositiveButton("EXIT", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            dialog.setNegativeButton("CANCEL", null);
            dialog.show();
        }
    }

    @Override
    public void doLogin(final String mobile_no, final String pass) {
        //loginButton.setEnabled(false);
        // Initialize  AsyncLogin() class with email and pass
        mobileno = mobile_no;
        password = pass;
        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<UserInfo> loginResult = apiInterface.getLoginResult(mobile_no,pass);

        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        loginResult.enqueue(new Callback<UserInfo> () {

            @Override
            public void onResponse(Call<UserInfo> call, Response<UserInfo> response) {
                //dismiss progress Dialog
                if (progressDialog.isShowing()){
                    REQUEST_SIGNUP=false;
                    progressDialog.dismiss();
                }

                UserInfo result = response.body();
                //this method will be running on UI thread
                if (!response.isSuccessful() || result == null) {
                    AlertDialog.Builder dialog = alert.showAlertDialog(MainActivity.this, "Connection Problem..", "OOPs! Check your Network Connection", "fail");
                    dialog.setNegativeButton("OK",null);
                    dialog.show();
                } else if (result.isSuccessful.equals("false")){
                    // If username and password does not match display a error message
                    AlertDialog.Builder dialog = alert.showAlertDialog(MainActivity.this, "Login failed..", "Username/Password is incorrect", "fail");
                    dialog.setNegativeButton("OK",null);
                    dialog.show();
                } else {
                /* Here launching another activity when login successful. If you persist login state
                use sharedPreferences of Android. and logout button to clear sharedPreferences.
                 */
                    session.createLoginSession(mobile_no, pass,result.fullName,result.usertype);

                    if (session.isLoggedIn()) {
                         Utils.updateFirebaseToken(MainActivity.this,result.fullName,session.getFirebaseToken());
                        //move to dashboardActivity
                        Intent intent = new Intent(MainActivity.this,DashboardActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                        overridePendingTransition(R.anim.left_enter, R.anim.right_out);
                    }
                }
            }

            @Override
            public void onFailure(Call<UserInfo> call, Throwable t) {
                call.cancel();
                //dismiss progress Dialog
                if (progressDialog.isShowing()){
                    REQUEST_SIGNUP=false;
                    progressDialog.dismiss();
                }
            }
        });
    }

    @Override
    public void doSignUp(String mobile_no, String pass, String emailId, String location, String fullName) {
        AlertDialog.Builder dialog = alert.showAlertDialog(MainActivity.this, "Please Contact Us..", "Call/Email/Whatsapp Us", "fail");
        dialog.setNegativeButton("OK",null);
        dialog.show();
    }

}
