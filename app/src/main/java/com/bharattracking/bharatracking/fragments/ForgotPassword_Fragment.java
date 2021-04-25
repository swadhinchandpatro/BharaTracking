package com.bharattracking.bharatracking.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.bharattracking.bharatracking.CustomToast;
import com.bharattracking.bharatracking.R;
import com.bharattracking.bharatracking.session.SessionManagement;
import com.bharattracking.bharatracking.activities.DashboardActivity;
import com.bharattracking.bharatracking.activities.MainActivity;
import com.bharattracking.bharatracking.utilities.SendMailTask;
import com.bharattracking.bharatracking.utilities.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ForgotPassword_Fragment extends Fragment implements
		OnClickListener {
	private View view;

	private EditText toEmailId,securityCodeText;
	private static List<String> toEmailList = new ArrayList<>();
	private TextView submit, back , resendSecurityCode;
	SessionManagement session;
	String securityCode = null;

	public ForgotPassword_Fragment() {

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.forgotpassword_layout, container,
				false);
		initViews();
		setListeners();
		return view;
	}

	// Initialize the views
	private void initViews() {
		toEmailId = view.findViewById(R.id.registered_emailid);
		submit = view.findViewById(R.id.forgot_button);
		back = view.findViewById(R.id.backToLoginBtn);
		securityCodeText = view.findViewById(R.id.security_code);
		securityCodeText.setVisibility(View.GONE);
		resendSecurityCode = view.findViewById(R.id.resendSecurityCode);
		resendSecurityCode.setVisibility(View.GONE);
		//get current session
		session = new SessionManagement(getActivity().getApplicationContext());
		// Setting text selector over textviews
		XmlResourceParser xrp = getResources().getXml(R.xml.text_selector);
		try {
			ColorStateList csl = ColorStateList.createFromXml(getResources(),
					xrp);

			back.setTextColor(csl);
			submit.setTextColor(csl);

		} catch (Exception e) {
		}

	}

	// Set Listeners over buttons
	private void setListeners() {
		back.setOnClickListener(this);
		submit.setOnClickListener(this);
		resendSecurityCode.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.backToLoginBtn:

				// Replace Login Fragment on Back Presses
				new MainActivity().replaceLoginFragment();
				break;

			case R.id.forgot_button:
				String code = securityCode;
				if (code != null && !code.isEmpty()) {
					String enteredCode = securityCodeText.getText().toString();
					if (enteredCode!=null && !enteredCode.isEmpty()){
						if (code.equals(enteredCode)) {
							doLogin();
						}else {
							new CustomToast().Show_Toast(getActivity(), view,
									"Security Code is incorrect");
						}
					}else {
						new CustomToast().Show_Toast(getActivity(), view,
								"Please enter Security Code");
					}
				}else {
					// Call Submit button task
					submitButtonTask();
				}
				break;
			case R.id.resendSecurityCode:
				submit.setText(getString(R.string.submit));
				securityCode=null;
				initViews();
				break;

		}

	}

	private void doLogin() {
		//move to dashboardActivity
		Activity currentActivity = getActivity();
		Intent intent = new Intent(currentActivity,DashboardActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
		currentActivity.finish();
		currentActivity.overridePendingTransition(R.anim.left_enter, R.anim.right_out);
	}

	private void submitButtonTask() {
		String getEmailId = toEmailId.getText().toString();

		// Pattern for email id validation
		Pattern p = Pattern.compile(Utils.regExEmail);

		// Match the pattern
		Matcher m = p.matcher(getEmailId);

		// First check if email id is not null else show error toast
		if (getEmailId.equals("") || getEmailId.length() == 0)

			new CustomToast().Show_Toast(getActivity(), view,
					"Please enter your Email Id.");

			// Check if email id is valid or not
		else if (!m.find())
			new CustomToast().Show_Toast(getActivity(), view,
					"Your Email Id is Invalid.");

			// Else submit email id and fetch passwod or do your stuff
		else{
			final String fromEmailId = getString(R.string.support_email);
			toEmailList.add(getEmailId);
			toEmailList.add(getString(R.string.support_email));
			Log.i("SendMailActivity", "To List: " + toEmailList);
			String emailSubject = getString(R.string.forgot_password_subject);
			String emailBody = getString(R.string.forgot_password_body);
			final String fromPassword = getString(R.string.support_pass);
			securityCode = Utils.randomAlphaNumeric(6);
			new SendMailTask(getActivity()).execute(fromEmailId,fromPassword
					,toEmailList, emailSubject, emailBody + " "+ securityCode);
			//after email is sent the security code textedit is made visible
			securityCodeText.setVisibility(View.VISIBLE);
			resendSecurityCode.setVisibility(View.VISIBLE);
			submit.setText(R.string.verify);
		}
	}
}