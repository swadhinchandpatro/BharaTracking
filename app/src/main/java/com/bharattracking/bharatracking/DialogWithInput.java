package com.bharattracking.bharatracking;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Objects;

public class DialogWithInput extends AppCompatDialogFragment {
    private EditText inputEditText;
    private InputCollection listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        listener = (InputCollection) context;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.driver_number_layout,null);

        inputEditText = view.findViewById(R.id.driver_mobile);

        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AppTheme_Dark_Dialog));

        TextView textView = new TextView(getActivity());
        final String vehicleNo = Objects.requireNonNull(getArguments()).getString(Constants.KEY_VEHICLE_NO);
        textView.setText(String.format("%s Driver:", vehicleNo));

        textView.setPadding(20, 30, 20, 30);
        textView.setTextSize(20F);
        textView.setBackgroundColor(getActivity().getResources().getColor(R.color.gradient_two));
        textView.setTextColor(Color.WHITE);
        // Setting Dialog Title
        builder.setView(view).setCustomTitle(textView);

        builder.setPositiveButton("SUBMIT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String driverNumber = inputEditText.getText().toString();
                if (!driverNumber.isEmpty()){
                    listener.getInputValue(driverNumber,vehicleNo);
                }
            }
        });

        builder.setNegativeButton("CANCEL",null);

        return builder.create();
    }

    public interface InputCollection {
        void getInputValue(String input, String vehicleNo);
    }

}
