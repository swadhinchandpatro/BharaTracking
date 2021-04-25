package com.bharattracking.bharatracking.fragments;

import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.XmlResourceParser;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Html;
import android.text.InputType;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bharattracking.bharatracking.CustomToast;
import com.bharattracking.bharatracking.interfaces.LoginManager;
import com.bharattracking.bharatracking.R;
import com.bharattracking.bharatracking.utilities.Utils;

public class LoginFragment extends Fragment implements OnClickListener {
    public static final int CONNECTION_TIMEOUT=10000;
    public static final int READ_TIMEOUT=15000;

	private static View view;

	private static EditText mobileno, password;
	private static Button loginButton;
	private static TextView login_header,forgotPassword, signUp ,demo_text;
	private static CheckBox show_hide_password;
	private static Animation downtotopAnimation,fadeIn;
	private static Animation shakeAnimation;
	private static FragmentManager fragmentManager;
	private static ImageView autologin;

	private LoginManager loginManager;
	private LinearLayout login_content;

    Runnable runnable = new Runnable() {
		@Override
		public void run() {
			ViewGroup.LayoutParams params = autologin.getLayoutParams();
			params.width = 280;
			autologin.setLayoutParams(params);
			login_content.setVisibility(View.VISIBLE);
			login_content.setAnimation(downtotopAnimation);
			autologin.setAnimation(fadeIn);
		}
	};

	public LoginFragment() {

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.login_layout, container, false);
		login_content = view.findViewById(R.id.login_content);
		initViews();
		setUpFabUi();
		setListeners();
		runnable.run();
		return view;
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

    // Initiate Views
	private void initViews() {
		//delete
		autologin =  view.findViewById(R.id.autologin);
		login_header = view.findViewById(R.id.login_header);
//		demo_text = view.findViewById(R.id.demo_text);

		fragmentManager = getActivity().getSupportFragmentManager();

		mobileno = view.findViewById(R.id.login_mobileno);
		password = view.findViewById(R.id.login_password);
		loginButton = view.findViewById(R.id.loginBtn);
		forgotPassword = view.findViewById(R.id.forgot_password);
		signUp = view.findViewById(R.id.createAccount);
		show_hide_password = view
				.findViewById(R.id.show_hide_password);
		// Load ShakeAnimation
		shakeAnimation = AnimationUtils.loadAnimation(getActivity(),
				R.anim.shake);
		// Load DownToTopAnimation
		downtotopAnimation = AnimationUtils.loadAnimation(getActivity(),
				R.anim.slide_up);
		// Load Fade In Animation
		fadeIn = AnimationUtils.loadAnimation(getActivity(),
				R.anim.fade_in);

		// Setting text selector over textviews
		XmlResourceParser xrp = getResources().getXml(R.xml.text_selector);
		try {
			ColorStateList csl = ColorStateList.createFromXml(getResources(),
					xrp);

			forgotPassword.setTextColor(csl);
			show_hide_password.setTextColor(csl);
			signUp.setTextColor(csl);
		} catch (Exception e) {
		}

		String loginHeaderText = "<font color='#071348'>LOG</font><font color='#b90404'>IN</font>";
		login_header.setText(Html.fromHtml(loginHeaderText));

//		String demoText = "click on <font color='#071348'>B</font><font color='#b90404'>T</font> logo for demo";
//		demo_text.setText(Html.fromHtml(demoText));
	}

