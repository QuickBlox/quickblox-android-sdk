package com.quickblox.chat_v2.ui.activities;

import java.io.IOException;
import java.net.MalformedURLException;

import org.json.JSONException;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.Session;
import com.facebook.SessionState;
import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.apis.FaceBookManager;
import com.quickblox.chat_v2.core.ChatApplication;
import com.quickblox.chat_v2.ui.dialogs.SplashDialog;
import com.quickblox.chat_v2.utils.SharedPreferencesHelper;
import com.quickblox.core.QBCallback;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.QBSettings;
import com.quickblox.core.result.Result;
import com.quickblox.internal.core.exception.BaseServiceException;
import com.quickblox.internal.core.server.BaseService;
import com.quickblox.module.auth.QBAuth;
import com.quickblox.module.auth.model.QBProvider;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.module.users.result.QBUserResult;

public class SplashActivity extends FragmentActivity implements QBCallback, Session.StatusCallback {
	
	private DialogFragment quickBloxDialog;
	private ProgressDialog progress;
	
	private FaceBookManager fbm;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.activity_splash);
		
		Button facebookButton = (Button) findViewById(R.id.splash_facebook_button);
		Button registrationButton = (Button) findViewById(R.id.splash_registration_button);
		Button siginButton = (Button) findViewById(R.id.splash_sign_in_button);
		
		progress = ProgressDialog.show(this, getResources().getString(R.string.app_name), getResources().getString(R.string.splash_progressdialog), true);
		
		fbm = new FaceBookManager();
		
		OnClickListener clickButtonListener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				switch (v.getId()) {
					case R.id.splash_facebook_button :
						if (!isOnline()) {
							break;
						}
						onFbClickLogin();
						break;
					
					case R.id.splash_registration_button :
						if (!isOnline()) {
							break;
						}
						quickBloxDialog = new SplashDialog(true);
						quickBloxDialog.show(getSupportFragmentManager(), null);
						break;
					
					case R.id.splash_sign_in_button :
						if (!isOnline()) {
							break;
						}
						quickBloxDialog = new SplashDialog(false);
						quickBloxDialog.show(getSupportFragmentManager(), null);
						break;
				
				}
			}
			
		};
		
		facebookButton.setOnClickListener(clickButtonListener);
		registrationButton.setOnClickListener(clickButtonListener);
		siginButton.setOnClickListener(clickButtonListener);
		
		if (isOnline()) {
			
			QBSettings.getInstance().fastConfigInit(getResources().getString(R.string.quickblox_app_id), getResources().getString(R.string.quickblox_auth_key),
					getResources().getString(R.string.quickblox_auth_secret));
			
			QBAuth.createSession(new QBCallbackImpl() {
				@Override
				public void onComplete(Result result) {
					
					Session session = new Session(SplashActivity.this);
					System.out.println("session = " + session);
					
					if (TextUtils.isEmpty(session.getAccessToken())) {
						System.out.println("Пролёт");
						if (!TextUtils.isEmpty(SharedPreferencesHelper.getLogin(SplashActivity.this))) {
							
							System.out.println("через КБ");
							QBUser user = new QBUser(); 
							user.setLogin(SharedPreferencesHelper.getLogin(SplashActivity.this));
							user.setPassword(SharedPreferencesHelper.getPassword(SplashActivity.this));

							ChatApplication.getInstance().setQbUser(user);
							QBUsers.signIn(user, SplashActivity.this, user.getPassword());
							return;
						}
						progress.dismiss();
					} else {
						System.out.println("Через сессию");
						QBUsers.signInUsingSocialProvider(QBProvider.FACEBOOK, session.getAccessToken(), null, SplashActivity.this);
						
					}
				}
			});
			
		} else {
			progress.dismiss();
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	}
	
	// FACEBOOK LOGIN
	private void onFbClickLogin() {
		
		Session session = Session.getActiveSession();
		
		if (session == null) {
			session = new Session(this);
			
			Session.setActiveSession(session);
			if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
				session.openForRead(new Session.OpenRequest(this).setCallback(this));
			}
			
		}
		if (!session.isOpened() && !session.isClosed()) {
			session.openForRead(new Session.OpenRequest(this).setCallback(this));
		}
	}
	
	// QB CALLBACK
	
	@Override
	public void onComplete(Result result) {
		System.out.println("Section 1");
		
		QBUser newUser = ((QBUserResult) result).getUser();
		newUser.setPassword(SharedPreferencesHelper.getPassword(SplashActivity.this));
		ChatApplication.getInstance().setFbUser(newUser);
		
		//progress.dismiss();
		loadMainScreen();
	}
	
	@Override
	public void onComplete(Result result, Object context) {
		System.out.println("Section 2");
		if (result.isSuccess()) {
			
			QBUser qbUser = ((QBUserResult) result).getUser();
			SharedPreferencesHelper.setLogin(getBaseContext(), qbUser.getLogin());
			
			if (context.toString().equals("social")) {
				
				try {
				
					ChatApplication.getInstance().setFbUser(qbUser);
					SharedPreferencesHelper.setPassword(this, BaseService.getBaseService().getToken());
					
				} catch (BaseServiceException e) {
					e.printStackTrace();
				}
			} else {
				SharedPreferencesHelper.setPassword(getBaseContext(), context.toString());
				ChatApplication.getInstance().setQbUser(qbUser);
				
			}
			
			loadMainScreen();
		} else {
			System.out.println("Регистрация неудачна");
		}
		
	}
	
	// FACEBOOK CALLBACK
	@Override
	public void call(Session session, SessionState state, Exception exception) {
		if (TextUtils.isEmpty(session.getAccessToken())) {
			
			return;
		}
		try {
			fbm.getMyInfo(getBaseContext(), session.getAccessToken());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		QBUsers.signInUsingSocialProvider(QBProvider.FACEBOOK, session.getAccessToken(), null, this, "social");
		progress = ProgressDialog.show(this, getResources().getString(R.string.app_name), getResources().getString(R.string.splash_progressdialog), true);
		
	}
	
	private void loadMainScreen() {
		Intent intent = new Intent(getBaseContext(), MainActivity.class);
		startActivity(intent);
		finish();
	}
	
	// INTERNET REVIEW
	public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		}
		Toast.makeText(SplashActivity.this, getResources().getString(R.string.splash_internet_error), Toast.LENGTH_LONG).show();
		return false;
	}
	
}