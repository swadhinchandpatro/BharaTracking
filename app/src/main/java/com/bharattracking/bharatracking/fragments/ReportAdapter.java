package com.bharattracking.bharatracking.fragments;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.bharattracking.bharatracking.APIresponses.VehicleLiveData;
import com.bharattracking.bharatracking.APIresponses.VehicleReportDetails;
import com.bharattracking.bharatracking.Constants;
import com.bharattracking.bharatracking.ImageLoader;
import com.bharattracking.bharatracking.R;
import com.bharattracking.bharatracking.RecordDataHolder;

import java.util.ArrayList;
import java.util.HashMap;

class ReportAdapter extends BaseAdapter implements Filterable {

    private String unitid = null;
    private Activity activity;
    private ArrayList<VehicleReportDetails> data;
    private static LayoutInflater inflater=null;
    public ImageLoader imageLoader;
    private ReportVehicleFilter ReportVehicleFilter;
    private ArrayList<VehicleReportDetails> filteredList;

    public ReportAdapter(Activity a, ArrayList<VehicleReportDetails> d) {
        activity = a;
        data = d;
        filteredList = d;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader = new ImageLoader(activity.getApplicationContext());
        getFilter();
    }

    public void updateReportData(ArrayList<VehicleReportDetails> newData){
        assignData(newData);
        this.notifyDataSetChanged();
    }

    private void assignData(ArrayList<VehicleReportDetails> newData) {
        data = newData;
        filteredList = newData;
    }

    public ArrayList<VehicleReportDetails> getData(){ return data;}

    public int getCount() {
        return filteredList.size();
    }

    public Object getItem(int position) {
        return filteredList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null)
            vi = inflater.inflate(R.layout.travelreport_list_item, parent,false);

        TextView tvlr_vehicle_no = vi.findViewById(R.id.tvlr_vehicle_no); // title
        TextView tvlr_running_time = vi.findViewById(R.id.tvlr_running_time); // location address
        TextView tvlr_stopped_time = vi.findViewById(R.id.tvlr_stopped_time); // ignition info
        TextView tvlr_dormant_time =  vi.findViewById(R.id.tvlr_dormant_time);//ignition duration
        TextView tvlr_nworking_time =  vi.findViewById(R.id.tvlr_nworking_time); //odometer info
        TextView tvlr_count = vi.findViewById(R.id.tvlr_count); // Speed
        TextView tvlr_distance = vi.findViewById(R.id.tvlr_distance); // last updated time
        TextView tvlr_avgspeed = vi.findViewById(R.id.tvlr_avgspeed); // last updated time
        TextView tvlr_maxspeed = vi.findViewById(R.id.tvlr_maxspeed); // last updated time
        ImageView tvlr_list_image = vi.findViewById(R.id.tvlr_list_image); // thumb image
        TextView alertorstop = vi.findViewById(R.id.alertorstop);

        VehicleReportDetails report;
        report = filteredList.get(position);
        if (report.totalstops != null && !report.totalstops.isEmpty()){
            alertorstop.setText("Stops");
            tvlr_count.setText(report.totalstops);
        }else {
            alertorstop.setText("Alerts");
            tvlr_count.setText(report.totalalerts);
        }

        tvlr_vehicle_no.setText(report.vehicleno);
        tvlr_running_time.setText(report.running);
        tvlr_stopped_time.setText(report.stopped);
        tvlr_dormant_time.setText(report.dormant);
        tvlr_nworking_time.setText(report.nworking);
        tvlr_distance.setText(report.distance);
        tvlr_avgspeed.setText(report.avgspeed);
        tvlr_maxspeed.setText(report.maxspeed);
        imageLoader.DisplayImage(report.vtype, tvlr_list_image);

        return vi;
    }

    @Override
    public Filter getFilter() {
        if (ReportVehicleFilter==null){
            ReportVehicleFilter = new ReportVehicleFilter();
        }
        return ReportVehicleFilter;
    }

    /**
     * Custom filter for vehicle list
     * Filter content in vehicle list according to the search text
     */
    private class ReportVehicleFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            if (constraint!=null && constraint.length()>0) {
                ArrayList<VehicleReportDetails> tempList = new ArrayList<>();

                // search content in vehicle list
                for (VehicleReportDetails vehicle : data) {
                    if (vehicle.vehicleno.toLowerCase().contains(constraint.toString().toLowerCase())) {
                        tempList.add(vehicle);
                    }
                }

                filterResults.count = tempList.size();
                filterResults.values = tempList;
            } else {
                filterResults.count = data.size();
                filterResults.values = data;
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
            filteredList = (ArrayList<VehicleReportDetails>) results.values;
            notifyDataSetChanged();
        }
    }

}

