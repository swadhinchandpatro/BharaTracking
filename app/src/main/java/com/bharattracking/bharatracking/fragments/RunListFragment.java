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
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bharattracking.bharatracking.Constants;
import com.bharattracking.bharatracking.CustomToast;
import com.bharattracking.bharatracking.R;
import com.bharattracking.bharatracking.session.SessionManagement;
import com.bharattracking.bharatracking.utilities.Utils;
import com.bharattracking.bharatracking.activities.MapsActivity;
import com.bharattracking.bharatracking.utilities.AlertDialogManager;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class RunListFragment extends Fragment implements SearchView.OnQueryTextListener {

    private static final String TAG = RunListFragment.class.getSimpleName();

    private View view;
    private Activity activity;

    private static RunListItemAdapter adapter;

    SessionManagement session;
    AlertDialogManager alert = new AlertDialogManager();

    private static ArrayList<HashMap<String,String>> runMarkList;
    private SearchView searchView;

    private TextView textNoReport;
    private ListView run_list;

    public RunListFragment() {
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
        view = inflater.inflate(R.layout.fragment_runlist, container, false);
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

        runMarkList = new ArrayList<>();
        session = new SessionManagement(activity.getApplicationContext());
        adapter=new RunListItemAdapter(getActivity(), runMarkList);
        run_list.setAdapter(adapter);
        run_list.setTextFilterEnabled(true);

        // Click event for single list row
        run_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick (AdapterView<?> parent, View view,
                                     int position, long id) {
                if(position>0 && position <= runMarkList.size()) {
                    // close search view if its visible
                    if (searchView.isShown()) {
                        searchView.setQuery("", false);
                    }
                }

                String vehicleNo = runMarkList.get(position).get(Constants.KEY_VEHICLE_NO);
                String location = runMarkList.get(position).get(Constants.KEY_LOCATION_INFO);
                String datetime = runMarkList.get(position).get(Constants.KEY_LAST_UPDATED_TIME);
                String lat = runMarkList.get(position).get(Constants.KEY_LAT);
                String lng = runMarkList.get(position).get(Constants.KEY_LNG);


                Intent routeIntent = new Intent(getActivity(),MapsActivity.class);
                routeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                routeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


                routeIntent.putExtra("vehicleno",vehicleNo);
                routeIntent.putExtra("datetime",datetime);
                routeIntent.putExtra("liveTrack",false);
                routeIntent.putExtra("lat",lat);
                routeIntent.putExtra("lng",lng);
                routeIntent.putExtra("location",location);

                activity.startActivity(routeIntent);
                activity.overridePendingTransition(R.anim.left_enter, R.anim.right_out);
            }
        });

        String vehicleNo = getArguments().getString("vehicleno");
        String start = getArguments().getString("startdate");
        String end = getArguments().getString("enddate");
        checkIfWithinWeek(vehicleNo,start,end);
        return view;
    }

    private void initView() {
        searchView = view.findViewById(R.id.run_search);
        run_list = view.findViewById(R.id.run_item_list);
        textNoReport = view.findViewById(R.id.text_no_report);
        activity = getActivity();
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
        run_list.requestFocus();
    }

    private void checkIfWithinWeek(String vehicleNo,String start, String end) {
        try {
            long duration = Utils.getTimeDifference(start,end);
            if (TimeUnit.MILLISECONDS.toDays(duration) > 7){
                new CustomToast().Show_Toast(getActivity(), view,
                        "Only 7 days alerts Available");
            }else {
                new RunListFragment.NewAsyncTask().execute(vehicleNo,start,end);
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
        adapter.getFilter().filter(newText);
        if (TextUtils.isEmpty(newText)) {
            adapter.getFilter().filter("");
        }
        else {
            adapter.getFilter().filter(newText);
        }
        return true;
    }

    /**
     * Async task class to get json by making HTTP call
     */
    private class NewAsyncTask extends AsyncTask<String, String, String> {

        HttpURLConnection conn;
        java.net.URL url = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // showing refresh animation before making http call
        }

        @Override
        protected String doInBackground(String... params) {
            String jsonStr = null;

            try {
                // Enter URL address where your php file resides
                url = new URL("http://bharattracking.com/reports/getRunMarks.php");
                // Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(Utils.READ_TIMEOUT);
                conn.setConnectTimeout(Utils.CONNECTION_TIMEOUT);
                conn.setRequestMethod("POST");

                // setDoInput and setDoOutput method depict handling of both send and receive
                conn.setDoInput(true);
                conn.setDoOutput(true);

                // Append parameters to URL
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("vehicleno", params[0])
                        .appendQueryParameter("start", params[1])
                        .appendQueryParameter("end",params[2]);
                String query = builder.build().getEncodedQuery();

                // Open connection for sending data
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
                conn.connect();

            } catch (MalformedURLException e) {
                Log.e(TAG, "MalformedURLException: " + e.getMessage());
                toastMessage("exception" + e.getMessage());
                return "exception";
            } catch (ProtocolException e) {
                Log.e(TAG, "ProtocolException: " + e.getMessage());
                toastMessage("exception" + e.getMessage());
                return "exception";
            } catch (IOException e) {
                Log.e(TAG, "IOException: " + e.getMessage());
                toastMessage("exception" + e.getMessage());
                return "exception";
            } catch (Exception e) {
                Log.e(TAG, "Exception: " + e.getMessage());
                toastMessage("exception" + e.getMessage());
                return "exception";
            }

            try {

                int response_code = conn.getResponseCode();

                // Check if successful connection made
                if (response_code == HttpURLConnection.HTTP_OK) {
                    // Read data sent from server
                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();
                    Log.d(TAG, "The result is" + result.toString());
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                        Log.i(TAG, "The line is " + line.trim());
                    }
                    // Assign data to jsonStr
                    jsonStr = result.toString();
                } else {
                    return ("unsuccessful");
                }
            } catch (IOException e) {
                //e.printStackTrace();
                Log.e(TAG, "exception", e);
                toastMessage("exception" + e.getMessage());
                return "exception";
            } finally {
                conn.disconnect();
            }

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    if (!jsonObj.getString("status").equals("okay")) {
                        return "exception";
                    }
                    Log.i(TAG, jsonObj.getJSONArray("json").toString());
                    JSONArray reports = jsonObj.getJSONArray("json");
                    runMarkList.clear();
                    // looping through All Alerts
                    for (int i = 0; i < reports.length(); i++) {
                        JSONObject c = reports.getJSONObject(i);

                        String vehicleno = c.getString("vehicleno");
                        String category = c.getString("category");
                        String duration = c.getString("duration");
                        String date = c.getString("dt");
                        String location = c.getString("location");
                        String lat = c.getString("lat");
                        String lng = c.getString("lng");
                        String dir = c.getString("dir");
                        String thumb_url = c.getString("thumb_url");

                        HashMap<String , String> run = new HashMap<>();

                        run.put(Constants.KEY_VEHICLE_NO, vehicleno);
                        run.put(Constants.KEY_CATEGORY,category);
                        run.put(Constants.KEY_DURATION,duration);
                        run.put(Constants.KEY_LAST_UPDATED_TIME,date);
                        run.put(Constants.KEY_LOCATION_INFO,location);
                        run.put(Constants.KEY_LAT,lat);
                        run.put(Constants.KEY_LNG,lng);
                        run.put(Constants.KEY_DIR,dir);
                        run.put(Constants.KEY_THUMB_URL,thumb_url);

                        runMarkList.add(run);
                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity().getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity().getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

            }

            return "success";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result.equalsIgnoreCase("exception") || result.equalsIgnoreCase("unsuccessful")) {
                AlertDialog.Builder dialog = alert.showAlertDialog(getActivity(), "Connection Problem..", "OOPs! Check your Network Connection", "fail");
                dialog.setNegativeButton("OK",null);
                dialog.show();
            }
            else if (runMarkList.isEmpty()){
                textNoReport.setVisibility(View.VISIBLE);
            }
            else {
                textNoReport.setVisibility(View.GONE);
                adapter.notifyDataSetChanged();
            }
        }
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
