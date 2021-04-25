package com.bharattracking.bharatracking.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.bharattracking.bharatracking.Constants;
import com.bharattracking.bharatracking.ImageLoader;
import com.bharattracking.bharatracking.R;

import java.util.ArrayList;
import java.util.HashMap;

public class RunListItemAdapter extends BaseAdapter implements Filterable {

    private Activity activity;
    private ArrayList<HashMap<String, String>> data;
    private static LayoutInflater inflater=null;
    public ImageLoader imageLoader;
    private LocationFilter locationFilter;
    private ArrayList<HashMap<String, String>> filteredList;

    public RunListItemAdapter(Activity a, ArrayList<HashMap<String, String>> d) {
        activity = a;
        data=d;
        filteredList = d;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader=new ImageLoader(activity.getApplicationContext());
        getFilter();
    }

    public ArrayList<HashMap<String, String>> getData(){ return data;}

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
            vi = inflater.inflate(R.layout.run_list_item, parent,false);

        TextView vehicle_no = vi.findViewById(R.id.vehicle_no); // title
        TextView location_info = vi.findViewById(R.id.location_info); // location address
        TextView ign_duration =  vi.findViewById(R.id.runDuration);//ignition duration
        TextView last_updated_time = vi.findViewById(R.id.last_updated_time); // last updated time
        ImageView thumb_image= vi.findViewById(R.id.list_image); // thumb image

        HashMap<String, String> run;
        run = filteredList.get(position);

        if (run.get(Constants.KEY_CATEGORY).equals("running")){
            vehicle_no.setTextColor(Color.GREEN);
        }
        else if(run.get(Constants.KEY_CATEGORY).equals("stopped")){
            vehicle_no.setTextColor(Color.RED);
        }
        else if (run.get(Constants.KEY_CATEGORY).equals("dormant")){
            vehicle_no.setTextColor(Color.parseColor("#FF5733"));
        }else {
            vehicle_no.setTextColor(Color.BLUE);
        }
        String runDuration = run.get(Constants.KEY_DURATION);
        ign_duration.setText(runDuration);
        vehicle_no.setText(run.get(Constants.KEY_VEHICLE_NO));
        location_info.setText(run.get(Constants.KEY_LOCATION_INFO));
        last_updated_time.setText(run.get(Constants.KEY_LAST_UPDATED_TIME));

        imageLoader.DisplayImage(run.get(Constants.KEY_THUMB_URL), thumb_image);

        vehicle_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        return vi;
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
            String label = Constants.KEY_LOCATION_INFO;
            if (constraint!=null && constraint.length()>0) {
                if (constraint.toString().contains("status:")){
                    constraint = constraint.subSequence(7,constraint.length());
                    label = Constants.KEY_CATEGORY;
                }
                ArrayList<HashMap<String, String>> tempList = new ArrayList<>();

                // search content in vehicle list
                for (HashMap<String, String> vehicle : data) {
                    if (vehicle.get(label).toLowerCase().contains(constraint.toString().toLowerCase())) {
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
            filteredList = (ArrayList<HashMap<String, String>>) results.values;
            notifyDataSetChanged();
        }
    }
}
