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
import com.bharattracking.bharatracking.activities.DashboardActivity;
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
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class TravelReportFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, SearchView.OnQueryTextListener {

    private static final String TAG = TravelReportFragment.class.getSimpleName();
    private static final String TAG_DATETIME_FRAGMENT = "TAG_DATETIME_FRAGMENT";

    private View view;
    private Activity activity;

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
    private ListView tvlr_list;
    private static FragmentManager fragmentManager;
    private ArrayList<VehicleReportDetails> travelReportList = new ArrayList<>();

    public TravelReportFragment() {
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
        view = inflater.inflate(R.layout.fragment_travelreport, container, false);
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
        adapter=new ReportAdapter(getActivity(), travelReportList);
        tvlr_list.setAdapter(adapter);
        tvlr_list.setTextFilterEnabled(true);

        ArrayList<VehicleLiveData> vehicleLiveData = RecordDataHolder.getInstance().getData();

        applyChangeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRefresh();
            }
        });
        // Click event for single list row
        tvlr_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick (AdapterView<?> parent, View view,
                                    int position, long id) {
                if(position>0 && position <= travelReportList.size()) {
                    // close search view if its visible
                    if (searchView.isShown()) {
                        searchView.setQuery("", false);
                    }
                }

                VehicleReportDetails run = travelReportList.get(position);
                String start = start_date.getText().toString();
                String end = end_date.getText().toString();
                Bundle args = new Bundle();
                args.putString("vehicleno", run.vehicleno);
                args.putString("startdate",start);
                args.putString("enddate",end);
                RunListFragment runListFragment = new RunListFragment();
                runListFragment.setArguments(args);
                ((DashboardActivity)getActivity()).setNavItemIndex(10, Constants.TAG_RUN_LIST);
                ((DashboardActivity)getActivity()).setToolBarTitle(run.vehicleno);

                fragmentManager
                        .beginTransaction()
                        .setCustomAnimations(R.anim.right_enter, R.anim.left_out)
                        .replace(R.id.frame,runListFragment,
                                Utils.RUN_LIST_FRAGMENT)
                        .addToBackStack(null)
                        .commit();
            }
        });
        return view;
    }

    private void initView() {
        searchView = view.findViewById(R.id.tvlr_search);
        swipeRefreshLayout = view.findViewById(R.id.swipe_travelreport);
        tvlr_list = view.findViewById(R.id.tvlr_list);
        start_date = view.findViewById(R.id.tvlr_start_date);
        end_date = view.findViewById(R.id.tvlr_end_date);
        applyChangeBtn = view.findViewById(R.id.tvlr_applyChangeBtn);
        textNoReport = view.findViewById(R.id.text_no_report);
        activity = getActivity();
        fragmentManager = getActivity().getSupportFragmentManager();
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
        //run your async task here since the user has just focused on your fragment
        onRefresh();
    }

    @Override
    public void onRefresh() {
        String start = start_date.getText().toString();
        String end = end_date.getText().toString();
        if(checkIfWithinWeek(start, end)){
            getTravelReport(session.getMobileNumber(),start,end);
        }else {
            new CustomToast().Show_Toast(getActivity(), view,
                    "Only 7 days alerts Available");
        }
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

    /**
     * Async task class to get json by making HTTP call
     */
    private void getTravelReport(String mobileno, String start, String end) {
        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        final Call<ArrayList<VehicleReportDetails>> travelDetailsCall = apiInterface.getTravelReport(mobileno,start,end);
        swipeRefreshLayout.setRefreshing(true);

        travelDetailsCall.enqueue(new Callback<ArrayList<VehicleReportDetails>>() {
            @Override
            public void onResponse(Call<ArrayList<VehicleReportDetails>> call, Response<ArrayList<VehicleReportDetails>> response) {
                if (response.isSuccessful()){
                    travelReportList.clear();
                    travelReportList = response.body();

                    populateReportListView();
                }else {
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

    private void populateReportListView() {
        if (travelReportList.isEmpty()){
            textNoReport.setVisibility(View.VISIBLE);
        } else {
            textNoReport.setVisibility(View.GONE);
            adapter.updateReportData(travelReportList);
        }
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
