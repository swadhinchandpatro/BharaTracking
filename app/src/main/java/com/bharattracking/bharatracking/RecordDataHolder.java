package com.bharattracking.bharatracking;

import com.bharattracking.bharatracking.APIresponses.VehicleLiveData;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by swadhin on 16/3/18.
 */

public class RecordDataHolder {
    private int vStatus[] = new int[4];
    private ArrayList<VehicleLiveData> vehicleListData = new ArrayList<>();
    public ArrayList<VehicleLiveData> getData() { return vehicleListData; }
    public int getVehicleCount() { return vehicleListData.size(); }
    public int[] getvStatus(){return vStatus;}
    public void setData(ArrayList<VehicleLiveData> data) {
        this.vehicleListData.clear();
        this.vehicleListData.addAll(data);
        for (int i=0;i<4;i++)
            this.vStatus[i]=0;
        for (VehicleLiveData vehicle: vehicleListData){
            switch (vehicle.category){
                case "running":
                    this.vStatus[0]++;
                    break;
                case "stopped":
                    this.vStatus[1]++;
                    break;
                case "dormant":
                    this.vStatus[2]++;
                    break;
                case "not working":
                    this.vStatus[3]++;
                    break;
            }
        }
    }

    private static RecordDataHolder holder = null;
    public static RecordDataHolder getInstance() {
        if (holder == null)
            holder = new RecordDataHolder();
        return holder;
    }
}