	void setUpFabUi(){
		FloatingActionButton fabCall = view.findViewById(R.id.fabCall);
		FloatingActionButton fabEmail = view.findViewById(R.id.fabEmail);
		FloatingActionButton fabWhatsapp = view.findViewById(R.id.fabwsp);

		fabCall.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent callIntent = new Intent(Intent.ACTION_DIAL);
				callIntent.setData(Uri.parse("tel:"+ getResources().getString(R.string.help_desk_phone)));
				if (callIntent.resolveActivity(getActivity().getPackageManager())!=null){
					startActivity(callIntent);
				}else {
					Snackbar.make(view, "No app to Support this action", Snackbar.LENGTH_LONG).show();
				}
			}
		});

		fabWhatsapp.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent wspIntent = new Intent(Intent.ACTION_VIEW);
				try {
					String url = "https://api.whatsapp.com/send?phone="+getResources().getString(R.string.help_desk_phone)
							+"&text="+ URLEncoder.encode(getResources().getString(R.string.default_wspmsg),"UTF-8");
					wspIntent.setPackage("com.whatsapp");
					wspIntent.setData(Uri.parse(url));
					if (wspIntent.resolveActivity(getActivity().getPackageManager())!=null){
						startActivity(wspIntent);
					}else {
						Snackbar.make(view, "No app to Support this action", Snackbar.LENGTH_LONG).show();
					}
				}catch (Exception e){
					Snackbar.make(view, "No app to Support this action", Snackbar.LENGTH_LONG).show();
				}
			}
		});

		fabEmail.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent sendIntent = new Intent(Intent.ACTION_SEND);
				sendIntent.setType("message/rfc822");
				final String emailTo = getResources().getString(R.string.support_email);
				sendIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{emailTo});
				try {
					startActivity(Intent.createChooser(sendIntent,"Send Email..."));
				}catch (Exception e){
					Snackbar.make(view, "No app to Support this action", Snackbar.LENGTH_LONG).show();
				}
			}
		});
	}

	// Set Listeners
	private void setListeners() {
		loginButton.setOnClickListener(this);
		forgotPassword.setOnClickListener(this);
		signUp.setOnClickListener(this);
//		autologin.setOnClickListener(this);

		// Set check listener over checkbox for showing and hiding password
		show_hide_password
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton button,
							boolean isChecked) {

						// If it is checkec then show password else hide
						// password
						if (isChecked) {

							show_hide_password.setText(R.string.hide_pwd);// change
																			// checkbox
																			// text

							password.setInputType(InputType.TYPE_CLASS_TEXT);
							password.setTransformationMethod(HideReturnsTransformationMethod
									.getInstance());// show password
						} else {
							show_hide_password.setText(R.string.show_pwd);// change
																			// checkbox
																			// text

							password.setInputType(InputType.TYPE_CLASS_TEXT
									| InputType.TYPE_TEXT_VARIATION_PASSWORD);
							password.setTransformationMethod(PasswordTransformationMethod
									.getInstance());// hide password

						}

					}
				});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.loginBtn:
			checkValidation();
			break;

		case R.id.forgot_password:

			// Replace forgot password fragment with animation
			fragmentManager
					.beginTransaction()
					.setCustomAnimations(R.anim.right_enter, R.anim.left_out)
					.replace(R.id.frameContainer,
							new ForgotPassword_Fragment(),
							Utils.ForgotPassword_Fragment).commit();
			break;
		case R.id.createAccount:

			// Replace signup frgament with animation
			fragmentManager
					.beginTransaction()
					.setCustomAnimations(R.anim.right_enter, R.anim.left_out)
					.replace(R.id.frameContainer, new SignUp_Fragment(),
							Utils.SignUp_Fragment).commit();
			break;
		case R.id.autologin:
			loginManager.doLogin("7904459203", "4321");
			break;
		}

	}

	// Check Validation before login
	private void checkValidation() {
		// Get email id and password
		String mobileNumber = mobileno.getText().toString();
		String pass = password.getText().toString();

		// Check patter for email id
		Pattern p = Pattern.compile(Utils.regExMobile);

		Matcher m = p.matcher(mobileNumber);

		// Check for both field is empty or not
		if (mobileNumber.equals("") || mobileNumber.length() == 0
				|| pass.equals("") || pass.length() == 0) {
			login_content.startAnimation(shakeAnimation);
			new CustomToast().Show_Toast(getActivity(), view,
					"Enter both credentials.");

		}
		// Check if email id is valid or not
		else if (!m.find()){
			login_content.startAnimation(shakeAnimation);
			new CustomToast().Show_Toast(getActivity(), view,
					"Your Mobile Number is Invalid.");
		}
		else if (pass.isEmpty() || pass.length() < 4 || pass.length() > 10) {
			login_content.startAnimation(shakeAnimation);
			new CustomToast().Show_Toast(getActivity(), view,
					"between 4 and 10 alphanumeric characters");
		}
		// Else do login and do your stuff
		else {
            loginManager.doLogin(mobileNumber, pass);
        }
	}


}
