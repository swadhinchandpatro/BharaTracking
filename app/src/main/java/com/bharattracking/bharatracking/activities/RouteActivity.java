package com.bharattracking.bharatracking.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bharattracking.bharatracking.APIresponses.RouteWithStops;
import com.bharattracking.bharatracking.APIresponses.VehicleLiveData;
import com.bharattracking.bharatracking.CustomToast;
import com.bharattracking.bharatracking.R;
import com.bharattracking.bharatracking.RecordDataHolder;
import com.bharattracking.bharatracking.fragments.AlertFragment;
import com.bharattracking.bharatracking.interfaces.APIInterface;
import com.bharattracking.bharatracking.utilities.APIClient;
import com.bharattracking.bharatracking.utilities.AlertDialogManager;
import com.bharattracking.bharatracking.utilities.Utils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.GeoApiContext;
import com.kunzisoft.switchdatetime.SwitchDateTimeDialogFragment;

import org.joda.time.DateTime;

import java.text.DecimalFormat;
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

import static com.bharattracking.bharatracking.fragments.AlertFragment.TAG_DATETIME_FRAGMENT;

public class RouteActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener, GoogleMap.OnMarkerClickListener {

    private static final String TAG = AlertFragment.class.getSimpleName();

    private GoogleMap mMap;

    private GeoApiContext mContext;
    private BottomSheetBehavior bottomSheetBehavior = null;
    private Spinner mapTypeSpinner;
    private Toolbar toolbar;
    private UiSettings mMapSetting;
    private ImageView prevStopBtn,nextStopBtn;
    private LinearLayout bottomSheet;
    private TextView location_info;
    private Polyline polyline = null;
    private Marker startMarker,endMarker;
    private TextView goToFirstStop;

    private DateTime curDateTimeObj;
    private Button start_date,end_date;
    private Button applyChangeBtn;
    private SimpleDateFormat myDateFormat;
    private SwitchDateTimeDialogFragment startDateTimeFragment,endDateTimeFragment;
    private Intent routeIntent;

    private TextView stopStartDate,stopEndDate,tripStartDist,lastStopDist;

    private String unitId,vehicleNo,vType;
    private String curDateTime;
    private LatLng[] latLngs;
    private ArrayList<VehicleLiveData> vehicleList;
    private ArrayList<Marker> stopMarkers = new ArrayList<>();
    private int currentStopPos = -1;
    private float previousZoom;
    public static DecimalFormat distanceFormat = new DecimalFormat("0.##");
    private RouteWithStops route;
    private Date startDate;

    AlertDialogManager alert = new AlertDialogManager();
//    private ArrayList<com.google.maps.model.LatLng> gmsPoints = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);
        // Obtain the MapFragment and get notified when the map is ready to be used.
        retrieveData();
        initView();

        mContext = new GeoApiContext.Builder().apiKey(getString(R.string.google_maps_web_services_key)).build();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                super.onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void retrieveData() {
        routeIntent = getIntent();
        vehicleList = RecordDataHolder.getInstance().getData();

        if(routeIntent.hasExtra("vehicle_no")){
            vehicleNo = routeIntent.getStringExtra("vehicle_no");
            for (VehicleLiveData vehicle : vehicleList){
                if(vehicle.vehicleno.equals(vehicleNo)) {
                    unitId = vehicle.unitid;
                    vType = vehicle.vtype;
                    break;
                }
            }
        }
    }

    private void initView() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mapTypeSpinner = findViewById(R.id.mapTypeSpinner);
        ArrayAdapter<String> mapAdapter= new ArrayAdapter<>(this,android.
                R.layout.simple_spinner_dropdown_item ,MapsActivity.mapTypes);

        mapTypeSpinner.setAdapter(mapAdapter);

        goToFirstStop = findViewById(R.id.go_to_first_stop);
        goToFirstStop.setOnClickListener(this);

        setUpDateTimePicker();
        getAllStops(true);

        createBottomSheet();

        applyChangeBtn = findViewById(R.id.applyChangeBtn);

