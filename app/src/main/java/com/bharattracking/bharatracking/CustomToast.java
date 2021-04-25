package com.bharattracking.bharatracking;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class CustomToast {

	private View layout;
	private Activity _activity;
	public CustomToast(Activity activity){
		this._activity = activity;
	}
	public CustomToast(){

	}
	// Custom Toast Method
	public void Show_Toast(Context context,View view, String msg,int duration) {

		// Layout Inflater for inflating custom view
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		// inflate the layout over view
		if (view!=null)
			layout = inflater.inflate(R.layout.custom_toast,
				(ViewGroup) view.findViewById(R.id.toast_root));
		else
			layout = inflater.inflate(R.layout.custom_toast,
					(ViewGroup) _activity.findViewById(R.id.toast_root));
		// Get TextView id and set msg
		TextView text =  layout.findViewById(R.id.toast_error);
		text.setText(msg);

		Toast toast = new Toast(context);// Get Toast Context
		toast.setGravity(Gravity.TOP | Gravity.FILL_HORIZONTAL, 0, 0);// Set
																		// Toast
																		// gravity
																		// and
																		// Fill
																		// Horizoontal

		toast.setDuration(duration);// Set Duration
		toast.setView(layout); // Set Custom View over toast

		toast.show();// Finally show toast
	}
	public void Show_Toast(Context context,View view,String msg){
		Show_Toast(context,view,msg,Toast.LENGTH_SHORT);
	}

}
