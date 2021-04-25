package com.bharattracking.bharatracking;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.bharattracking.bharatracking.APIresponses.VehicleLiveData;
import com.bharattracking.bharatracking.fragments.RecordFragment;
import com.bharattracking.bharatracking.utilities.DrawableHelper;
import com.bharattracking.bharatracking.utilities.Utils;
import com.devs.vectorchildfinder.VectorChildFinder;
import com.devs.vectorchildfinder.VectorDrawableCompat;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.RecordViewHolder> implements Filterable {

    private ArrayList<VehicleLiveData> recordDataSet,filteredList;
    private VehicleFilter vehicleFilter;
    private Activity activity;
    private String appliedFilter = null;

    public static class RecordViewHolder extends RecyclerView.ViewHolder {

        public TextView location_info;
        TextView updated_time;
        TextView vehicle_no,ignTime,signalText,speedText,odometer;
        ImageView vehicleIcon;
        ImageView moreOptions;
        ImageView ignImage,signalImage,odomIcon;

        RecordViewHolder(View itemView) {
            super(itemView);
            this.vehicleIcon = itemView.findViewById(R.id.vehicle_icon);
            this.vehicle_no = itemView.findViewById(R.id.vehicle_no);
            this.updated_time =  itemView.findViewById(R.id.updated_time);
            this.location_info = itemView.findViewById(R.id.location_info);
            this.moreOptions = itemView.findViewById(R.id.more_options);
            this.ignImage = itemView.findViewById(R.id.ignImage);
            this.ignTime = itemView.findViewById(R.id.ignTime);
            this.signalImage = itemView.findViewById(R.id.signalImage);
            this.signalText = itemView.findViewById(R.id.signal_msg);
            this.speedText = itemView.findViewById(R.id.speedometer);
            this.odometer = itemView.findViewById(R.id.odometer);
            this.odomIcon = itemView.findViewById(R.id.odom_icon);
        }
    }

    public RecordAdapter(Activity activity, ArrayList<VehicleLiveData> data) {
        setItems(activity, data);
    }

    public void updateLiveData(ArrayList<VehicleLiveData> newData){
        assignData(newData);
        this.notifyDataSetChanged();
    }

    private void assignData(ArrayList<VehicleLiveData> newData) {
        recordDataSet = newData;
        filteredList = getFilteredResult(appliedFilter);
    }

    @Override
    public RecordAdapter.RecordViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.record_card_layout, parent, false);

        view.setOnClickListener(RecordFragment.recordOnClickListener);

        return new RecordAdapter.RecordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecordAdapter.RecordViewHolder holder, final int listPosition) {
        VectorChildFinder vector;
        VectorDrawableCompat.VFullPath path;
        int vehicleColor = 0;

        VehicleLiveData vehicle = filteredList.get(listPosition);

        holder.vehicle_no.setText(vehicle.vehicleno);
        holder.updated_time.setText(vehicle.dt);
        holder.location_info.setText(vehicle.location);
        switch (vehicle.category){
            case "running":
                holder.vehicle_no.setTextColor(activity.getResources().getColor(R.color.running_color));
                vehicleColor = R.color.running_color;
                break;
            case "stopped":
                holder.vehicle_no.setTextColor(activity.getResources().getColor(R.color.stopped_color));
                vehicleColor = R.color.stopped_color;
                break;
            case "dormant":
                holder.vehicle_no.setTextColor(activity.getResources().getColor(R.color.dormant_color));
                vehicleColor = R.color.dormant_color;
                break;
            case "not working":
                holder.vehicle_no.setTextColor(activity.getResources().getColor(R.color.nworking_color));
                vehicleColor = R.color.nworking_color;
                break;
        }

        vector = new VectorChildFinder(activity,Utils.vehicleIcon[Integer.parseInt(vehicle.vtype)],holder.vehicleIcon);
        path = vector.findPathByName("body");
        path.setFillColor(activity.getResources().getColor(vehicleColor));

        holder.moreOptions.setOnClickListener(RecordFragment.optionsOnClickListner);
        int ignColor;
        if(vehicle.ign.equals("1")){
            ignColor = R.color.running_color;
        }else {
            ignColor = R.color.dormant_color;
        }
        vector = new VectorChildFinder(activity,R.drawable.ic_car_key_red,holder.ignImage);
        path = vector.findPathByName("body");
        path.setFillColor(activity.getResources().getColor(ignColor));
        path = vector.findPathByName("legs");
        path.setFillColor(activity.getResources().getColor(ignColor));
        try {
            long duration = Utils.getTimeDifference(vehicle.igntime,vehicle.dt);
            long diffInHours = TimeUnit.MILLISECONDS.toHours(duration);
            long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(duration)%60;

            String time = String.format(Locale.getDefault(),"%dh %dm", diffInHours, diffInMinutes);
            holder.ignTime.setText(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String[] signal_msgs = activity.getResources().getStringArray(R.array.signal_msg);
        int signalVal = Integer.parseInt(vehicle.signal);
        //TODO signal divide 8 removal
        holder.signalText.setText(signal_msgs[signalVal > 4 ? signalVal/8 : signalVal]);
        holder.signalText.setTextColor(signalVal > 0 ? Color.parseColor("#808080") : Color.RED);

        vector = new VectorChildFinder(activity,R.drawable.ic_wifi,holder.signalImage);
        for(int i=0;i<Constants.SIGNAL_LEVELS;i++) {
            path = vector.findPathByName("level" + i);
            if(i < signalVal){
                path.setFillColor(activity.getResources().getColor(R.color.signal_on));
            }else {
                path.setFillColor(activity.getResources().getColor(R.color.signal_off));
            }
        }

        String speed = vehicle.speed + " km/h";
        holder.speedText.setText(speed);

        if (vehicle.odom == null || vehicle.odom.isEmpty()){
            holder.odomIcon.setVisibility(View.GONE);
            holder.odometer.setVisibility(View.GONE);
        }else {
            holder.odomIcon.setVisibility(View.VISIBLE);
            holder.odometer.setVisibility(View.VISIBLE);
            String odom = vehicle.odom + " km";
            holder.odometer.setText(odom);
        }
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    @Override
    public Filter getFilter() {
        if (vehicleFilter == null){
            vehicleFilter = new VehicleFilter();
        }
        return vehicleFilter;
    }

    public void filter(String filterText){
        appliedFilter = filterText;
        getFilter().filter(filterText);
    }

    private void setItems(Activity activity, ArrayList<VehicleLiveData> alerts) {
        this.activity = activity;
        this.recordDataSet = alerts;
        this.filteredList = alerts;
    }

    private ArrayList<VehicleLiveData> getFilteredResult(CharSequence constraint) {
        ArrayList<VehicleLiveData> tempList = new ArrayList<>();
        if (constraint == null || constraint.length() == 0) {
            return recordDataSet;
        }
        String label = Constants.KEY_VEHICLE_NO;
        if (constraint.toString().contains("status:")){
            constraint = constraint.subSequence(7,constraint.length());
            label = Constants.KEY_CATEGORY;
        }

        // search content in vehicle list
        if(label.equals(Constants.KEY_VEHICLE_NO)){
            for (VehicleLiveData vehicle : recordDataSet) {
                if (vehicle.vehicleno.toLowerCase().contains(constraint.toString().toLowerCase())) {
                    tempList.add(vehicle);
                }
            }
        }else {
            for (VehicleLiveData vehicle : recordDataSet) {
                if (vehicle.category.toLowerCase().contains(constraint.toString().toLowerCase())) {
                    tempList.add(vehicle);
                }
            }
        }
        return tempList;
    }

    /**
     * Custom filter for vehicle list
     * Filter content in vehicle list according to the search text
     */
    private class VehicleFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            if (constraint != null && constraint.length() > 0) {
                ArrayList<VehicleLiveData> tempList = getFilteredResult(constraint);

                filterResults.count = tempList.size();
                filterResults.values = tempList;
            } else {
                filterResults.count = recordDataSet.size();
                filterResults.values = recordDataSet;
            }

            return filterResults;
        }

        /**
         * Notify about filtered list to ui
         * @param constraint text
         * @param results filtered result
         */
        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredList = (ArrayList<VehicleLiveData>) results.values;
            notifyDataSetChanged();
        }
    }

}
