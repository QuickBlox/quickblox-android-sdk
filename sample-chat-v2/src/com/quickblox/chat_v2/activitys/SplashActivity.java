package com.quickblox.chat_v2.activitys;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.Session;
import com.facebook.SessionState;
import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.fragment.SplashDialog;
import com.quickblox.chat_v2.others.ChatApplication;
import com.quickblox.core.QBCallback;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.QBSettings;
import com.quickblox.core.result.Result;
import com.quickblox.internal.core.exception.BaseServiceException;
import com.quickblox.internal.core.server.BaseService;
import com.quickblox.module.auth.QBAuth;
import com.quickblox.module.auth.model.QBProvider;
import com.quickblox.module.chat.QBChat;
import com.quickblox.module.chat.xmpp.LoginListener;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.module.users.result.QBUserResult;

public class SplashActivity extends FragmentActivity implements QBCallback, Session.StatusCallback {
	
	private DialogFragment quickBloxDialog;
	private ChatApplication app;
	private ProgressDialog progress;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.activity_splash);
		
		Button facebookButton = (Button) findViewById(R.id.splash_facebook_button);
		Button registrationButton = (Button) findViewById(R.id.splash_registration_button);
		Button siginButton = (Button) findViewById(R.id.splash_sign_in_button);
		
		progress = ProgressDialog.show(this, getResources().getString(R.string.app_name), getResources().getString(R.string.splash_progressdialog), true);
		
		OnClickListener clickButtonListener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				switch (v.getId()) {
					case R.id.splash_facebook_button :
						if (!isOnline()) { break;}
						onFbClickLogin();
						break;
					
					case R.id.splash_registration_button :
						if (!isOnline()) { break;}
						quickBloxDialog = new SplashDialog(true);
						quickBloxDialog.show(getSupportFragmentManager(), null);
						break;
					
					case R.id.splash_sign_in_button :
						if (!isOnline()) { break;}
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
			
			app = ChatApplication.getInstance();
			
			QBAuth.createSession(new QBCallbackImpl() {
				@Override
				public void onComplete(Result arg0) {
					progress.dismiss();
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
	public void onComplete(Result arg0) {
	}
	
	@Override
	public void onComplete(Result arg0, Object arg1) {
		
		QBUserResult result = (QBUserResult) arg0;
		
		if (result == null || result.getUser() == null) {
			return;
		}
		
		QBUser currentUser = result.getUser();
		
		if (arg1.equals("social")) {
			// Set Chat password
			try {
				currentUser.setPassword(BaseService.getBaseService().getToken());
			} catch (BaseServiceException e) {
				e.printStackTrace();
			}
		} else {
			app.setAuthUser(currentUser);
		}
		
		// LOGIN IN XMMP
		QBChat.loginWithUser(currentUser, new LoginListener() {
			
			@Override
			public void onLoginError() {
				progress.dismiss();
				Toast.makeText(SplashActivity.this, getResources().getString(R.string.splash_login_reject), Toast.LENGTH_LONG).show();
			}
			
			@Override
			public void onLoginSuccess() {
				
				Intent intent = new Intent(SplashActivity.this, MainTabActivity.class);
				startActivity(intent);
				finish();
			}
			
		});
		
	}
	
	// FACEBOOK CALLBACK
	@Override
	public void call(Session session, SessionState state, Exception exception) {
		QBUsers.signInUsingSocialProvider(QBProvider.FACEBOOK, session.getAccessToken(), null, this, "social");
		
		progress = ProgressDialog.show(this, getResources().getString(R.string.app_name), getResources().getString(R.string.splash_progressdialog), true);
		
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
