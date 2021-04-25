package com.bharattracking.bharatracking.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.bharattracking.bharatracking.APIresponses.VehicleLiveData;
import com.bharattracking.bharatracking.CustomToast;
import com.bharattracking.bharatracking.R;
import com.bharattracking.bharatracking.RecordDataHolder;
import com.bharattracking.bharatracking.session.SessionManagement;
import com.bharattracking.bharatracking.utilities.AlertDialogManager;
import com.bharattracking.bharatracking.utilities.Utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandFragment extends Fragment {

    private View view;
    private ArrayList<VehicleLiveData> vehicleList = new ArrayList<>();
    private SessionManagement session;
    private Button sendBtn;
    private TextView selectCommandText,selectVehicleText;
    private ProgressDialog progressDialog;
    private String[] vehicleNames;

    private String command = null;

    private static final int SERVER_PORT = 8446;
    private static final String SERVER_IP = "168.235.102.152";
    private ClientThread clientThread = null;
    private Thread thread;
    private Socket socket;
    private ArrayAdapter<String> adapter;
    private AlertDialog.Builder vehicleDialog;
    private EditText valueToSend;
    private AlertDialogManager alert = new AlertDialogManager();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        session = new SessionManagement(getActivity().getApplicationContext());
        progressDialog = new ProgressDialog(getActivity(),
                R.style.AppTheme_Dark_Dialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_command,container,false);

        initView();

        vehicleDialog = new AlertDialog.Builder(getActivity());
        vehicleDialog.setTitle(getString(R.string.select_vehicle));

        selectVehicleText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vehicleDialog.show();
            }
        });

        final AlertDialog.Builder cmdDialog = new AlertDialog.Builder(getActivity());
        cmdDialog.setTitle(getString(R.string.select_cmd));
        cmdDialog.setItems(R.array.cmd_list, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i){
                    case 0:
                        command = getString(R.string.set_maxspeed);
                        valueToSend.setVisibility(View.VISIBLE);
                        valueToSend.requestFocus();
                        break;
                    case 1:
                        command = getString(R.string.set_odom_command);
                        valueToSend.setVisibility(View.VISIBLE);
                        valueToSend.requestFocus();
                        break;
                    case 2:
                        command = getString(R.string.start_immobilize_command);
                        valueToSend.setVisibility(View.GONE);
                        break;
                    case 3:
                        command = getString(R.string.stop_immobilize_command);
                        valueToSend.setVisibility(View.GONE);
                        break;
                }
                String[] cmds = getResources().getStringArray(R.array.cmd_list);
                setTextViewText(selectCommandText,cmds[i]);
            }
        });

        selectCommandText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cmdDialog.show();
            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validate()){
                    String commandText = selectCommandText.getText().toString();
                    AlertDialog.Builder dialog = alert.showAlertDialog(getActivity(),"Are you sure to "+ commandText,"please review before proceeding","info");
                    dialog.setNegativeButton("CANCEL",null);
                    dialog.setPositiveButton("SUBMIT", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String unitid = getUnitId(selectVehicleText.getText().toString());
                            String enteredValue = valueToSend.getVisibility() == View.VISIBLE ? valueToSend.getText().toString() : null;
                            if (unitid == null){
                                toastMessage("No Such Vehicle Found");
                            }
                            if (enteredValue != null){
                                String odomVal = !enteredValue.equals("0") ? enteredValue + getResources().getString(R.string.odom_command_suffix) : enteredValue;
                                clientThread.setSendcmd(command+odomVal,unitid);
                            }
                        }
                    }).show();
                }
            }
        });

        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isFragmentVisible_) {
        super.setUserVisibleHint(isFragmentVisible_);
        if (this.isVisible()) {
            // we check that the fragment is becoming visible
            if (isFragmentVisible_) {
                setUpClient();
                vehicleList = RecordDataHolder.getInstance().getData();
                vehicleNames = new String[vehicleList.size()];
                for (int i=0;i<vehicleList.size();i++){
                    vehicleNames[i] = vehicleList.get(i).vehicleno;
                }
                vehicleDialog.setItems(vehicleNames, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        setTextViewText(selectVehicleText, vehicleNames[i]);
                    }
                });
            }else if (thread != null && thread.isAlive()){
                try {
                    socket.close();
                    thread.interrupt();
                    Log.d("MapsActivity","Connection Closed");
                } catch (IOException e) {
                    toastMessage(e.getMessage());
                }
            }
        }
    }

    private void setTextViewText(TextView textView, String value) {
        textView.setText(value);
    }

    public void setSelectVehicleText(String value){
        setTextViewText(selectVehicleText,value);
    }

    private boolean validate() {
        if (selectVehicleText.getText().toString().contains("Vehicle")) {
            toastMessage("Choose Vehicle No");
            return false;
        }else if(selectCommandText.getText().toString().contains("Command")){
            toastMessage("Choose Command to Send");
            return false;
        }else if(valueToSend.getVisibility() == View.VISIBLE){
            if(valueToSend.getText().toString().isEmpty()) {
                toastMessage("Enter Odometer Value to Set");
                return false;
            }
            else {
                Pattern mob = Pattern.compile(Utils.regNumber);
                Matcher matcher = mob.matcher(valueToSend.getText().toString());
                if (!matcher.find()){
                    toastMessage("Enter a Valid Value");
                    return false;
                }
            }
        }
        return true;
    }

    private String getUnitId(String vehicleno) {
        for(VehicleLiveData vehicle : vehicleList){
            if (vehicle.vehicleno.equals(vehicleno)){
                return vehicle.unitid;
            }
        }
        return null;
    }

    private void setUpClient() {
        if (clientThread==null || !thread.isAlive()){
            clientThread = new ClientThread();
            thread = new Thread(clientThread);
            thread.start();
        }
    }

    private void initView() {
        selectVehicleText = view.findViewById(R.id.vehicleno);
        selectCommandText = view.findViewById(R.id.command);
        valueToSend = view.findViewById(R.id.value_to_send);
        sendBtn = view.findViewById(R.id.send_command);

        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
    }

    private void updateMessage(final String msg) {
        progressDialog.setMessage(msg);
    }

    private void toastMessage(String msg){
        new CustomToast().Show_Toast(getActivity(), view, msg);
    }

    class ClientThread implements Runnable {

        private BufferedReader input;

        @Override
        public void run() {

            try {
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);

                socket = new Socket(serverAddr, SERVER_PORT);

                if (socket != null) {
                    while (!Thread.currentThread().isInterrupted()) {
                        this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        String message = input.readLine();
                        progressDialog.dismiss();
                        if (message.contains("success")) {
                            toastMessage("Command Successfully sent");
                            break;
                        } else if (message.contains("offline")) {
                            toastMessage("Vehicle is offline");
                            break;
                        } else if (message.contains("unavailable")){
                            toastMessage("Not Connected");
                            break;
                        }
                    }
                }
            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        }

        public void setSendcmd(String cmd,String unitid) {
            try {
                PrintWriter out = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream())),
                        true);
                updateMessage("Sending cmd...");
                progressDialog.show();
                out.print("$BTCMD"+cmd+"BTUNITID"+unitid);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e)        {
                e.printStackTrace();
            }
        }
    }
}
