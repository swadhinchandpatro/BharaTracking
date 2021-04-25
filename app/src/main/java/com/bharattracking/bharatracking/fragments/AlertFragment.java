package com.bharattracking.bharatracking.fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bharattracking.bharatracking.CustomToast;
import com.bharattracking.bharatracking.interfaces.APIInterface;
import com.bharattracking.bharatracking.session.SessionManagement;
import com.bharattracking.bharatracking.activities.MapsActivity;
import com.bharattracking.bharatracking.notifications.AlertAdapter;
import com.bharattracking.bharatracking.R;
import com.bharattracking.bharatracking.utilities.APIClient;
import com.bharattracking.bharatracking.utilities.Utils;
import com.bharattracking.bharatracking.notifications.AlertData;
import com.bharattracking.bharatracking.utilities.AlertDialogManager;
import com.google.android.gms.maps.model.LatLng;
import com.kunzisoft.switchdatetime.SwitchDateTimeDialogFragment;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.bharattracking.bharatracking.Constants.KEY_LAST_UPDATED_TIME;
import static com.bharattracking.bharatracking.Constants.KEY_LAT;
import static com.bharattracking.bharatracking.Constants.KEY_LNG;
import static com.bharattracking.bharatracking.Constants.KEY_LOCATION_INFO;
import static com.bharattracking.bharatracking.Constants.KEY_VEHICLE_NO;
import static com.bharattracking.bharatracking.Constants.TRACK_LIVE;

/**
 * A simple {@link Fragment} subclass.
 */
public class AlertFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, SearchView.OnQueryTextListener {

    private static final String TAG = AlertFragment.class.getSimpleName();
    public static final String TAG_DATETIME_FRAGMENT = "TAG_DATETIME_FRAGMENT";

    private View view;
    public static View.OnClickListener alertOnClickListener;
    private Activity activity;

    private static AlertAdapter adapter;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;

    private SwitchDateTimeDialogFragment startDateTimeFragment,endDateTimeFragment;
    SessionManagement session;
    AlertDialogManager alert = new AlertDialogManager();

    private ArrayList<AlertData> alertList;
    private SearchView searchView;
    private Button start_date,end_date;
    private Button applyChangeBtn;

    private SimpleDateFormat myDateFormat;
    private String curDateTime;
    private DateTime curDateTimeObj;
    private TextView textNoRecord;

    private boolean _hasLoadedOnce=false;

