package com.bharattracking.bharatracking.fragments;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.bharattracking.bharatracking.CustomToast;
import com.bharattracking.bharatracking.interfaces.LoginManager;
import com.bharattracking.bharatracking.R;
import com.bharattracking.bharatracking.utilities.Utils;
import com.bharattracking.bharatracking.activities.MainActivity;

public class SignUp_Fragment extends Fragment implements OnClickListener {
	private static View view;
	private static EditText fullNameEdit, emailIdEdit, mobileNumberEdit, locationEdit,
			passwordEdit, confirmPasswordEdit;
	private static TextView login, signup_header;
	private static Button signUpButton;
	private static CheckBox terms_conditions;
	private LoginManager loginManager;

	public SignUp_Fragment() {

	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		try {
			loginManager = (LoginManager) context;
		} catch (ClassCastException e) {
			throw new ClassCastException(context.toString() + e.getMessage());
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.signup_layout, container, false);
		initViews();
		setListeners();
		return view;
	}

	// Initialize all views
	private void initViews() {
		signup_header = view.findViewById(R.id.signup_header);

		fullNameEdit =  view.findViewById(R.id.fullName);
		emailIdEdit = view.findViewById(R.id.userEmailId);
		mobileNumberEdit =  view.findViewById(R.id.mobileNumber);
		locationEdit =  view.findViewById(R.id.location);
		passwordEdit = view.findViewById(R.id.password);
		confirmPasswordEdit = view.findViewById(R.id.confirmPassword);
		signUpButton =  view.findViewById(R.id.signUpBtn);
		login =  view.findViewById(R.id.already_user);
		terms_conditions = view.findViewById(R.id.terms_conditions);

		// Setting text selector over textviews
		XmlResourceParser xrp = getResources().getXml(R.xml.text_selector);
		try {
			ColorStateList csl = ColorStateList.createFromXml(getResources(),
					xrp);

			login.setTextColor(csl);
			terms_conditions.setTextColor(csl);
		} catch (Exception e) {
		}

		String signupHeaderText = "<font color='#071348'>SIGN</font><font color='#b90404'>UP</font>";
		signup_header.setText(Html.fromHtml(signupHeaderText));
	}

	// Set Listeners
	private void setListeners() {
		signUpButton.setOnClickListener(this);
		login.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.signUpBtn:

			// Call checkValidation method
			checkValidation();
			break;

		case R.id.already_user:

			// Replace login fragment
			new MainActivity().replaceLoginFragment();
			break;
		}

	}

	// Check Validation Method
	private void checkValidation() {

		// Get all edittext texts
		String fullName = fullNameEdit.getText().toString();
		String emailId = emailIdEdit.getText().toString();
		String mobileNumber = mobileNumberEdit.getText().toString().replaceAll("\\s+","");
		String location = locationEdit.getText().toString();
		String password = passwordEdit.getText().toString();
		String confirmPassword = confirmPasswordEdit.getText().toString();

		// Pattern match for email id
		Pattern p = Pattern.compile(Utils.regExEmail);
		Matcher m = p.matcher(emailId);
		Pattern mob = Pattern.compile(Utils.regExMobile);
		Matcher matcher = mob.matcher(mobileNumber);

		// Check if all strings are null or not
		if (mobileNumber.equals("") || mobileNumber.length() == 0
				|| location.equals("") || location.length() == 0
				|| password.equals("") || password.length() == 0
				|| confirmPassword.equals("")
				|| confirmPassword.length() == 0)

			new CustomToast().Show_Toast(getActivity(), view,
					"All * fields are required.");

		// Check if email id valid or not
		else if (!m.find() && !emailId.isEmpty())
			new CustomToast().Show_Toast(getActivity(), view,
					"Your Email Id is Invalid.");
		// Check if mobileno is valid or not
		else if (!matcher.find())
			new CustomToast().Show_Toast(getActivity(), view,
					"Your Mobile Number is Invalid.");
		// Check if both password should be equal
		else if (!confirmPassword.equals(password))
			new CustomToast().Show_Toast(getActivity(), view,
					"Both password doesn't match.");

		// Make sure user should check Terms and Conditions checkbox
		else if (!terms_conditions.isChecked())
			new CustomToast().Show_Toast(getActivity(), view,
					"Please select Terms and Conditions.");

		// Else do signup or do your stuff
		else {
			loginManager.doSignUp(mobileNumber,password,emailId,location,fullName);
		}
	}
}
