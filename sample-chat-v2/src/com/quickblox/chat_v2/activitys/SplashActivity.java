package com.quickblox.chat_v2.activitys;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

import com.facebook.Session;
import com.facebook.SessionState;
import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.fragment.SplashDialog;
import com.quickblox.chat_v2.others.ChatApplication;
import com.quickblox.chat_v2.others.SampleChatApp;
import com.quickblox.core.QBCallback;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.QBSettings;
import com.quickblox.core.result.Result;
import com.quickblox.module.auth.QBAuth;
import com.quickblox.module.auth.model.QBProvider;
import com.quickblox.module.chat.QBChat;
import com.quickblox.module.chat.xmpp.LoginListener;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.result.QBUserResult;

public class SplashActivity extends FragmentActivity implements QBCallback, Session.StatusCallback {
	
	private DialogFragment quickBloxDialog;
	private ChatApplication app;
	private Context context;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.activity_splash);
		
		Button facebookButton = (Button) findViewById(R.id.splash_facebook_button);
		Button registrationButton = (Button) findViewById(R.id.splash_registration_button);
		Button siginButton = (Button) findViewById(R.id.splash_sign_in_button);
		
		OnClickListener clickButtonListener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				switch (v.getId()) {
					case R.id.splash_facebook_button :
						onFbClickLogin();
						break;
					
					case R.id.splash_registration_button :
						quickBloxDialog = new SplashDialog(true);
						quickBloxDialog.show(getSupportFragmentManager(), null);
						break;
					
					case R.id.splash_sign_in_button :
						quickBloxDialog = new SplashDialog(false);
						quickBloxDialog.show(getSupportFragmentManager(), null);
						break;
				
				}
			}
			
		};
		
		facebookButton.setOnClickListener(clickButtonListener);
		registrationButton.setOnClickListener(clickButtonListener);
		siginButton.setOnClickListener(clickButtonListener);
		
		QBSettings.getInstance().fastConfigInit(getResources().getString(R.string.quickblox_app_id), getResources().getString(R.string.quickblox_auth_key),
				getResources().getString(R.string.quickblox_auth_secret));
		
		app = ChatApplication.getInstance();
		SampleChatApp sa = new SampleChatApp(); 
		
		
		QBAuth.createSession(new QBCallbackImpl() {
			@Override
			public void onComplete(Result arg0) {
				
			}
		});
		
		context = this;
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	}
	
	// FACEBOOK LOGIN
	private void onFbClickLogin() {
		
		System.out.println("Вход");
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
		
		QBUserResult result = (QBUserResult) arg0;
		
		if (result == null || result.getUser() == null) {
			return;
		}
		app.setAuthUser(result.getUser());
		
		QBChat.loginWithUser(app.getAuthUser(), new LoginListener() {
			
			@Override
			public void onLoginError() {
				System.out.println("False");
			}
			
			@Override
			public void onLoginSuccess() {
				
				System.out.println("true");
				Intent intent = new Intent(context, MainTabActivity.class);
				startActivity(intent);
				finish();
			}
			
		});
		
	}
	
	@Override
	public void onComplete(Result arg0, Object arg1) {
		// TODO Auto-generated method stub
		
	}
	
	// FACEBOOK CALLBACK
	@Override
	public void call(Session session, SessionState state, Exception exception) {
		QBUsers.signInUsingSocialProvider(QBProvider.FACEBOOK, session.getAccessToken(), null, this);
	}
	
}
