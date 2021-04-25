package com.bharattracking.bharatracking.activities;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bharattracking.bharatracking.Constants;
import com.bharattracking.bharatracking.CustomToast;
import com.bharattracking.bharatracking.DialogWithInput;
import com.bharattracking.bharatracking.R;
import com.bharattracking.bharatracking.session.SessionManagement;
import com.bharattracking.bharatracking.TabWithIconContent;
import com.bharattracking.bharatracking.fragments.CommandFragment;
import com.bharattracking.bharatracking.fragments.StopListFragment;
import com.bharattracking.bharatracking.session.SessionTimeoutManager;
import com.bharattracking.bharatracking.session.TimeOutListner;
import com.bharattracking.bharatracking.utilities.Utils;
import com.bharattracking.bharatracking.fragments.PhotosFragment;
import com.bharattracking.bharatracking.fragments.StopReportFragment;
import com.bharattracking.bharatracking.fragments.TravelReportFragment;
import com.bharattracking.bharatracking.notifications.NotificationConfig;
import com.bharattracking.bharatracking.notifications.NotificationUtils;
import com.bharattracking.bharatracking.utilities.AlertDialogManager;

import java.net.URLEncoder;
import java.util.HashMap;

public class DashboardActivity extends AppCompatActivity implements TimeOutListner, DialogWithInput.InputCollection {


    private static final String TAG = DashboardActivity.class.getSimpleName();
    AlertDialogManager alert = new AlertDialogManager();
    SessionManagement session;

    HashMap<String, String> user;
    // index to identify current nav menu item
    public static int navItemIndex = 0;

    public static String CURRENT_TAG = Constants.TAG_DASHBOARD;

    // toolbar titles respected to selected nav menu item
    private String[] activityTitles;

    // flag to load home fragment when user presses back key
    private boolean shouldLoadHomeFragOnBackPress = true;
    private Handler mHandler;
    private NavigationView navigationView;
    private DrawerLayout drawer;
    private Toolbar toolbar;
    private boolean isDashboardLoaded = true;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private FragmentManager fragmentManager;
    private ActionBarDrawerToggle mDrawerToggler;
    private boolean mToolBarNavigationListenerIsRegistered = false;
    public TabWithIconContent tabWithIconContent;
    private LinearLayout actionLayout;
    private FloatingActionButton fab;
    private ConstraintLayout vehicleCategoryMetric;

    private Animation slideUp,slideDown;
    private View.OnClickListener fabOnClickListner;

