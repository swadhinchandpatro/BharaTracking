package com.bharattracking.bharatracking.activities;


import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bharattracking.bharatracking.APIresponses.VehicleLiveData;
import com.bharattracking.bharatracking.Constants;
import com.bharattracking.bharatracking.CustomToast;
import com.bharattracking.bharatracking.DialogWithInput;
import com.bharattracking.bharatracking.R;
import com.bharattracking.bharatracking.RecordDataHolder;
import com.bharattracking.bharatracking.session.SessionManagement;
import com.bharattracking.bharatracking.interfaces.APIInterface;
import com.bharattracking.bharatracking.interfaces.LatLngInterpolator;
import com.bharattracking.bharatracking.utilities.APIClient;
import com.bharattracking.bharatracking.utilities.AlertDialogManager;
import com.bharattracking.bharatracking.utilities.MarkerAnimation;
import com.bharattracking.bharatracking.utilities.Utils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CustomCap;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.GeoApiContext;
import com.marcinmoskala.arcseekbar.ArcSeekBar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.bharattracking.bharatracking.Constants.*;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,GoogleMap.OnMarkerClickListener, View.OnClickListener, DialogWithInput.InputCollection {

    private static final String TAG = MapsActivity.class.getSimpleName();
    private GoogleMap mMap;

    private GeoApiContext mContext;
    private BottomSheetBehavior bottomSheetBehavior;
    private Spinner mapTypeSpinner,chooseVehicleSpinner;
    private ImageButton closeBottomSheet;

    public static final String[] mapTypes = {"Normal","Hybrid","Satellite"};
    private static final double LN2 = 0.6931471805599453;
    private static final int WORLD_PX_HEIGHT = 256;
    private static final int WORLD_PX_WIDTH = 256;
    private static final int ZOOM_MAX = 21;

    public static final int PATTERN_DASH_LENGTH_PX = 20;
    public static final int PATTERN_GAP_LENGTH_PX = 20;
    public static final PatternItem DOT = new Dot();
    public static final PatternItem DASH = new Dash(PATTERN_DASH_LENGTH_PX);
    public static final PatternItem GAP = new Gap(PATTERN_GAP_LENGTH_PX);
    public static final List<PatternItem> PATTERN_POLYGON_ALPHA = Arrays.asList(GAP, DASH);

    AlertDialogManager alert = new AlertDialogManager();
    SessionManagement session;
    private Intent mapIntent = null;
    private Toolbar toolbar;
    private ArrayList<Marker> markers = new ArrayList<>();
    private ArrayList<LatLng> liveDataPoints = new ArrayList<>();
    private Marker currentMarker;
    private UiSettings mMapSetting;
    private ArrayList<VehicleLiveData> liveData;
    private ArcSeekBar speedArcSeekBar;
    private TextView speedText;
    private View speedLayout;
    private TextView location_info;
    private Button callDriverBtn,showRouteBtn;

    Runnable mUpdateMarker = new Runnable() {
        @Override
        public void run() {
            if (mapIntent == null || mapIntent.getBooleanExtra(TRACK_LIVE,false)){
                try {
                    fetchLiveData(Utils.DATA_REQUEST_TOKEN,session.getMobileNumber());
                }finally {
                    mHandler.postDelayed(mUpdateMarker,8000);
                }
            }
        }
    };
    private Handler mHandler;
    private ArrayList<String> vehicleNameList = new ArrayList<>();
    private ArrayAdapter<String> vehicleAdapter;
    private ArrayList<LatLng> allLiveDataPoints = new ArrayList<>();
    private Polyline liveRoute = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        initView();

        mapIntent = getIntent();
        mHandler = new Handler();

        session = new SessionManagement(getApplicationContext());
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        mContext = new GeoApiContext.Builder().apiKey(getString(R.string.google_maps_web_services_key)).build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopPeriodicUpdatedData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startPeriodicUpdatedData();
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
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mUpdateMarker.run();
            }
        },8000);
    }
    //end periodic task
    private void stopPeriodicUpdatedData() {
        mHandler.removeCallbacks(mUpdateMarker);
    }

    private void initView() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mapTypeSpinner = findViewById(R.id.mapTypeSpinner);
        ArrayAdapter<String> mapAdapter= new ArrayAdapter<String>(this,android.
                R.layout.simple_spinner_dropdown_item ,mapTypes);

        mapTypeSpinner.setAdapter(mapAdapter);

        chooseVehicleSpinner = findViewById(R.id.choose_vehicle);
        liveData = RecordDataHolder.getInstance().getData();
        vehicleNameList.add("All");
        for (VehicleLiveData vehicle : liveData){
            vehicleNameList.add(vehicle.vehicleno);
        }
        vehicleAdapter= new ArrayAdapter<String>(this,android.
                R.layout.simple_spinner_dropdown_item,vehicleNameList);
        chooseVehicleSpinner.setAdapter(vehicleAdapter);

        View bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        callDriverBtn = findViewById(R.id.call_driver_btn);
        showRouteBtn = findViewById(R.id.show_route_btn);

        callDriverBtn.setOnClickListener(this);
        showRouteBtn.setOnClickListener(this);

        closeBottomSheet = findViewById(R.id.closeSheet);
        location_info = findViewById(R.id.location_info);

        speedLayout = findViewById(R.id.speed_display);
        speedArcSeekBar = findViewById(R.id.speedSeekBar);
        speedText = findViewById(R.id.speedText);
        speedText.setSingleLine(false);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMapSetting = mMap.getUiSettings();
        mMapSetting.setZoomControlsEnabled(true);
        mMapSetting.setMapToolbarEnabled(true);
        mMapSetting.setCompassEnabled(true);
        mMap.setPadding(0,100,0,250);
        mMap.setOnMarkerClickListener(this);
        mMap.setBuildingsEnabled(true);

        mapTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                int pos = mapTypeSpinner.getSelectedItemPosition();
                switch (mapTypes[pos]){
                    case "Normal":
                        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        speedArcSeekBar.setProgressColor(getResources().getColor(R.color.gradient_one));
                        speedArcSeekBar.setProgressBackgroundColor(getResources().getColor(R.color.black));
                        speedText.setTextColor(getResources().getColor(R.color.gradient_one));
                        break;
                    case "Hybrid":
                        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        speedArcSeekBar.setProgressColor(getResources().getColor(R.color.white));
                        speedArcSeekBar.setProgressBackgroundColor(getResources().getColor(R.color.iron));
                        speedText.setTextColor(getResources().getColor(R.color.white));
                        break;
                    case "Satellite":
                        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        speedArcSeekBar.setProgressColor(getResources().getColor(R.color.white));
                        speedArcSeekBar.setProgressBackgroundColor(getResources().getColor(R.color.iron));
                        speedText.setTextColor(getResources().getColor(R.color.white));
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        chooseVehicleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                int pos = chooseVehicleSpinner.getSelectedItemPosition();
                switch (pos){
                    case 0:
                        displayAllVehicles();
                        break;
                    default:
                        showSingleVehicle(vehicleNameList.get(pos));
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
                    case BottomSheetBehavior.STATE_EXPANDED:
                        closeBottomSheet.setImageResource(R.drawable.ic_close_white_35dp);
//                        mMap.setPadding(0,0,0,450);
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
//                        mMap.setPadding(0,0,0,250);
                        break;
                    default:
                        closeBottomSheet.setImageResource(R.drawable.ic_close_white_35dp);
//                        mMap.setPadding(0,0,0,250);
                        break;
                }
//                mMap.moveCamera(CameraUpdateFactory.newLatLng(markers.get(0).getPosition()));
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        closeBottomSheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });

        if (mapIntent.hasExtra(Constants.KEY_VEHICLE_NO)){
            String vehicleno = mapIntent.getStringExtra(Constants.KEY_VEHICLE_NO);
            if (mapIntent.getBooleanExtra(TRACK_LIVE,false)){
                int pos = 0;
                for (int i = 0; i< liveData.size(); i++){
                    if(liveData.get(i).vehicleno.equals(vehicleno)){
                        pos = i;
                    }
                }
                chooseVehicleSpinner.setVisibility(View.VISIBLE);
                chooseVehicleSpinner.setSelection(pos+1);
                LatLng currentLocation = new LatLng(
                        Double.parseDouble(liveData.get(pos).lat),
                        Double.parseDouble(liveData.get(pos).lng)
                );
                allLiveDataPoints.add(currentLocation);
            }else {
                toolbar.setTitle(vehicleno);
                toolbar.setSubtitle(mapIntent.getStringExtra(KEY_LAST_UPDATED_TIME));

                VehicleLiveData vehicle = new VehicleLiveData();

                vehicle.vehicleno = vehicleno;
                vehicle.location = mapIntent.getStringExtra(KEY_LOCATION_INFO);
                vehicle.lat = mapIntent.getStringExtra(KEY_LAT);
                vehicle.lng = mapIntent.getStringExtra(KEY_LNG);
                vehicle.dt = mapIntent.getStringExtra(KEY_LAST_UPDATED_TIME);

                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                addNewVehicleMarker(builder,vehicle, null,1.0f);
                displayVehicleMarkers(builder);
                currentMarker = markers.get(0);

                location_info.setText(vehicle.location);
                speedLayout.setVisibility(View.GONE);
            }
        }else if(mapIntent.getBooleanExtra(TRACK_LIVE,false)){
            chooseVehicleSpinner.setVisibility(View.VISIBLE);
            chooseVehicleSpinner.setSelection(0);
        }
    }

    private void showSingleVehicle(String vehicleno) {
        mMap.clear();
        markers.clear();
        liveData = RecordDataHolder.getInstance().getData();
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        speedLayout.setVisibility(View.VISIBLE);
        VehicleLiveData vehicle = new VehicleLiveData();
        for (VehicleLiveData v : liveData){
            if (v.vehicleno.equals(vehicleno)){
                vehicle = v;
                break;
            }
        }

        toolbar.setTitle(vehicleno);
        toolbar.setSubtitle(vehicle.dt);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        addNewVehicleMarker(builder,vehicle, vehicle.vtype,1.0f);
        displayVehicleMarkers(builder);

        if (!markers.isEmpty()){
            currentMarker = markers.get(0);

            String speed = vehicle.speed + " km/h";
            int splitpos = vehicle.speed.length();
            SpannableString s1 = new SpannableString(speed);
            s1.setSpan(new RelativeSizeSpan(2f),0,splitpos,0);
            s1.setSpan(new StyleSpan(Typeface.BOLD),splitpos+1,speed.length(),0);

            int speedInt = Integer.parseInt(speed.replaceAll("[\\D]",""));
            speedArcSeekBar.setProgress(speedInt);
            speedText.setText(s1);

            location_info.setText(vehicle.location);
        }
    }

    private void displayAllVehicles() {
        liveData = RecordDataHolder.getInstance().getData();
        markers.clear();
        mMap.clear();
        currentMarker = null;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (VehicleLiveData v : liveData){
            addNewVehicleMarker(builder,v, v.vtype,1.0f);
        }
        displayVehicleMarkers(builder);
        toolbar.setTitle("All");
        toolbar.setSubtitle("");
        speedLayout.setVisibility(View.GONE);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    private void displayVehicleMarkers(LatLngBounds.Builder builder) {
        if (markers.size()==1){
            if (mMap.getCameraPosition() != null && mMap.getCameraPosition().zoom > 14.0f) {
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(markers.get(0).getPosition())
                        .tilt(40)
                        .zoom(mMap.getCameraPosition().zoom)
                        .build();

                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }else {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(markers.get(0).getPosition(),16.0f));
            }
        }else if (!markers.isEmpty()){
            LatLngBounds bounds = builder.build();
            int zoomCalculated = getBoundsZoomLevel(bounds, findViewById(R.id.map).getMeasuredWidth(), findViewById(R.id.map).getMeasuredHeight());
            LatLng mapCenter = bounds.getCenter();
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(mapCenter) // Center Set
                    .zoom(zoomCalculated)                // Orientation of the camera to east
                    .tilt(10)                // Tilt of the camera to 45 degrees
                    .build();                   // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 2000, new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() { }
                @Override
                public void onCancel() { }
            });
        }else {
            new CustomToast(MapsActivity.this).Show_Toast(MapsActivity.this,null,"Please Subscribe your Vehicles",Toast.LENGTH_LONG);
        }
    }

    private void addNewVehicleMarker(LatLngBounds.Builder builder, VehicleLiveData vehicle, String thumb_url,float z_index) {
        String vehicleName = vehicle.vehicleno;
        if(vehicle.lat == null || vehicle.lat.isEmpty()){
            return;
        }
        LatLng latLng = new LatLng(Double.parseDouble(vehicle.lat),Double.parseDouble(vehicle.lng));
        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(vehicleName)
                .draggable(false)
                .alpha(0.7f)
                .zIndex(z_index)
        );
        if (thumb_url!=null){
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(createStoreMarker(thumb_url)));
        }
        if(thumb_url==null || thumb_url.equals("3") || thumb_url.equals("4")){
            marker.setFlat(false);
            marker.setAlpha(1.0f);
        }
        else{
            float angle = Float.parseFloat(vehicle.dir);
            marker.setAnchor(0.5f,0.5f);
            marker.setRotation(angle);
            double x = Math.sin(-angle * Math.PI / 180) * 0.5 + 0.5;
            double y = -(Math.cos(-angle * Math.PI / 180) * 0.5 - 0.5);
            marker.setInfoWindowAnchor((float) x,(float) y);
        }
        if(builder!=null){
            markers.add(marker);
            builder.include(latLng);
        }
    }

    private int getBoundsZoomLevel(LatLngBounds bounds, int mapWidthPx, int mapHeightPx) {
        int error = 2;
        LatLng ne = bounds.northeast;
        LatLng sw = bounds.southwest;

        double latFraction = (latRad(ne.latitude) - latRad(sw.latitude)) / Math.PI;

        double lngDiff = ne.longitude - sw.longitude;
        double lngFraction = ((lngDiff < 0) ? (lngDiff + 360) : lngDiff) / 360;

        double latZoom = zoom(mapHeightPx, WORLD_PX_HEIGHT, latFraction);
        double lngZoom = zoom(mapWidthPx, WORLD_PX_WIDTH, lngFraction);

        int result = Math.min((int)latZoom, (int)lngZoom);
        return Math.min(result, ZOOM_MAX)-error;
    }

    private double latRad(double lat) {
        double sin = Math.sin(lat * Math.PI / 180);
        double radX2 = Math.log((1 + sin) / (1 - sin)) / 2;
        return Math.max(Math.min(radX2, Math.PI), -Math.PI) / 2;
    }
    private double zoom(int mapPx, int worldPx, double fraction) {
        return Math.floor(Math.log(mapPx / worldPx / fraction) / LN2);
    }

    private Bitmap createStoreMarker(String vehicleType) {
        View markerLayout = getLayoutInflater().inflate(R.layout.store_marker_layout, null);

        ImageView markerImage =  markerLayout.findViewById(R.id.marker_image);
        switch (vehicleType){
            case "0":
                markerImage.setImageResource(R.drawable.car_marker);
                break;
            case "1":
                markerImage.setImageResource(R.drawable.truck_marker);
                markerImage.setRotation(90);
                break;
            case "2":
                markerImage.setImageResource(R.drawable.bike_marker);
                break;
            case "3":
                markerImage.setImageResource(R.drawable.start_marker);
                break;
            case "4":
                markerImage.setImageResource(R.drawable.end_point);
                break;
            case "5":
                markerImage.setImageResource(R.drawable.arrowup);
                break;
        }

        markerLayout.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        markerLayout.layout(0, 0, markerLayout.getMeasuredWidth(), markerLayout.getMeasuredHeight());

        Bitmap bitmap = Bitmap.createBitmap(markerLayout.getMeasuredWidth(), markerLayout.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        markerLayout.draw(canvas);
        return bitmap;
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.map,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.show_traffic:
                if(mMap.isTrafficEnabled()){
                    mMap.setTrafficEnabled(false);
                    item.getIcon().setAlpha(255);
                }else {
                    mMap.setTrafficEnabled(true);
                    item.getIcon().setAlpha(128);
                }
                break;
            case R.id.action_logout:
                session.logoutUser(MapsActivity.this);
                break;
            case android.R.id.home:
                super.onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
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

    @Override
    public boolean onMarkerClick(Marker marker) {
        if(currentMarker!=null){
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
        return false;
    }

//    private void fetchLiveData(ArrayList<VehicleLiveData> vehicles) {
//        ArrayList<VehicleData> vehicleDatas;
//        for (int i=0; i < vehicles.size();i++){
//
//        }
//        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
//        final Call<ArrayList<VehicleLiveData>> getliveData = apiInterface.getLiveRouteData(vehicles);
//    }

    private void fetchLiveData(String dataRequestToken, String user) {
        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        final Call<ArrayList<VehicleLiveData>> getliveData = apiInterface.getLiveData(user, dataRequestToken);

        getliveData.enqueue(new Callback<ArrayList<VehicleLiveData>>() {
            @Override
            public void onResponse(Call<ArrayList<VehicleLiveData>> call, Response<ArrayList<VehicleLiveData>> response) {
                if (response.isSuccessful()) {
                    liveData = response.body();
                    if (liveData.size() > 0) {
                        vehicleNameList.clear();
                        vehicleNameList.add("All");
                        for (VehicleLiveData vehicle : liveData){
                            vehicleNameList.add(vehicle.vehicleno);
                        }
                        RecordDataHolder mHolder =  RecordDataHolder.getInstance();
                        mHolder.setData(liveData);
                        vehicleAdapter.notifyDataSetChanged();
                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        for (int i = 0; i < markers.size(); i++) {
                            for (VehicleLiveData vehicle : liveData) {
                                if (vehicle.vehicleno.equals(markers.get(i).getTitle())) {
                                    LatLng newPosition = new LatLng(Double.parseDouble(vehicle.lat),
                                            Double.parseDouble(vehicle.lng));
                                    Marker marker = markers.get(i);
                                    updateMarkerProperties(builder, vehicle, newPosition, marker);
                                    break;
                                }
                            }
                        }
                        displayVehicleMarkers(builder);
                    }
                }else {
                    AlertDialog.Builder dialog = alert.showAlertDialog(MapsActivity.this, "Connection Problem..", "OOPs! Check your Network Connection", "fail");
                    dialog.setNegativeButton("OK", null);
                    dialog.show();
                }
            }

            @Override
            public void onFailure(Call<ArrayList<VehicleLiveData>> call, Throwable t) {
                call.cancel();
            }
        });
    }

    private void updateMarkerProperties(LatLngBounds.Builder builder, VehicleLiveData vehicle, LatLng newPosition, Marker marker) {
        float angle = Float.parseFloat(vehicle.dir);
        marker.setAnchor(0.5f,0.5f);
        double x = Math.sin(-angle * Math.PI / 180) * 0.5 + 0.5;
        double y = -(Math.cos(-angle * Math.PI / 180) * 0.5 - 0.5);
        marker.setInfoWindowAnchor((float) x,(float) y);
        if(markers.size() == 1 && !toolbar.getTitle().toString().equals(vehicle.dt)){
            String speed = vehicle.speed + " km/h";
            int splitpos = vehicle.speed.length();
            SpannableString s1 = new SpannableString(speed);
            s1.setSpan(new RelativeSizeSpan(2f),0,splitpos,0);
            s1.setSpan(new StyleSpan(Typeface.BOLD),splitpos+1,speed.length(),0);

            int speedInt = Integer.parseInt(speed.replaceAll("[\\D]",""));
            speedArcSeekBar.setProgress(speedInt);
            speedText.setText(s1);
            toolbar.setSubtitle(vehicle.dt);
            location_info.setText(vehicle.location);
            allLiveDataPoints.add(newPosition);
            marker.setRotation(angle);
            MarkerAnimation.animateMarkerToGB(marker, newPosition, new LatLngInterpolator.Spherical());
            if(liveRoute == null){
                liveRoute = mMap.addPolyline(new PolylineOptions()
                        .addAll(allLiveDataPoints)
                        .geodesic(true)
                        .width(12f)
                        .zIndex(1000)
                        .color(getResources().getColor(R.color.route_color))
                        .jointType(JointType.ROUND)
                );
            }else {
                liveRoute.setPoints(allLiveDataPoints);
            }
            if (allLiveDataPoints.size() == 2) {
                liveRoute.setStartCap(new CustomCap(BitmapDescriptorFactory.fromResource(R.drawable.start_bubble)));
            }
        }else {
            marker.setPosition(newPosition);
            builder.include(newPosition);
        }
    }

    @Override
    public void onClick(View view) {
        String selectedVehicleNo = chooseVehicleSpinner.getSelectedItem().toString();
        if (selectedVehicleNo.equalsIgnoreCase("all")){
            new CustomToast(MapsActivity.this).Show_Toast(MapsActivity.this,null,"Select any one vehicle..");
            return;
        }
        switch (view.getId()){
            case R.id.call_driver_btn:
                Utils.callDriver(view,getSupportFragmentManager(),MapsActivity.this,selectedVehicleNo);
                break;
            case R.id.show_route_btn:
                Intent routeIntent = new Intent(MapsActivity.this,RouteActivity.class);
                routeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                routeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                routeIntent.putExtra("vehicle_no",selectedVehicleNo);
                startActivity(routeIntent);
                overridePendingTransition(R.anim.left_enter, R.anim.right_out);
                break;
        }
    }

    @Override
    public void getInputValue(String input, String vehicleNo) {
        Utils.updateDriverNumber(input,vehicleNo,findViewById(R.id.map),MapsActivity.this);
    }
}
