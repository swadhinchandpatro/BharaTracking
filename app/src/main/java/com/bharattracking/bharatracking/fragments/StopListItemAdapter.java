package com.bharattracking.bharatracking.fragments;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.bharattracking.bharatracking.APIresponses.RouteWithStops;
import com.bharattracking.bharatracking.R;

import java.util.ArrayList;
import java.util.Locale;

public class StopListItemAdapter extends RecyclerView.Adapter<StopListItemAdapter.StopListViewHolder> implements Filterable {

    private ArrayList<RouteWithStops.VehicleStop> stopListDataSet,filteredList;
    private LocationFilter locationFilter;

    static class StopListViewHolder extends RecyclerView.ViewHolder {

        TextView stop_count,stop_duration,location_info;
        TextView start_stoptime,end_stoptime,trip_start_dist;

        StopListViewHolder(View itemView) {
            super(itemView);

            this.stop_count = itemView.findViewById(R.id.stop_count);
            this.start_stoptime = itemView.findViewById(R.id.start_stoptime);
            this.end_stoptime = itemView.findViewById(R.id.end_stoptime);
            this.stop_duration = itemView.findViewById(R.id.stopDuration);
            this.location_info = itemView.findViewById(R.id.location_info);
            this.trip_start_dist = itemView.findViewById(R.id.dist_trip_start);
        }
    }

    StopListItemAdapter(ArrayList<RouteWithStops.VehicleStop> d) {
        stopListDataSet = d;
        filteredList = d;
    }

    @NonNull
    @Override
    public StopListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.stop_list_item, parent, false);

        view.setOnClickListener(StopListFragment.stopListOnClickListner);

        return new StopListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final StopListViewHolder holder, int position) {
        RouteWithStops.VehicleStop stop = filteredList.get(position);

        String stop_no = String.format(Locale.getDefault(),"%d",position + 1);
        holder.stop_count.setText(stop_no);
        holder.start_stoptime.setText(stop.dt);
        holder.end_stoptime.setText(stop.end_dt);
        holder.stop_duration.setText(stop.duration);
        holder.location_info.setText(stop.location);
        String dist_display = stop.trip_distance + " km ";
        holder.trip_start_dist.setText(dist_display);
    }

    public ArrayList<RouteWithStops.VehicleStop> getData(){ return stopListDataSet;}

    void updateStopList(ArrayList<RouteWithStops.VehicleStop> newData){
        assignData(newData);
        this.notifyDataSetChanged();
    }

    private void assignData(ArrayList<RouteWithStops.VehicleStop> newData) {
        stopListDataSet = newData;
        filteredList = newData;
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    @Override
    public Filter getFilter() {
        if (locationFilter ==null){
            locationFilter = new LocationFilter();
        }
        return locationFilter;
    }
    /**
     * Custom filter for vehicle list
     * Filter content in vehicle list according to the search text
     */
    private class LocationFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            if (constraint!=null && constraint.length()>0) {
                ArrayList<RouteWithStops.VehicleStop> tempList = new ArrayList<>();

                // search content in vehicle list
                for (RouteWithStops.VehicleStop vehicle : stopListDataSet) {
                    if (vehicle.location.toLowerCase().contains(constraint.toString().toLowerCase())) {
                        tempList.add(vehicle);
                    }
                }

                filterResults.count = tempList.size();
                filterResults.values = tempList;
            } else {
                filterResults.count = stopListDataSet.size();
                filterResults.values = stopListDataSet;
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
            filteredList = (ArrayList<RouteWithStops.VehicleStop>) results.values;
            notifyDataSetChanged();
        }
    }
}
