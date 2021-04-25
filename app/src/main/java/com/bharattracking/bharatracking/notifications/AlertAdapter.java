package com.bharattracking.bharatracking.notifications;

import android.graphics.Color;
import android.support.annotation.ArrayRes;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.bharattracking.bharatracking.R;
import com.bharattracking.bharatracking.fragments.AlertFragment;

import java.util.ArrayList;

public class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.AlerViewHolder> implements Filterable{

    private ArrayList<AlertData> alertDataSet,filteredList;
    private AlertData alert;
    private AlertFilter alertFilter;
    private String appliedFilter = null;

    public static class AlerViewHolder extends RecyclerView.ViewHolder {
        static final String SPEED_ICON_URL="speedIcon";
        static final String IGN_ICON_URL="ignIcon";

        public TextView location_info;
        CardView cardView;
        TextView notificationMsg;
        TextView updated_time;
        TextView vehicle_no;
        ImageView notificationIcon;

        AlerViewHolder(View itemView) {
            super(itemView);
            this.cardView = itemView.findViewById(R.id.card_view);
            this.notificationIcon = itemView.findViewById(R.id.notificationIcon);
            this.vehicle_no = itemView.findViewById(R.id.vehicle_no);
            this.updated_time =  itemView.findViewById(R.id.updated_time);
            this.location_info = itemView.findViewById(R.id.location_info);
            this.notificationMsg = itemView.findViewById(R.id.notificationMsg);
        }
    }

    public AlertAdapter(ArrayList<AlertData> data) {
        setItems(data);
    }

    public void updateAlertData(ArrayList<AlertData> newData){
        assignData(newData);
        this.notifyDataSetChanged();
    }

    private void assignData(ArrayList<AlertData> newData) {
        alertDataSet = newData;
        filteredList = getFilteredResult(appliedFilter);
    }

    public void filter(String filterText){
        appliedFilter = filterText;
        getFilter().filter(filterText);
    }

    @Override
    public AlerViewHolder onCreateViewHolder(ViewGroup parent,
                                             int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cards_layout, parent, false);

        view.setOnClickListener(AlertFragment.alertOnClickListener);

        return new AlerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final AlerViewHolder holder, final int listPosition) {

        alert = filteredList.get(listPosition);
        if (alert.iconUrl.equals(AlerViewHolder.IGN_ICON_URL))
            holder.notificationIcon.setImageResource(R.mipmap.ignicon);
        else if(alert.iconUrl.equals(AlerViewHolder.SPEED_ICON_URL))
            holder.notificationIcon.setImageResource(R.mipmap.overspeed);
        else
            holder.notificationIcon.setImageResource(R.drawable.error);
        holder.vehicle_no.setText(alert.vehicleNo);
        holder.updated_time.setText(alert.updatedTime);
        holder.location_info.setText(alert.location);
        holder.notificationMsg.setText(alert.notificationMsg);
        if(alert.isSeen){
            holder.cardView.setCardBackgroundColor(Color.parseColor("#DDDDDD"));
        }
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    @Override
    public Filter getFilter() {
        if (alertFilter ==null){
            alertFilter = new AlertFilter();
        }
        return alertFilter;
    }

    public void setItems(ArrayList<AlertData> alerts) {
        this.alertDataSet = alerts;
        this.filteredList = alerts;
    }

    private ArrayList<AlertData> getFilteredResult(CharSequence constraint) {
        ArrayList<AlertData> tempList = new ArrayList<>();

        if (constraint == null || constraint.length() == 0) {
            return alertDataSet;
        }
        // search content in vehicle list
        for (AlertData alert : alertDataSet) {
            if (alert.vehicleNo.toLowerCase().contains(constraint.toString().toLowerCase())) {
                tempList.add(alert);
            }
        }
        return tempList;
    }

    /**
     * Custom filter for vehicle list
     * Filter content in vehicle list according to the search text
     */
    private class AlertFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            if (constraint!=null && constraint.length()>0) {
                ArrayList<AlertData> tempList = getFilteredResult(constraint);

                filterResults.count = tempList.size();
                filterResults.values = tempList;
            } else {
                filterResults.count = alertDataSet.size();
                filterResults.values = alertDataSet;
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
            filteredList = (ArrayList<AlertData>) results.values;
            notifyDataSetChanged();
        }
    }
}
