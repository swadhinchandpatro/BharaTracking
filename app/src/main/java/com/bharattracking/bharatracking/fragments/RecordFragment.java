package com.bharattracking.bharatracking.fragments;


import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bharattracking.bharatracking.APIresponses.VehicleLiveData;
import com.bharattracking.bharatracking.R;
import com.bharattracking.bharatracking.RecordAdapter;
import com.bharattracking.bharatracking.RecordDataHolder;
import com.bharattracking.bharatracking.activities.DashboardActivity;
import com.bharattracking.bharatracking.activities.MapsActivity;
import com.bharattracking.bharatracking.activities.RouteActivity;
import com.bharattracking.bharatracking.session.SessionManagement;
import com.bharattracking.bharatracking.interfaces.APIInterface;
import com.bharattracking.bharatracking.utilities.APIClient;
import com.bharattracking.bharatracking.utilities.Utils;
import com.bharattracking.bharatracking.utilities.AlertDialogManager;

import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.bharattracking.bharatracking.Constants.KEY_END_TIME;
import static com.bharattracking.bharatracking.Constants.KEY_START_TIME;
import static com.bharattracking.bharatracking.Constants.KEY_THUMB_URL;
import static com.bharattracking.bharatracking.Constants.KEY_UNIT_ID;
import static com.bharattracking.bharatracking.Constants.KEY_VEHICLE_NO;
import static com.bharattracking.bharatracking.Constants.TAG_STOPPAGE_REPORT;
import static com.bharattracking.bharatracking.Constants.TAG_STOP_LIST;


/**
 * A simple {@link Fragment} subclass.
 */
public class RecordFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, SearchView.OnQueryTextListener, View.OnClickListener {

    static final String URL = "http://api.androidhive.info/music/music.xml";
    private static final String TAG = RecordFragment.class.getSimpleName();

    private int mInterval = 30000; // 5 seconds by default, can be changed later
    private Handler mHandler;
    SessionManagement session;
    AlertDialogManager alert = new AlertDialogManager();
    RecordAdapter adapter;
    View recordFragmentView;
    String statusPressed = "0000";
    RecordDataHolder mHolder;