        applyChangeBtn.setOnClickListener(this);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map_with_route);
        mapFragment.getMapAsync(this);
    }

    private void getAllStops(boolean defaultRange) {
        final String start,end;
        if(defaultRange){
            start = curDateTime.substring(0,curDateTime.indexOf(' '))+" "+"00:00";
            end = curDateTime;
        }else {
            start = start_date.getText().toString();
            end = end_date.getText().toString();
        }
        try {
            String inValidMsg = validateDates(start, end);
            if (!Boolean.valueOf(inValidMsg)) {
                new CustomToast(RouteActivity.this).Show_Toast(RouteActivity.this,null,inValidMsg,Toast.LENGTH_LONG);
                return;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            new CustomToast(RouteActivity.this).Show_Toast(RouteActivity.this,null,"Please Try Again...", Toast.LENGTH_LONG);
            return;
        }

        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<RouteWithStops> loginResult = apiInterface.getRouteWithStops(unitId,start,end);

        final ProgressDialog progressDialog = new ProgressDialog(this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        loginResult.enqueue(new Callback<RouteWithStops> () {

            @Override
            public void onResponse(Call<RouteWithStops> call, Response<RouteWithStops> response) {
                route = response.body();

                if (!response.isSuccessful()) {
                    AlertDialog.Builder dialog = alert.showAlertDialog(RouteActivity.this, "Connection Problem..", "OOPs! Check your Network Connection", "fail");
                    dialog.setNegativeButton("OK",null);
                    dialog.show();
                } else if (route.locationPoints.size() == 0){
                    new CustomToast(RouteActivity.this).Show_Toast(RouteActivity.this,null,"No Route Found in the Time Range",Toast.LENGTH_LONG);
                } else {
                    mMap.clear();
                    toolbar.setTitle(vehicleNo);
                    LatLngBounds.Builder bounds = new LatLngBounds.Builder();

                    int i =0;
                    latLngs = new LatLng[route.locationPoints.size()];
                    for (RouteWithStops.LocationPoints points : route.locationPoints){
                        double latitude = Double.parseDouble(points.lat);
                        double longitude = Double.parseDouble(points.lng);
                        LatLng position = new LatLng(latitude, longitude);
//                        com.google.maps.model.LatLng point = new com.google.maps.model.LatLng(latitude, longitude);
//                        gmsPoints.add(point);
                        latLngs[i++] = position;
                        bounds.include(position);
                    }

                    addRouteEndPoints(latLngs[0],latLngs[latLngs.length -1]);

                    polyline = mMap.addPolyline(new PolylineOptions()
                            .add(latLngs)
                            .geodesic(true)
                            .color(getResources().getColor(R.color.route_color))
                            .width(12f)
                            .zIndex(100)
                            .jointType(JointType.ROUND)
                    );
//                    mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
//                        @Override
//                        public void onCameraIdle() {
//                            float zoomLevel = mMap.getCameraPosition().zoom;
//                            if (previousZoom <= 14 && zoomLevel > 14){
//                                for(LatLng latLng : markers.keySet()){
//                                    markers.get(latLng).setVisible(true);
//                                }
//                            } else if (previousZoom > 14 && zoomLevel <= 14){
//                                for(LatLng latLng : markers.keySet()){
//                                    markers.get(latLng).setVisible(false);
//                                }
//                            }
//                            previousZoom = zoomLevel;
//                        }
//                    });
                    if (route.vehicleStops.size() == 0){
                        new CustomToast(RouteActivity.this).Show_Toast(RouteActivity.this,null,"No Stops Found in the Time Range",Toast.LENGTH_LONG);
                    } else {
                        int zIndex = 100;
                        i = 1;
                        stopMarkers.clear();
                        for (RouteWithStops.VehicleStop stop : route.vehicleStops){
                            LatLng latLng = new LatLng(Double.parseDouble(stop.lat),Double.parseDouble(stop.lng));
                            Marker marker = addNewMarker(latLng,zIndex,null);
                            marker.setIcon(BitmapDescriptorFactory.fromBitmap(Utils.writeTextOnDrawable(getApplicationContext(),R.drawable.stop_marker,String.valueOf(i), R.color.white)));
                            marker.setTitle(stop.duration);
                            marker.setTag(String.valueOf(i));
                            stopMarkers.add(marker);
                            zIndex++;
                            i++;
                        }

                        showGoToFirstStopBtn();
                    }

                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(),70));
                }


                //dismiss progress Dialog
                if (progressDialog.isShowing()){
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call<RouteWithStops> call, Throwable t) {
                //dismiss progress Dialog
                if (progressDialog.isShowing()){
                    progressDialog.dismiss();
                }
            }
        });
    }

    private void showGoToFirstStopBtn() {
        goToFirstStop.setVisibility(View.VISIBLE);
    }

    private void hideGoToFirstStopBtn() {
        goToFirstStop.setVisibility(View.GONE);
    }

    private String validateDates(String start, String end) throws ParseException {
        Date date1 = null,date2 = null;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        date1 = format.parse(start);
        date2 = format.parse(end);

        if (date1.compareTo(date2) > 0) {
            return "Start Date is Greater than the End Date";
        }
//        if (Utils.getTimeDifferenceFromNow(start, TimeUnit.DAYS) > 7){
//            new CustomToast(RouteActivity.this).Show_Toast(RouteActivity.this,null,"Find Route between last 7 days",Toast.LENGTH_LONG);
//            return "Search Available within a week";
//        }
        return "true";
    }

    private void addRouteEndPoints(LatLng startPoint , LatLng endPoint) {
        startMarker = addNewMarker(startPoint,10000,null);
        endMarker = addNewMarker(endPoint,10000,null);

        startMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.start_marker));
        startMarker.setTitle("Start");
        startMarker.setAnchor(0,1.0f);
        startMarker.setTag("0");
        startMarker.setFlat(false);
        startMarker.setAlpha(1.0f);

        endMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.end_marker));
        endMarker.setTitle("End");
        endMarker.setAnchor(0,1.0f);
        endMarker.setTag(Integer.toString(route.vehicleStops.size()));
        endMarker.setFlat(false);
        endMarker.setAlpha(1.0f);

    }

    private Marker addNewMarker(LatLng latLng,float z_index, String rotation){
        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .draggable(false)
                .alpha(0.7f)
                .zIndex(z_index)
                .flat(false)
        );

        marker.setAnchor(0.5f,0.5f);

        if(rotation != null){
            float angle = Float.parseFloat(rotation);
            marker.setRotation(angle);
            double x = Math.sin(-angle * Math.PI / 180) * 0.5 + 0.5;
            double y = -(Math.cos(-angle * Math.PI / 180) * 0.5 - 0.5);
            marker.setInfoWindowAnchor((float) x,(float) y);
        }

        return marker;
    }

    private void createBottomSheet() {
        if(bottomSheetBehavior == null){
            bottomSheet = findViewById(R.id.bottom_sheet_stop_detail);
            bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
            prevStopBtn = findViewById(R.id.show_prev_stop_btn);
            nextStopBtn = findViewById(R.id.show_next_stop_btn);
            location_info = findViewById(R.id.location_info);
            stopStartDate = findViewById(R.id.stop_start_date);
            stopEndDate = findViewById(R.id.stop_end_date);
            tripStartDist = findViewById(R.id.trip_start_distance);
            lastStopDist = findViewById(R.id.last_stop_distance);

            prevStopBtn.setLongClickable(true);
            nextStopBtn.setLongClickable(true);
            prevStopBtn.setOnClickListener(this);
            nextStopBtn.setOnClickListener(this);
            prevStopBtn.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (currentStopPos > 0)
                    moveMapCameraToStop(0);
                    return false;
                }
            });
            nextStopBtn.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (currentStopPos < stopMarkers.size() - 1)
                        moveMapCameraToStop(stopMarkers.size() -1);
                    return false;
                }
            });

        }
    }

    private SwitchDateTimeDialogFragment getDateTimeFragment() {
        SwitchDateTimeDialogFragment dateTimeFragment = (SwitchDateTimeDialogFragment) getSupportFragmentManager().findFragmentByTag(TAG_DATETIME_FRAGMENT);
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

    private void setUpDateTimePicker() {
        start_date = findViewById(R.id.start_date);
        end_date = findViewById(R.id.end_date);
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
                    startDateTimeFragment.show(getSupportFragmentManager(), TAG_DATETIME_FRAGMENT);
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
                DateTime dateTime = new DateTime(startDate);
                endDateTimeFragment.startAtCalendarView();
                endDateTimeFragment.setDefaultDay(dateTime.getDayOfMonth());
                endDateTimeFragment.setDefaultMonth(dateTime.getMonthOfYear() -1);
                endDateTimeFragment.show(getSupportFragmentManager(), TAG_DATETIME_FRAGMENT);
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

    @Override
    public boolean onMarkerClick(Marker marker) {
        String title = marker.getTitle();
        if (title.matches("(\\d+)h(\\d+)m")){
            if(goToFirstStop.getVisibility() == View.VISIBLE){
                goToFirstStop.setVisibility(View.GONE);
            }
            moveMapCameraToStop(Integer.parseInt(marker.getTag().toString()) -1);
        }
        return false;
    }

    /**
     * Snaps the points to their most likely position on roads using the Roads API.
     */
//    private ArrayList<SnappedPoint> snapToRoads(GeoApiContext context) throws Exception {
//        ArrayList<SnappedPoint> snappedPoints = new ArrayList<>();
//
//        int offset = 0;
//        while (offset < points.size()) {
//            // Calculate which points to include in this request. We can't exceed the API's
//            // maximum and we want to ensure some overlap so the API can infer a good location for
//            // the first few points in each request.
//            if (offset > 0) {
//                offset -= Utils.PAGINATION_OVERLAP;   // Rewind to include some previous points.
//            }
//            int lowerBound = offset;
//            int upperBound = Math.min(offset + Utils.PAGE_SIZE_LIMIT, points.size());
//
//            // Get the data we need for this page.
//            com.google.maps.model.LatLng[] page = points.subList(lowerBound, upperBound)
//                    .toArray(new com.google.maps.model.LatLng[upperBound - lowerBound]);
//            // Perform the request. Because we have interpolate=true, we will get extra data points
//            // between our originally requested path. To ensure we can concatenate these points, we
//            // only start adding once we've hit the first new point (that is, skip the overlap).
//            SnappedPoint[] points = RoadsApi.snapToRoads(context,true,page).await();
//            boolean passedOverlap = false;
//            for (SnappedPoint point : points) {
//                if (offset == 0 || point.originalIndex >= Utils.PAGINATION_OVERLAP - 1) {
//                    passedOverlap = true;
//                }
//                if (passedOverlap) {
//                    snappedPoints.add(point);
//                }
//            }
//
//            offset = upperBound;
//        }
//
//        return snappedPoints;
//    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the MapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMapSetting = mMap.getUiSettings();
        mMapSetting.setZoomControlsEnabled(true);
        mMapSetting.setMapToolbarEnabled(true);
        mMapSetting.setCompassEnabled(true);
        mMap.setPadding(0,100,0,50);
        mMap.setOnMarkerClickListener(this);

        mapTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                int pos = mapTypeSpinner.getSelectedItemPosition();
                switch (MapsActivity.mapTypes[pos]){
                    case "Normal":
                        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        break;
                    case "Hybrid":
                        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        break;
                    case "Satellite":
                        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState){
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        mMap.setPadding(0,100,0,550);
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                transitionBottomSheetBackgroundColor(slideOffset);
                animateBottomSheetArrows(slideOffset);
            }
        });
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.applyChangeBtn:
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                getAllStops(false);
                break;
            case R.id.go_to_first_stop:
                moveMapCameraToStop(0);
                hideGoToFirstStopBtn();
                break;
            case R.id.show_prev_stop_btn:
                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED){
                    if (currentStopPos > 0){
                        moveMapCameraToStop(currentStopPos - 1);
                    }else {
                        new CustomToast(RouteActivity.this).Show_Toast(RouteActivity.this,null,"You have reached the Start Point",Toast.LENGTH_SHORT);
                    }
                }else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
                break;
            case R.id.show_next_stop_btn:
                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED){
                    if (currentStopPos < stopMarkers.size() - 1){
                        moveMapCameraToStop(currentStopPos + 1);
                    }else {
                        new CustomToast(RouteActivity.this).Show_Toast(RouteActivity.this,null,"You have reached the End Point",Toast.LENGTH_SHORT);
                    }
                }else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
                break;
        }
    }

    private void animateBottomSheetArrows(float slideOffset) {
        // Animate counter-clockwise
        prevStopBtn.setRotation(slideOffset * -90);
        // Animate clockwise
        nextStopBtn.setRotation(slideOffset * 90);
    }

    private void transitionBottomSheetBackgroundColor(float slideOffset) {
        int colorFrom = getResources().getColor(R.color.bottomsheet_dark);
        int colorTo = getResources().getColor(R.color.bottomsheet_light);
        bottomSheet.setBackgroundColor(interpolateColor(slideOffset,
                colorFrom, colorTo));
    }

    /**
     * This function returns the calculated in-between value for a color
     * given integers that represent the start and end values in the four
     * bytes of the 32-bit int. Each channel is separately linearly interpolated
     * and the resulting calculated values are recombined into the return value.
     *
     * @param fraction The fraction from the starting to the ending values
     * @param startValue A 32-bit int value representing colors in the
     * separate bytes of the parameter
     * @param endValue A 32-bit int value representing colors in the
     * separate bytes of the parameter
     * @return A value that is calculated to be the linearly interpolated
     * result, derived by separating the start and end values into separate
     * color channels and interpolating each one separately, recombining the
     * resulting values in the same way.
     */
    private int interpolateColor(float fraction, int startValue, int endValue) {
        int startA = (startValue >> 24) & 0xff;
        int startR = (startValue >> 16) & 0xff;
        int startG = (startValue >> 8) & 0xff;
        int startB = startValue & 0xff;
        int endA = (endValue >> 24) & 0xff;
        int endR = (endValue >> 16) & 0xff;
        int endG = (endValue >> 8) & 0xff;
        int endB = endValue & 0xff;
        return ((startA + (int) (fraction * (endA - startA))) << 24) |
                ((startR + (int) (fraction * (endR - startR))) << 16) |
                ((startG + (int) (fraction * (endG - startG))) << 8) |
                ((startB + (int) (fraction * (endB - startB))));
    }

    //position starts from 0
    private void moveMapCameraToStop(int stopPosition) {
        Marker currentMarker = stopMarkers.get(stopPosition);
        RouteWithStops.VehicleStop stop = route.vehicleStops.get(stopPosition);
        toolbar.setSubtitle(stop.dt);

        location_info.setText(stop.location);
        stopStartDate.setText(stop.dt);
        stopEndDate.setText(stop.end_dt);
        tripStartDist.setText(stop.trip_distance + " km");
        if (stopPosition == 0){
            lastStopDist.setText("0 km");
        }else {
            Double prevTripDistance = Double.parseDouble(route.vehicleStops.get(stopPosition -1).trip_distance);
            Double currentTripDistance = Double.parseDouble(stop.trip_distance);
            lastStopDist.setText(distanceFormat.format(currentTripDistance - prevTripDistance) + " km");
        }

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentMarker.getPosition(),16.0f));
        currentMarker.showInfoWindow();

        if(bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED){
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }

        currentStopPos = stopPosition;
    }
    //TODO
    private void moveMapCameraToRouteEnd() {

    }
    //TODO
    private void moveCameraToRouteStart() {

    }

    /** Helper for toasting exception messages on the UI thread. */
    private void toastMessage(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),msg, Toast.LENGTH_LONG).show();
            }
        });
    }
}
