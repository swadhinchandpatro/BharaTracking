package com.bharattracking.bharatracking.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bharattracking.bharatracking.APIresponses.RouteWithStops;
import com.bharattracking.bharatracking.Constants;
import com.bharattracking.bharatracking.CustomToast;
import com.bharattracking.bharatracking.R;
import com.bharattracking.bharatracking.session.SessionManagement;
import com.bharattracking.bharatracking.activities.DashboardActivity;
import com.bharattracking.bharatracking.interfaces.APIInterface;
import com.bharattracking.bharatracking.utilities.APIClient;
import com.bharattracking.bharatracking.utilities.Utils;
import com.bharattracking.bharatracking.activities.MapsActivity;
import com.bharattracking.bharatracking.utilities.AlertDialogManager;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StopListFragment extends Fragment implements SearchView.OnQueryTextListener{

    private static final String TAG = StopListFragment.class.getSimpleName();
    public static View.OnClickListener stopListOnClickListner;

    private View view;
    private Activity activity;

    private StopListItemAdapter adapter;

    SessionManagement session;
    AlertDialogManager alert = new AlertDialogManager();

    private static ArrayList<RouteWithStops.VehicleStop> stopMarkList;
    private SearchView searchView;

    private TextView textNoReport;
    private RecyclerView recyclerView;
    private String currentVehicleNo = null;

    public StopListFragment() {
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
        view = inflater.inflate(R.layout.fragment_stoplist, container, false);
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
        searchAutoComplete.setHint(getResources().getString(R.string.search_address_hint));
        searchAutoComplete.setTextColor(Color.BLACK);
        /*Code for changing the search icon */
        ImageView searchIcon = searchView.findViewById(android.support.v7.appcompat.R.id.search_mag_icon);
        searchIcon.setImageResource(R.drawable.ic_search_black_24dp);
        searchView.setOnQueryTextListener(this);

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        stopMarkList = new ArrayList<>();
        session = new SessionManagement(activity.getApplicationContext());
        adapter = new StopListItemAdapter(stopMarkList);
        recyclerView.setAdapter(adapter);

        // Click event for single list row
        stopListOnClickListner = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = recyclerView.getChildLayoutPosition(view);

                if (position >= 0 && currentVehicleNo != null){
                    String location = stopMarkList.get(position).location;
                    String datetime = stopMarkList.get(position).dt;
                    String lat = stopMarkList.get(position).lat;
                    String lng = stopMarkList.get(position).lng;


                    Intent routeIntent = new Intent(getActivity(),MapsActivity.class);
                    routeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    routeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


                    routeIntent.putExtra("vehicleno",currentVehicleNo);
                    routeIntent.putExtra("datetime",datetime);
                    routeIntent.putExtra("liveTrack",false);
                    routeIntent.putExtra("lat",lat);
                    routeIntent.putExtra("lng",lng);
                    routeIntent.putExtra("location",location);

                    activity.startActivity(routeIntent);
                    activity.overridePendingTransition(R.anim.left_enter, R.anim.right_out);
                }
            }
        };

        currentVehicleNo = getArguments().getString("vehicleno");
        String unitid = getArguments().getString("unitid");
        String start = getArguments().getString("startdate");
        String end = getArguments().getString("enddate");
        ((DashboardActivity)getActivity()).setNavItemIndex(20, Constants.TAG_STOP_LIST);
        ((DashboardActivity)getActivity()).setToolBarTitle(currentVehicleNo + " Stops");
        checkIfWithinWeek(unitid,start,end);
        return view;
    }

    private void initView() {
        searchView = view.findViewById(R.id.stop_search);
        recyclerView = view.findViewById(R.id.stoplist_recycler_view);
        textNoReport = view.findViewById(R.id.text_no_report);
        activity = getActivity();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_alert_fragment, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onResume() {
        super.onResume();
        recyclerView.requestFocus();
    }

    private void checkIfWithinWeek(String unitid,String start, String end) {
        try {
            long duration = Utils.getTimeDifference(start,end);
            if (TimeUnit.MILLISECONDS.toDays(duration) > 7){
                new CustomToast().Show_Toast(getActivity(), view,
                        "Only 7 days alerts Available");
            }else {
                getAllVehicleStops(unitid,start,end);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void getAllVehicleStops(String unitid, String start, String end) {
        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<ArrayList<RouteWithStops.VehicleStop>> loginResult = apiInterface.getVehicleStopMarks(unitid,start,end);

        final ProgressDialog progressDialog = new ProgressDialog(getActivity(),
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        loginResult.enqueue(new Callback<ArrayList<RouteWithStops.VehicleStop>>() {

            @Override
            public void onResponse(Call<ArrayList<RouteWithStops.VehicleStop>> call, Response<ArrayList<RouteWithStops.VehicleStop>> response) {
                stopMarkList = response.body();

                if (!response.isSuccessful()) {
                    AlertDialog.Builder dialog = alert.showAlertDialog(getActivity(), "Connection Problem..", "OOPs! Check your Network Connection", "fail");
                    dialog.setNegativeButton("OK", null);
                    dialog.show();
                }else if (stopMarkList.isEmpty()){
                    textNoReport.setVisibility(View.VISIBLE);
                }
                else {
                    textNoReport.setVisibility(View.GONE);
                    adapter.updateStopList(stopMarkList);
                }

                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ArrayList<RouteWithStops.VehicleStop>> call, Throwable t) {
                progressDialog.dismiss();
                toastMessage(t.getMessage());
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