    private RecyclerView recyclerView;
    private ArrayList<VehicleLiveData> liveData;
    private SwipeRefreshLayout swipeRefreshLayout;
    private BottomSheetDialog bottomSheetDialog;
    private SearchView searchView;
    private LinearLayout runningLayout, stoppedLayout, dormantLayout, nworkingLayout;
    private TextView running,stopped,dormant,notWorking;
    private Activity activity;

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                fetchLiveData(Utils.DATA_REQUEST_TOKEN,session.getMobileNumber());
            } finally {
                //call the runnable again after interval
                mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };

    private boolean _hasLoadedOnce=false;

    private TextView textNoRecord;
    private String selectedVehicleNo,selectedDuration = null;
    private HashMap<String,Button> allowedDurationOptions = new HashMap<>();

    public static View.OnClickListener recordOnClickListener,optionsOnClickListner;

    public RecordFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mHolder =  RecordDataHolder.getInstance();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        recordFragmentView = inflater.inflate(R.layout.fragment_records, container, false);

        initView();

        runningLayout.setOnClickListener(this);
        stoppedLayout.setOnClickListener(this);
        dormantLayout.setOnClickListener(this);
        nworkingLayout.setOnClickListener(this);

        swipeRefreshLayout = recordFragmentView.findViewById(R.id.swipe_record);
        session = new SessionManagement(activity.getApplicationContext());

        searchView = recordFragmentView.findViewById(R.id.record_search);
        SearchManager searchManager = (SearchManager) activity.getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(activity.getComponentName()));
        /*Expanding the search view */
        searchView.setIconified(false);
        searchView.setIconifiedByDefault(false);

        /*Code for changing the search icon */
        ImageView searchIcon = searchView.findViewById(android.support.v7.appcompat.R.id.search_mag_icon);
        searchIcon.setImageResource(R.drawable.ic_search_black_24dp);
        searchView.setOnQueryTextListener(this);

        swipeRefreshLayout.setOnRefreshListener(this);
        recyclerView.setHasFixedSize(true);
        liveData = new ArrayList<>();

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        // Getting adapter by passing xml data ArrayList
        adapter = new RecordAdapter(activity, liveData);
        recyclerView.setAdapter(adapter);

        /**
         * Showing Swipe Refresh animation on activity create
         * As animation won't start on onCreate, post runnable is used
         */
        mHandler = new Handler();

        // Click event for single list row
        optionsOnClickListner = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View parentRow = (View) view.getParent();
                TextView vehicleNoView = parentRow.findViewById(R.id.vehicle_no);
                selectedVehicleNo = vehicleNoView.getText().toString();
                createVehicleOptionsDialog();
                bottomSheetDialog.show();
            }
        };

        recordOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView vehicleno = view.findViewById(R.id.vehicle_no);
                Intent mapIntent = new Intent(activity,MapsActivity.class);
                mapIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                mapIntent.putExtra(KEY_VEHICLE_NO,vehicleno.getText().toString());
                mapIntent.putExtra("liveTrack",true);
                activity.startActivity(mapIntent);
                activity.overridePendingTransition(R.anim.left_enter, R.anim.right_out);
            }
        };

        //TODO : multi select vehicle

        return recordFragmentView;
    }

    private void initView() {
        activity = getActivity();
        bottomSheetDialog = new BottomSheetDialog(activity);
        textNoRecord = recordFragmentView.findViewById(R.id.text_no_record);
        textNoRecord.setVisibility(View.GONE);

        recyclerView = recordFragmentView.findViewById(R.id.record_recycler_view);

        View vehicleCategoryMetric = ((DashboardActivity)activity).getVehicleCategoryMetric();
        running = vehicleCategoryMetric.findViewById(R.id.running_vehicle);
        stopped = vehicleCategoryMetric.findViewById(R.id.stopped_vehicle);
        dormant = vehicleCategoryMetric.findViewById(R.id.dormant_vehicle);
        notWorking = vehicleCategoryMetric.findViewById(R.id.not_working_vehicle);

        runningLayout = vehicleCategoryMetric.findViewById(R.id.running_status);
        stoppedLayout = vehicleCategoryMetric.findViewById(R.id.stopped_status);
        dormantLayout = vehicleCategoryMetric.findViewById(R.id.dormant_status);
        nworkingLayout = vehicleCategoryMetric.findViewById(R.id.not_working_status);
    }

    private void createVehicleOptionsDialog() {
        if(selectedVehicleNo != null){
            View view =  LayoutInflater.from(activity).inflate(R.layout.bottomsheet_vehicle_options,null);

            TextView bottomSheetHeader = view.findViewById(R.id.bottom_sheet_header);
            ImageButton stopRecordBtn = view.findViewById(R.id.report);
            ImageButton stopMapBtn = view.findViewById(R.id.route);
            ImageButton callDriver = view.findViewById(R.id.call_driver_btn);
            ImageButton controls = view.findViewById(R.id.controls);
            ImageButton shareLocation = view.findViewById(R.id.share_location);

            stopRecordBtn.setOnClickListener(this);
            stopMapBtn.setOnClickListener(this);
            callDriver.setOnClickListener(this);
            controls.setOnClickListener(this);
            shareLocation.setOnClickListener(this);
            bottomSheetHeader.setText(selectedVehicleNo.toUpperCase());
            bottomSheetDialog.setContentView(view);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void createShareLocationDialog() {

        View view =  LayoutInflater.from(activity).inflate(R.layout.bottomsheet_share_live_location,null);

        TextView bottomSheetHeader = view.findViewById(R.id.bottom_sheet_header);
        FloatingActionButton shareLocationBtn = view.findViewById(R.id.share_location_btn);
        LinearLayout durationLayout = view.findViewById(R.id.duration_layout);
        final EditText shareTextView = view.findViewById(R.id.share_message);
        bottomSheetHeader.setText("Share " + selectedVehicleNo.toUpperCase() + " Live");

        String[] allowedDurations = activity.getResources().getStringArray(R.array.duration_values);
        for (int i=0 ; i< allowedDurations.length ; i++) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(Utils.getPixelFromDp(activity,50),Utils.getPixelFromDp(activity,50),1.0f);
            params.setMargins(Utils.getPixelFromDp(activity,5),0,Utils.getPixelFromDp(activity,5),0);

            final Button button = new Button(activity);
            button.setBackgroundResource(R.drawable.round_border_line_bg);
            button.setElevation(Utils.getPixelFromDp(activity,2));
            button.setText(allowedDurations[i]);
            button.setTag(allowedDurations[i]);
            button.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
            button.setTextColor(Color.parseColor("#02305E"));
            button.setAllCaps(false);
            button.setLayoutParams(params);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String duration = view.getTag().toString();
                    if (selectedDuration != duration) {
                        allowedDurationOptions.get(selectedDuration).setBackgroundResource(R.drawable.round_border_line_bg);
                        allowedDurationOptions.get(selectedDuration).setTextColor(Color.parseColor("#02305E"));
                        button.setBackgroundResource(R.drawable.rounded_button);
                        button.setTextColor(Color.WHITE);
                        selectedDuration = duration;
                    }
                }
            });

            if (i == 0){
                button.setBackgroundResource(R.drawable.rounded_button);
                button.setTextColor(Color.WHITE);
                selectedDuration = allowedDurations[i];
            }
            durationLayout.addView(button);
            allowedDurationOptions.put(allowedDurations[i],button);
        }

        shareLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String shareMessage = shareTextView.getText().toString();
                Utils.shareLocation(recordFragmentView,getActivity().getSupportFragmentManager(),activity,shareMessage,selectedVehicleNo,selectedDuration);
            }
        });

        bottomSheetDialog.setContentView(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        swipeRefreshLayout.requestFocus();
        startPeriodicUpdatedData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopPeriodicUpdatedData();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopPeriodicUpdatedData();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopPeriodicUpdatedData();
    }

    //start periodic task

    private void startPeriodicUpdatedData() {
        mStatusChecker.run();
    }
    //end periodic task
    private void stopPeriodicUpdatedData() {
        mHandler.removeCallbacks(mStatusChecker);
    }

    private void fetchLiveData(String dataRequestToken, String user) {
        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        final Call<ArrayList<VehicleLiveData>> getliveData = apiInterface.getLiveData(user,dataRequestToken);

        swipeRefreshLayout.setRefreshing(true);

        getliveData.enqueue(new Callback<ArrayList<VehicleLiveData>>() {
            @Override
            public void onResponse(Call<ArrayList<VehicleLiveData>> call, Response<ArrayList<VehicleLiveData>> response) {
                if(response.isSuccessful()) {
                    liveData = response.body();

                    if (liveData != null && liveData.isEmpty()){
                        textNoRecord.setVisibility(View.VISIBLE);
                    } else {
                        textNoRecord.setVisibility(View.GONE);
                        int vehicleCount = ((DashboardActivity)activity).getTabWithIconContent().getTabIconCount(0);
                        mHolder.setData(liveData);
                        int[] vStatus = mHolder.getvStatus();

                        if (vehicleCount != liveData.size()) {
                            ((DashboardActivity)activity).getTabWithIconContent().updateTabIconCount(0,liveData.size());
                        }

                        running.setText(String.format(Locale.getDefault(),"%d",vStatus[0]));
                        stopped.setText(String.format(Locale.getDefault(),"%d",vStatus[1]));
                        dormant.setText(String.format(Locale.getDefault(),"%d",vStatus[2]));
                        notWorking.setText(String.format(Locale.getDefault(),"%d",vStatus[3]));

                        /**
                         * Updating parsed JSON data into ListView
                         * */
                        if (!_hasLoadedOnce) {
                            final LayoutAnimationController controller =
                                    AnimationUtils.loadLayoutAnimation(activity, R.anim.layout_animation_up_to_down);

                            recyclerView.setLayoutAnimation(controller);
                            adapter.updateLiveData(liveData);
                            recyclerView.scheduleLayoutAnimation();
                        }else {
                            adapter.updateLiveData(liveData);
                        }
                        _hasLoadedOnce = true;
                    }
                }else {
                    AlertDialog.Builder dialog = alert.showAlertDialog(activity, "Connection Problem..", "OOPs! Check your Network Connection", "fail");
                    dialog.setNegativeButton("OK",null);
                    dialog.show();
                }
                if (swipeRefreshLayout.isRefreshing())
                    swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<ArrayList<VehicleLiveData>> call, Throwable t) {
                if (swipeRefreshLayout.isRefreshing()){
                    swipeRefreshLayout.setRefreshing(false);
                    AlertDialog.Builder dialog = alert.showAlertDialog(activity, "Connection Problem..", "OOPs! Check your Network Connection", "fail");
                    dialog.setNegativeButton("OK",null);
                    dialog.show();
                }
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_records_fragment, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onRefresh() {
        fetchLiveData(Utils.DATA_REQUEST_TOKEN,session.getMobileNumber());
        stopPeriodicUpdatedData();
        startPeriodicUpdatedData();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_clear_log:
                onRefresh();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        adapter.getFilter().filter(newText);
        if (TextUtils.isEmpty(newText)) {
            adapter.filter("");
        }
        else {
            adapter.filter(newText);
        }
        return true;
    }

//    @Override
//    public void setUserVisibleHint(boolean isFragmentVisible_) {
//        super.setUserVisibleHint(isFragmentVisible_);
//        if (this.isVisible()) {
//            // we check that the fragment is becoming visible
//            if (isFragmentVisible_ && !_hasLoadedOnce) {
//                //run your async task here since the user has just focused on your fragment
////                startPeriodicUpdatedData();
//            }
//        }
//    }

    @Override
    public void onClick(View view) {
        switch (statusPressed){
            case "1000":
                runningLayout.setBackground(getResources().getDrawable(R.drawable.green_shade));
                break;
            case "0100":
                stoppedLayout.setBackground(getResources().getDrawable(R.drawable.yellow_shade));
                break;
            case "0010":
                dormantLayout.setBackground(getResources().getDrawable(R.drawable.red_shade));
                break;
            case "0001":
                nworkingLayout.setBackground(getResources().getDrawable(R.drawable.blue_shade));
                break;
        }
        switch (view.getId()){
            case R.id.running_status:
                if (!statusPressed.equals("1000")){
                    runningLayout.setBackgroundColor(getResources().getColor(R.color.running_bg));
                    adapter.filter("status:running");
                    statusPressed = "1000";
                }else {
                    adapter.filter("");
                    statusPressed = "0000";
                }
                break;
            case R.id.stopped_status:
                if (!statusPressed.equals("0100")){
                    stoppedLayout.setBackgroundColor(getResources().getColor(R.color.stopped_bg));
                    adapter.filter("status:stopped");
                    statusPressed = "0100";
                }else {
                    adapter.filter("");
                    statusPressed = "0000";
                }
                break;
            case R.id.dormant_status:
                if (!statusPressed.equals("0010")) {
                    dormantLayout.setBackgroundColor(getResources().getColor(R.color.dormant_bg));
                    adapter.filter("status:dormant");
                    statusPressed = "0010";
                }else {
                    adapter.filter("");
                    statusPressed = "0000";
                }
                break;
            case R.id.not_working_status:
                if (!statusPressed.equals("0001")) {
                    nworkingLayout.setBackgroundColor(getResources().getColor(R.color.nworking_bg));
                    adapter.filter("status:not working");
                    statusPressed = "0001";
                }else {
                    adapter.filter("");
                    statusPressed = "0000";
                }
                break;
            case R.id.report:
                bottomSheetDialog.dismiss();
                Bundle args = new Bundle();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                DateTime curDateTimeObj = new DateTime();
                String end = dateFormat.format(curDateTimeObj.toDate());
                String start = end.substring(0,end.indexOf(' '))+" 00:00";
                for (VehicleLiveData v : liveData){
                    if (v.vehicleno.equals(selectedVehicleNo)){
                        args.putString(KEY_VEHICLE_NO, v.vehicleno);
                        args.putString(KEY_UNIT_ID, v.unitid);
                        args.putString(KEY_THUMB_URL, v.vtype);
//                        args.putString(KEY_START_TIME,start);
//                        args.putString(KEY_END_TIME,end);
                        break;
                    }
                }
//                ((DashboardActivity)activity).setNavItemIndex(20,TAG_STOP_LIST);
                ((DashboardActivity)activity).setNavItemIndex(2,TAG_STOPPAGE_REPORT);
                ((DashboardActivity)activity).loadHomeFragment(args);

                break;
            case R.id.route:
                bottomSheetDialog.dismiss();
                Intent routeIntent = new Intent(activity,RouteActivity.class);
                routeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                routeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                routeIntent.putExtra("vehicle_no",selectedVehicleNo);
                activity.startActivity(routeIntent);
                activity.overridePendingTransition(R.anim.left_enter, R.anim.right_out);
                break;
            case R.id.call_driver_btn:
                bottomSheetDialog.dismiss();
                Utils.callDriver(view,getActivity().getSupportFragmentManager(),activity,selectedVehicleNo);
                break;
            case R.id.controls:
                bottomSheetDialog.dismiss();
                ((DashboardActivity)activity).goToControlTab(selectedVehicleNo);
                break;
            case R.id.share_location:
                bottomSheetDialog.dismiss();
                createShareLocationDialog();
                bottomSheetDialog.show();
        }
    }
}