    @Override
    public void onSessionTimeOut() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                vehicleCategoryMetric.startAnimation(slideDown);
                if (actionLayout.getVisibility() == View.VISIBLE){
                    actionLayout.setVisibility(View.GONE);
                    final OvershootInterpolator interpolator = new OvershootInterpolator();
                    ViewCompat.animate(fab).
                            rotation(0f).
                            withLayer().
                            setDuration(300).
                            setInterpolator(interpolator).
                            withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    fab.hide();
                                }
                            }).
                            start();
                }else {
                    fab.hide();
                }

            }
        });
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (vehicleCategoryMetric.getVisibility() == View.GONE){
                    vehicleCategoryMetric.startAnimation(slideUp);
                    fab.show();
                }
            }
        });
        ((SessionTimeoutManager) getApplication()).onUserInteracted();
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = new SessionManagement(getApplicationContext());

        if (!session.isLoggedIn()) {
            //move to dashBoardActivity
            Intent intent = new Intent(DashboardActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.left_enter, R.anim.right_out);
        }
        setContentView(R.layout.activity_dashboard);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //check for update
        Utils.initializeFirebase(DashboardActivity.this);

        setUpFabUi();
        vehicleCategoryMetric = findViewById(R.id.status_bar_layout);
        slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        slideDown = AnimationUtils.loadAnimation(this,R.anim.slide_down);
        slideUp.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                vehicleCategoryMetric.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        slideDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                vehicleCategoryMetric.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        // load toolbar titles from string resources
        fragmentManager = getSupportFragmentManager();
        activityTitles = getResources().getStringArray(R.array.nav_item_view_titles);

        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        mHandler = new Handler();
        // initializing navigation menu
        new Thread(new Runnable() {
            @Override
            public void run() {
                setUpNavigationView();
                if (savedInstanceState == null) {
                    setNavItemIndex(0, Constants.TAG_DASHBOARD);
                    loadHomeFragment(null);
                }
            }
        }).start();
        //navigationView.getBackground().setAlpha(130);
        session = new SessionManagement(getApplicationContext());

        TextView userInfo = navigationView.getHeaderView(0).findViewById(R.id.user_mobile);
        // To retrieve userinfo from session
        final String userName = session.getUserName();
        userInfo.setText(userName);

        Button nav_footer = findViewById(R.id.nav_footer);
        nav_footer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Clear the session data
                // This will clear all session data and
                // redirect user to LoginActivity
                AlertDialog.Builder alertDialog;
                alertDialog = alert.showAlertDialog(DashboardActivity.this, "Do you wish to logout ?", "", "info");
                alertDialog.setNegativeButton("CANCEL", null);
                alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //logout user
                        session.logoutUser(DashboardActivity.this);
                    }
                }).show();
                drawer.closeDrawer(Gravity.START);
            }
        });

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(NotificationConfig.REGISTRATION_COMPLETE)) {
                    if (session.isLoggedIn()) {
                        user = session.getUserDetails();
                        String mobileNo = user.get(SessionManagement.KEY_MOBILE_NUMBER);
                        String token = user.get(SessionManagement.KEY_FIREBASE_TOKEN);
                        Utils.updateFirebaseToken(DashboardActivity.this, mobileNo, token);
                    }
                } else if (intent.getAction().equals(NotificationConfig.PUSH_NOTIFICATION)) {
                    String msg = intent.getStringExtra("message");
                    String title = intent.getStringExtra("title");
                    Log.d(TAG, "BroadCast from BTFirebaseService :" + msg);
                    new CustomToast(DashboardActivity.this).Show_Toast(DashboardActivity.this, null, title + "\n" + msg);
                }
            }
        };

        //start user timeout session
        SessionTimeoutManager manager = ((SessionTimeoutManager) getApplicationContext());
        manager.registerUserSessionListner(this);
        manager.startUserSession();
    }

    public View getVehicleCategoryMetric(){
        return this.vehicleCategoryMetric;
    }

    private void setUpFabUi() {
        final FloatingActionButton fabWhatsapp, fabEmail;
        final Animation downToTop = AnimationUtils.loadAnimation(DashboardActivity.this, R.anim.down_to_top);

        downToTop.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                actionLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        fabWhatsapp = findViewById(R.id.fabwsp);
        fabEmail = findViewById(R.id.fabEmail);
        fab = findViewById(R.id.fab);

        actionLayout = findViewById(R.id.action_layout);

        fabOnClickListner = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final OvershootInterpolator interpolator = new OvershootInterpolator();
                ViewPropertyAnimatorCompat animateFab = ViewCompat.animate(fab).
                        withLayer().
                        setDuration(300).
                        setInterpolator(interpolator);
                if (actionLayout.getVisibility() == View.VISIBLE) {
                    animateFab.rotation(0f).start();
                    actionLayout.setVisibility(View.GONE);
                } else {
                    animateFab.rotation(45f).start();
                    actionLayout.startAnimation(downToTop);
                }
            }
        };
        fab.setOnClickListener(fabOnClickListner);

        fabWhatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent wspIntent = new Intent(Intent.ACTION_VIEW);
                try {
                    String url = "https://api.whatsapp.com/send?phone=" + getResources().getString(R.string.help_desk_phone)
                            + "&text=" + URLEncoder.encode("hi " + session.getUserName() + " here." + getResources().getString(R.string.default_wspmsg), "UTF-8");
                    wspIntent.setPackage("com.whatsapp");
                    wspIntent.setData(Uri.parse(url));
                    if (wspIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(wspIntent);
                    } else {
                        Snackbar.make(view, "No app to Support this action", Snackbar.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Snackbar.make(view, "No app to Support this action", Snackbar.LENGTH_LONG).show();
                }
            }
        });

        fabEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.setType("message/rfc822");
                final String emailTo = getResources().getString(R.string.support_email);
                sendIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{emailTo});
                try {
                    System.out.println("All is well");
                    startActivity(Intent.createChooser(sendIntent, "Send Email..."));
                } catch (Exception e) {
                    System.out.println("Error in gmail");
                    Snackbar.make(view, "No app to Support this action", Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    /***
     * Returns respected fragment that user
     * selected from navigation menu
     * @param args
     */
    public void loadHomeFragment(final Bundle args) {
        View dashboardView = findViewById(R.id.dashboard_content);
        FragmentManager manager = getSupportFragmentManager();

        // selecting appropriate nav menu item
        if (navItemIndex < 10) {
            selectNavMenu();
        }

        // if user select the current navigation menu again, don't do anything
        // just close the navigation drawer
        if ((dashboardView.getVisibility() == View.VISIBLE && navItemIndex == 0) || getSupportFragmentManager().findFragmentByTag(CURRENT_TAG) != null) {
            return;
        }
        //if dashboard is loaded then set the visibilty as gone
        if (dashboardView.getVisibility() == View.VISIBLE) {
            dashboardView.setVisibility(View.GONE);
        }
        if (navItemIndex == 0) {
            if (manager.getBackStackEntryCount() > 0) {
                manager.popBackStackImmediate(Constants.TAG_STOPPAGE_REPORT, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
            dashboardView.setVisibility(View.VISIBLE);
            setUpTabWithIconContent();
            isDashboardLoaded = true;
        } else {
            // Sometimes, when fragment has huge data, screen seems hanging
            // when switching between navigation menus
            // So using runnable, the fragment is loaded with cross fade effect
            // This effect can be seen in GMail app
            Runnable mPendingRunnable = new Runnable() {
                @Override
                public void run() {
                    // update the main content by replacing fragments
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
                            android.R.anim.fade_out);
                    Fragment fragment = getHomeFragment();
                    if (args != null) {
                        fragment.setArguments(args);
                    }
                    if (isDashboardLoaded) {
                        fragmentTransaction.add(R.id.frame, fragment, CURRENT_TAG);
                    } else {
                        getSupportFragmentManager().popBackStackImmediate(CURRENT_TAG, 0);
                        fragmentTransaction.replace(R.id.frame, fragment, CURRENT_TAG);
                    }
                    fragmentTransaction.addToBackStack(CURRENT_TAG);
                    fragmentTransaction.commitAllowingStateLoss();
                    isDashboardLoaded = false;
                }
            };
            // If mPendingRunnable is not null, then add to the message queue
            mHandler.post(mPendingRunnable);
        }

        // show or hide the fab button
        //toggleFab();

        // refresh toolbar menu
        invalidateOptionsMenu();
    }

    private Fragment getHomeFragment() {
        switch (navItemIndex) {
            case 1:
                return new TravelReportFragment();
            case 2:
                return new StopReportFragment();
            case 20:
                return new StopListFragment();
            default:
                return new PhotosFragment();
        }
    }

    private void setUpTabWithIconContent() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                tabWithIconContent = new TabWithIconContent(DashboardActivity.this);
                tabWithIconContent.setUpTabWIthIconContent(getSupportFragmentManager());
                tabWithIconContent.setViewWithCurrentTab(0);
            }
        });
    }

    public void goToControlTab(final String vehicleno) {
        final CommandFragment fragment = tabWithIconContent.getCommandFragment();
        tabWithIconContent.setViewWithCurrentTab(2);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                fragment.setSelectVehicleText(vehicleno);
            }
        }, 500);
    }

    public TabWithIconContent getTabWithIconContent() {
        return this.tabWithIconContent;
    }

    private void setUpToolBar() {
        if (navItemIndex < 10) {
            //regain power to open drwer by left swipe
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            setToolBarTitle(activityTitles[navItemIndex]);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            //show hamburger
            mDrawerToggler.setDrawerIndicatorEnabled(true);
        } else {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            //not want to open the drawer on swipe from the left in this case
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            //remove hamburger
            mDrawerToggler.setDrawerIndicatorEnabled(false);
//
//            if(!mToolBarNavigationListenerIsRegistered) {
//                mDrawerToggler.setToolbarNavigationClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        onBackPressed();
//                    }
//                });
//                mToolBarNavigationListenerIsRegistered = true;
//            }
        }
    }

    public void setToolBarTitle(String title) {
        toolbar.setTitle(title);
    }

    private void selectNavMenu() {
        int itemPosition = navItemIndex > 10 ? navItemIndex / 10 : navItemIndex;
        navigationView.getMenu().getItem(itemPosition).setChecked(true);
    }

    private void setUpNavigationView() {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @SuppressWarnings("StatementWithEmptyBody")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // Handle navigation view item clicks here.
                int id = item.getItemId();

                //Checking if the item is in checked state or not, if not make it in checked state
                if (item.isChecked()) {
                    item.setChecked(false);
                } else {
                    item.setChecked(true);
                }
                item.setChecked(true);

                switch (id) {
                    case R.id.nav_dashboard:
                        setNavItemIndex(0, Constants.TAG_DASHBOARD);
                        break;
                    case R.id.nav_stoppage_report:
                        setNavItemIndex(2, Constants.TAG_STOPPAGE_REPORT);
                        break;
                    case R.id.nav_setting:
                        Intent settingIntent = new Intent(DashboardActivity.this, SettingsActivity.class);
                        settingIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        settingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(settingIntent);
                        overridePendingTransition(R.anim.left_enter, R.anim.right_out);
                        break;
//                    case R.id.nav_profile:
//                        Intent profileIntent = new Intent(DashboardActivity.this,ProfileActivity.class);
//                        profileIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                        profileIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        startActivity(profileIntent);
//                        overridePendingTransition(R.anim.left_enter, R.anim.right_out);
//                        break;
//                    case R.id.nav_about_us:
//                        break;
                    case R.id.nav_privacy_policy:
                        Intent policyIntent = new Intent(DashboardActivity.this, PrivacyPolicy.class);
                        policyIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        policyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(policyIntent);
                        overridePendingTransition(R.anim.left_enter, R.anim.right_out);
                        break;
                    default:
                        setNavItemIndex(0, Constants.TAG_DASHBOARD);
                }

                //close the nav drawer
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });
        mDrawerToggler = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
                loadHomeFragment(null);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank
                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawer.addDrawerListener(mDrawerToggler);

        //calling sync state is necessary or else your hamburger icon wont show up
        mDrawerToggler.syncState();

    }

    public void setNavItemIndex(int index, String tag) {
        navItemIndex = index;
        CURRENT_TAG = tag;
        // set toolbar title
        setUpToolBar();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return;
        }
        // This code loads home fragment when back key is pressed
        // when user is in other fragment than home
        if (shouldLoadHomeFragOnBackPress) {
            // checking if user is on other navigation menu
            // rather than home

            FragmentManager manager = getSupportFragmentManager();
            if (manager.getBackStackEntryCount() > 0) {
                manager.popBackStackImmediate();
            }
            Fragment StopReportFragment = fragmentManager
                    .findFragmentByTag(Constants.TAG_STOPPAGE_REPORT);

//            if (StopList_Fragment!=null){
//                FragmentManager manager = getSupportFragmentManager();
//                if (manager.getBackStackEntryCount()>0){
//                    manager.popBackStackImmediate();
//                    manager.get
//                    setNavItemIndex(2, Constants.TAG_STOPPAGE_REPORT);
//                }
//            }
            if (StopReportFragment != null) {
                setNavItemIndex(2, Constants.TAG_STOPPAGE_REPORT);
            } else if (navItemIndex != 0) {
                setNavItemIndex(0, Constants.TAG_DASHBOARD);
                loadHomeFragment(null);
            } else {
                AlertDialog.Builder dialog = alert.showAlertDialog(DashboardActivity.this, "Closing Activity", "Do you want to Exit?", "fail");
                // Setting EXIT Button
                dialog.setPositiveButton("EXIT", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                dialog.setNegativeButton("CANCEL", null);
                dialog.show();
            }
        } else {
            AlertDialog.Builder dialog = alert.showAlertDialog(DashboardActivity.this, "Closing Activity", "Do you want to Exit?", "fail");
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        switch (navItemIndex) {
            case 0:
                getMenuInflater().inflate(R.menu.dashboard, menu);
                break;
            default:
                getMenuInflater().inflate(R.menu.dashboard, menu);
                break;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_logout:
                // Clear the session data
                // This will clear all session data and
                // redirect user to LoginActivity
                AlertDialog.Builder alertDialog;
                alertDialog = alert.showAlertDialog(DashboardActivity.this, "Do you wish to logout ?", "", "fail");
                alertDialog.setNegativeButton("CANCEL", null);
                alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //logout user
                        session.logoutUser(DashboardActivity.this);
                    }
                }).show();
                return true;
            case R.id.action_show_map:
                //Change to
                Intent mapIntent = new Intent(DashboardActivity.this, MapsActivity.class);
                mapIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mapIntent.putExtra("liveTrack", true);
                startActivity(mapIntent);
                overridePendingTransition(R.anim.left_enter, R.anim.right_out);
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // register GCM registration complete receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(NotificationConfig.REGISTRATION_COMPLETE));

        // register new push message receiver
        // by doing this, the activity will be notified each time a new message arrives
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(NotificationConfig.PUSH_NOTIFICATION));

        // clear the notification area when the app is opened
        NotificationUtils.clearNotifications(getApplicationContext());
    }

    @Override
    public void getInputValue(final String input, String vehicleNo) {
        Utils.updateDriverNumber(input, vehicleNo, findViewById(R.id.viewpager), DashboardActivity.this);
    }
}
