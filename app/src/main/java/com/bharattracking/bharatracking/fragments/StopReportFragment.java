package com.bharattracking.bharatracking.fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bharattracking.bharatracking.APIresponses.VehicleLiveData;
import com.bharattracking.bharatracking.APIresponses.VehicleReportDetails;
import com.bharattracking.bharatracking.Constants;
import com.bharattracking.bharatracking.CustomToast;
import com.bharattracking.bharatracking.R;
import com.bharattracking.bharatracking.RecordDataHolder;
import com.bharattracking.bharatracking.session.SessionManagement;
import com.bharattracking.bharatracking.interfaces.APIInterface;
import com.bharattracking.bharatracking.utilities.APIClient;
import com.bharattracking.bharatracking.utilities.AlertDialogManager;
import com.bharattracking.bharatracking.utilities.Utils;
import com.kunzisoft.switchdatetime.SwitchDateTimeDialogFragment;

import org.joda.time.DateTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StopReportFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, SearchView.OnQueryTextListener {

    private static final String TAG = StopReportFragment.class.getSimpleName();
    private static final String TAG_DATETIME_FRAGMENT = "TAG_DATETIME_FRAGMENT";

    private View view;
    private Activity activity;
    private static FragmentManager fragmentManager;

    private static ReportAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    private SwitchDateTimeDialogFragment startDateTimeFragment,endDateTimeFragment;
    SessionManagement session;
    AlertDialogManager alert = new AlertDialogManager();

    private SearchView searchView;
    private Button start_date,end_date;
    private Button applyChangeBtn;

    private SimpleDateFormat myDateFormat;
    private String curDateTime;
    private DateTime curDateTimeObj;
    private TextView textNoReport;
    private ListView stop_list;
    private boolean isSingleVehicle = false;
    private ArrayList<VehicleReportDetails> stopReportList = new ArrayList<>();
    private ArrayList<VehicleLiveData> vehicleLiveData = new ArrayList<>();
    private HashMap<String,String> selectedVehicle = new HashMap<>();
    private Date startDate;

    public StopReportFragment() {
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
        view = inflater.inflate(R.layout.fragment_stopreport, container, false);
        //assign all view elements
        initView();
        textNoReport.setVisibility(View.GONE);

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

        session = new SessionManagement(activity.getApplicationContext());
        adapter=new ReportAdapter(getActivity(), stopReportList);
        stop_list.setAdapter(adapter);
        stop_list.setTextFilterEnabled(true);

        vehicleLiveData = RecordDataHolder.getInstance().getData();

        String start,end;
        start = start_date.getText().toString();
        end = end_date.getText().toString();
        if (getArguments()!= null && getArguments().getString("unitid") != null){
            isSingleVehicle = true;
            selectedVehicle.put(Constants.KEY_UNIT_ID,getArguments().getString(Constants.KEY_UNIT_ID));
            selectedVehicle.put(Constants.KEY_THUMB_URL,getArguments().getString(Constants.KEY_THUMB_URL));
            selectedVehicle.put(Constants.KEY_VEHICLE_NO,getArguments().getString(Constants.KEY_VEHICLE_NO));

            getVehicleStopDetails(selectedVehicle.get(Constants.KEY_UNIT_ID),start,end);
//            if (checkIfWithinWeek(start, end)){
//            }else {
//                new CustomToast().Show_Toast(getActivity(), view,
//                        "Only 7 days alerts Available");
//            }
        }else {
            if (checkIfWithinWeek(start, end)){
                getAllStopsDetails(session.getMobileNumber(),start,end);
            }else {
                new CustomToast().Show_Toast(getActivity(), view,
                        "Only 7 days alerts Available");
            }
        }

        applyChangeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRefresh();
            }
        });
        // Click event for single list row
        stop_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick (AdapterView<?> parent, View view,
                                     int position, long id) {
                if(position>0 && position <= stopReportList.size()) {
                    // close search view if its visible
                    if (searchView.isShown()) {
                        searchView.setQuery("", false);
                    }
                }

                VehicleReportDetails stop = stopReportList.get(position);
                String start = start_date.getText().toString();
                String end = end_date.getText().toString();
                Bundle args = new Bundle();
                args.putString("vehicleno", stop.vehicleno);
                args.putString("unitid", stop.unitid);
                args.putString("startdate",start);
                args.putString("enddate",end);
                StopListFragment stopListFragment = new StopListFragment();
                stopListFragment.setArguments(args);

                fragmentManager
                        .beginTransaction()
                        .setCustomAnimations(R.anim.right_enter, R.anim.left_out)
                        .replace(R.id.frame,stopListFragment ,
                                Utils.STOP_LIST_FRAGMENT)
                        .addToBackStack(null)
                        .commit();
            }
        });
        return view;
    }

    private void initView() {
        searchView = view.findViewById(R.id.stop_search);
        swipeRefreshLayout = view.findViewById(R.id.swipe_stopreport);
        stop_list = view.findViewById(R.id.stop_list);
        applyChangeBtn = view.findViewById(R.id.stop_applyChangeBtn);
        textNoReport = view.findViewById(R.id.text_no_report);
        activity = getActivity();
        fragmentManager = getActivity().getSupportFragmentManager();
    }

    private void setUpDateTimePicker() {
        start_date = view.findViewById(R.id.stop_start_date);
        end_date = view.findViewById(R.id.stop_end_date);
        //init curDateTime and set start and end button text
        setCurDateTime();
        // Construct SwitchDateTimePicker
        startDateTimeFragment = getDateTimeFragment();
        endDateTimeFragment = getDateTimeFragment();

        startDateTimeFragment.setOnButtonClickListener (new SwitchDateTimeDialogFragment.OnButtonClickListener() {
            @Override
            public void onPositiveButtonClick(Date date) {
                setDateInPickDateTime(date,start_date);
                startDate = date;
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
                setDateInPickDateTime(date,end_date);
            }

            @Override
            public void onNegativeButtonClick(Date date) {

            }
        });

        end_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DateTime dateTime = new DateTime(startDate);
                endDateTimeFragment.startAtCalendarView();
                endDateTimeFragment.setDefaultDay(dateTime.getDayOfMonth());
                endDateTimeFragment.setDefaultMonth(dateTime.getMonthOfYear() -1);
                endDateTimeFragment.show(getActivity().getSupportFragmentManager(), TAG_DATETIME_FRAGMENT);
            }
        });

    }

    private void setDateInPickDateTime(Date date,TextView textView){
        textView.setText(myDateFormat.format(date));
    }

    private void setCurDateTime() {
        // Init format
        myDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        //current DateTime
        curDateTimeObj = new DateTime();
        setDateInPickDateTime(curDateTimeObj.toDate(),end_date);
        curDateTime = myDateFormat.format(curDateTimeObj.toDate());
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
        //run your async task here since the user has just focused on your fragment
    }

    @Override
    public void onRefresh() {
        String start = start_date.getText().toString();
        String end = end_date.getText().toString();
        if (isSingleVehicle){
            getVehicleStopDetails(selectedVehicle.get(Constants.KEY_UNIT_ID),start,end);
        }else {
            getAllStopsDetails(session.getMobileNumber(),start,end);
        }
//        if (checkIfWithinWeek(start, end)){
//
//        }else {
//            new CustomToast().Show_Toast(getActivity(), view,
//                    "Only 7 days alerts Available");
//        }
    }

    private boolean checkIfWithinWeek(String... params) {
        try {
            long duration = Utils.getTimeDifference(params[0],params[1]);
            if (TimeUnit.MILLISECONDS.toDays(duration) > 7){
                return false;
            }else {
                return true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return true;
        }
    }

    private void populateReportListView() {
        if (stopReportList.isEmpty()){
            textNoReport.setVisibility(View.VISIBLE);
        } else {
            textNoReport.setVisibility(View.GONE);
            adapter.updateReportData(stopReportList);
        }
    }

    private void getVehicleStopDetails(final String unitid, String start, String end) {
        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        final Call<VehicleReportDetails> stopDetailsCall = apiInterface.getVehicleStopReport(unitid,start,end);
        swipeRefreshLayout.setRefreshing(true);

        stopDetailsCall.enqueue(new Callback<VehicleReportDetails>() {
            @Override
            public void onResponse(Call<VehicleReportDetails> call, Response<VehicleReportDetails> response) {
                if (response.isSuccessful()){
                    VehicleReportDetails stopDetails = response.body();
                    stopReportList.clear();
                    stopReportList.add(stopDetails);

                    if (stopReportList.size() > 0){
                        stopReportList.get(0).vehicleno = selectedVehicle.get(Constants.KEY_VEHICLE_NO);
                        stopReportList.get(0).vtype = selectedVehicle.get(Constants.KEY_THUMB_URL);
                        stopReportList.get(0).unitid = selectedVehicle.get(Constants.KEY_UNIT_ID);
                    }
                    populateReportListView();
                }else {
                    AlertDialog.Builder dialog = alert.showAlertDialog(getActivity(), "Connection Problem..", "OOPs! Check your Network Connection", "fail");
                    dialog.setNegativeButton("OK",null);
                    dialog.show();
                }
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<VehicleReportDetails> call, Throwable t) {
                toastMessage(t.getMessage());
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void getAllStopsDetails(String mobileno, String start, String end) {
        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        final Call<ArrayList<VehicleReportDetails>> allStopsDetailsCall = apiInterface.getAllStopsReport(mobileno,start,end);
        swipeRefreshLayout.setRefreshing(true);

        allStopsDetailsCall.enqueue(new Callback<ArrayList<VehicleReportDetails>>() {
            @Override
            public void onResponse(Call<ArrayList<VehicleReportDetails>> call, Response<ArrayList<VehicleReportDetails>> response) {
                if (response.isSuccessful()){
                    stopReportList.clear();
                    stopReportList = response.body();

                    populateReportListView();
                } else {
                    AlertDialog.Builder dialog = alert.showAlertDialog(getActivity(), "Connection Problem..", "OOPs! Check your Network Connection", "fail");
                    dialog.setNegativeButton("OK",null);
                    dialog.show();
                }
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<ArrayList<VehicleReportDetails>> call, Throwable t) {
                toastMessage(t.getMessage());
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        adapter.getFilter().filter(newText);
        if (TextUtils.isEmpty(newText)) {
            adapter.getFilter().filter("");
        }
        else {
            adapter.getFilter().filter(newText);
        }
        return true;
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