    public AlertFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_alert, container, false);
        //assign all view elements
        initView();
        textNoRecord.setVisibility(View.GONE);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        /*Expanding the search view */
        searchView.setIconified(false);
        searchView.setIconifiedByDefault(false);

        /* Code for changing the textcolor and hint color for the search view */

        SearchView.SearchAutoComplete searchAutoComplete =
                searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchAutoComplete.setHintTextColor(Color.BLACK);
        searchAutoComplete.setTextColor(Color.BLACK);
        /*Code for changing the search icon */
        ImageView searchIcon = searchView.findViewById(android.support.v7.appcompat.R.id.search_mag_icon);
        searchIcon.setImageResource(R.drawable.ic_search_black_24dp);
        searchView.setOnQueryTextListener(this);

        swipeRefreshLayout.setOnRefreshListener(this);

        setUpDateTimePicker();

        recyclerView.setHasFixedSize(true);
        alertList = new ArrayList<>();
        session = new SessionManagement(activity.getApplicationContext());

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        applyChangeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String start = start_date.getText().toString();
                String end = end_date.getText().toString();
                getAlertData(session.getMobileNumber(),start,end);
            }
        });

        adapter = new AlertAdapter(alertList);
        recyclerView.setAdapter(adapter);

        alertOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = recyclerView.getChildLayoutPosition(view);
                String vehicleNo = alertList.get(position).vehicleNo;
                String lat = alertList.get(position).lat;
                String lng = alertList.get(position).lng;
                String location = alertList.get(position).location;
                String datetime = alertList.get(position).updatedTime;

                Intent mapIntent = new Intent(activity,MapsActivity.class);
                mapIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                mapIntent.putExtra(KEY_VEHICLE_NO,vehicleNo);
                mapIntent.putExtra(KEY_LAST_UPDATED_TIME,datetime);
                mapIntent.putExtra(KEY_LAT,lat);
                mapIntent.putExtra(KEY_LNG,lng);
                mapIntent.putExtra(KEY_LOCATION_INFO,location);
                mapIntent.putExtra(TRACK_LIVE,false);

                activity.startActivity(mapIntent);
                activity.overridePendingTransition(R.anim.left_enter, R.anim.right_out);
            }
        };
        return view;
    }

    private void initView() {
        searchView = view.findViewById(R.id.alert_search);
        swipeRefreshLayout = view.findViewById(R.id.swipe_alerts);
        recyclerView = view.findViewById(R.id.notification_recycler_view);
        start_date = view.findViewById(R.id.start_date);
        end_date = view.findViewById(R.id.end_date);
        applyChangeBtn = view.findViewById(R.id.applyChangeBtn);
        textNoRecord = view.findViewById(R.id.text_no_alert);
        activity = getActivity();
    }

    private void setUpDateTimePicker() {
        //init curDateTime and set start and end button text
        setCurDateTime();
        // Construct SwitchDateTimePicker
        startDateTimeFragment = getDateTimeFragment();
        endDateTimeFragment = getDateTimeFragment();

        startDateTimeFragment.setOnButtonClickListener (new SwitchDateTimeDialogFragment.OnButtonClickListener() {
            @Override
            public void onPositiveButtonClick(Date date) {
                start_date.setText(myDateFormat.format(date));
            }

            @Override
            public void onNegativeButtonClick(Date date) {

            }
        });

        start_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Re-init each time
                String dateString = start_date.getText().toString();
                try {
                    Date date = myDateFormat.parse(dateString);
                    startDateTimeFragment.startAtCalendarView();
                    startDateTimeFragment.setDefaultDateTime(date);
                    startDateTimeFragment.show(getActivity().getSupportFragmentManager(), TAG_DATETIME_FRAGMENT);
                } catch (ParseException e) {
                    e.printStackTrace();
                    toastMessage(e.getMessage());
                }
            }
        });

        endDateTimeFragment.setOnButtonClickListener(new SwitchDateTimeDialogFragment.OnButtonClickListener() {
            @Override
            public void onPositiveButtonClick(Date date) {
                end_date.setText(myDateFormat.format(date));
            }

            @Override
            public void onNegativeButtonClick(Date date) {

            }
        });

        end_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Re-init each time
                String dateString = end_date.getText().toString();
                try {
                    Date date = myDateFormat.parse(dateString);
                    endDateTimeFragment.startAtCalendarView();
                    endDateTimeFragment.setDefaultDateTime(date);
                    endDateTimeFragment.show(getActivity().getSupportFragmentManager(), TAG_DATETIME_FRAGMENT);
                } catch (ParseException e) {
                    e.printStackTrace();
                    toastMessage(e.getMessage());
                }
            }
        });

    }

    private void setCurDateTime() {
        // Init format
        myDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        //current DateTime
        curDateTimeObj = new DateTime();
        curDateTime = myDateFormat.format(curDateTimeObj.toDate());
        end_date.setText(curDateTime);
        String datetime = curDateTime.substring(0,curDateTime.indexOf(' '))+" "+"00:00";
        start_date.setText(datetime);
    }

    private SwitchDateTimeDialogFragment getDateTimeFragment() {
        SwitchDateTimeDialogFragment dateTimeFragment = (SwitchDateTimeDialogFragment) getActivity().getSupportFragmentManager().findFragmentByTag(TAG_DATETIME_FRAGMENT);
        if(dateTimeFragment == null) {
            dateTimeFragment = SwitchDateTimeDialogFragment.newInstance(
                    getString(R.string.label_datetime_dialog),
                    getString(android.R.string.ok),
                    getString(android.R.string.cancel),
                    getString(R.string.clear) // Optional
            );
        }

        // Optionally define a timezone
        dateTimeFragment.setTimeZone(TimeZone.getTimeZone("Asia/Calcutta"));

        // Assign unmodifiable values
        dateTimeFragment.set24HoursMode(true);
        dateTimeFragment.setHighlightAMPMSelection(false);
        dateTimeFragment.setMinimumDateTime(new GregorianCalendar(2017, Calendar.JANUARY, 1).getTime());
        dateTimeFragment.setMaximumDateTime(new GregorianCalendar(2025, Calendar.DECEMBER, 31).getTime());

        // Define new day and month format
        try {
            dateTimeFragment.setSimpleDateMonthAndDayFormat(new SimpleDateFormat("MMMM dd", Locale.getDefault()));
        } catch (SwitchDateTimeDialogFragment.SimpleDateMonthAndDayFormatException e) {
            Log.e(TAG, e.getMessage());
        }

        return dateTimeFragment;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_alert_fragment, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        swipeRefreshLayout.requestFocus();
    }

    @Override
    public void onRefresh() {
        String start = start_date.getText().toString();
        String end = end_date.getText().toString();
        checkIfWithinWeek(start, end);
    }

    @Override
    public void setUserVisibleHint(boolean isFragmentVisible_) {
        super.setUserVisibleHint(isFragmentVisible_);
        if (this.isVisible()) {
            // we check that the fragment is becoming visible
            if (isFragmentVisible_) {
                setCurDateTime();
                //run your async task here since the user has just focused on your fragment
                String start = start_date.getText().toString();
                String end = end_date.getText().toString();
                checkIfWithinWeek(start, end);
            }
        }
    }

    private void checkIfWithinWeek(String... params) {
        try {
            long duration = Utils.getTimeDifference(params[0],params[1]);
            if (TimeUnit.MILLISECONDS.toDays(duration) > 7){
                new CustomToast().Show_Toast(getActivity(), view,
                        "Only 7 days alerts Available");
            }else {
                getAlertData(session.getMobileNumber(),params[0],params[1]);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        adapter.filter(newText);
        if (TextUtils.isEmpty(newText)) {
            adapter.filter("");
        }
        else {
            adapter.filter(newText);
        }
        return true;
    }

    private void getAlertData(String userName, String start, String end) {
        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        final Call<ArrayList<AlertData>> getAlertData = apiInterface.getAlertData(userName, start, end);

        swipeRefreshLayout.setRefreshing(true);

        getAlertData.enqueue(new Callback<ArrayList<AlertData>>() {
            @Override
            public void onResponse(Call<ArrayList<AlertData>> call, Response<ArrayList<AlertData>> response) {
                if (response.isSuccessful()) {
                    alertList = response.body();

                    if (alertList.isEmpty()) {
                        textNoRecord.setVisibility(View.VISIBLE);
                    } else {
                        textNoRecord.setVisibility(View.GONE);
                        if (!_hasLoadedOnce) {
                            final LayoutAnimationController controller =
                                    AnimationUtils.loadLayoutAnimation(activity, R.anim.layout_animation_up_to_down);
                            recyclerView.setLayoutAnimation(controller);
                            adapter.updateAlertData(alertList);
                            recyclerView.scheduleLayoutAnimation();
                        } else {
                            adapter.updateAlertData(alertList);
                        }
                        _hasLoadedOnce = true;

                    }
                } else {
                    AlertDialog.Builder dialog = alert.showAlertDialog(activity, "Connection Problem..", "OOPs! Check your Network Connection", "fail");
                    dialog.setNegativeButton("OK", null);
                    dialog.show();
                }
                if (swipeRefreshLayout.isRefreshing())
                    swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<ArrayList<AlertData>> call, Throwable t) {
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                    AlertDialog.Builder dialog = alert.showAlertDialog(activity, "Connection Problem..", "OOPs! Check your Network Connection", "fail");
                    dialog.setNegativeButton("OK", null);
                    dialog.show();
                }
            }
        });
    }

    /** Helper for toasting exception messages on the UI thread. */
    private void toastMessage(final String msg) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity().getApplicationContext(),msg, Toast.LENGTH_LONG).show();
            }
        });
    }
}
