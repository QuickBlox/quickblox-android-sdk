package com.quickblox.chat_v2.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.core.ChatApplication;
import com.quickblox.chat_v2.ui.activities.FriendProfileActivity;
import com.quickblox.chat_v2.utils.GlobalConsts;

/**
 * Created with IntelliJ IDEA. User: Andrew Dmitrenko Date: 4/8/13 Time: 3:38 PM
 */
public class TopBar extends RelativeLayout {
	
	public static final String CHAT_ACTIVITY = "Chat";
	public static final String NEW_DIALOG_ACTIVITY = "New Dialog";
	
	private TextView screenTitle;
	private ImageView userAvatar;
	private Context context;
	
	private String fragmentName;
	
	private int friendId;
	
	public TopBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		this.context = context;
		
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.top_bar, null);
		this.addView(view);
		
		initViews(view);
	}
	
	private void initViews(View view) {
		screenTitle = (TextView) view.findViewById(R.id.screen_title);
		userAvatar = (ImageView) view.findViewById(R.id.user_avatar_iv);
		userAvatar.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				System.out.println("Есть нажатие");
				
				String[] data = {context.getResources().getString(R.string.chat_dialog_view_profile),
						context.getResources().getString(R.string.chat_dialog_add_contact)};

				
				AlertDialog.Builder adb = new AlertDialog.Builder(context);
				adb.setTitle(R.string.chat_dialog_name);
				
				adb.setItems(data, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
						switch (which) {
							case 0 :
								Intent i = new Intent(context, FriendProfileActivity.class);
								i.putExtra(GlobalConsts.FRIEND_ID, friendId);
								context.startActivity(i);
								break;
							
							case 1 : 
								ChatApplication.getInstance().getRstManager().sendRequestToSubscribe(friendId);								
								break;
						}
					}
				});

				adb.create().show();
			}
	
		});
		
	}
	
	public void setFragmentParams(String fragmentName, boolean isUserPicVisible) {
		this.fragmentName = fragmentName;
		screenTitle.setText(fragmentName);
		
		if (!isUserPicVisible){
			userAvatar.setVisibility(View.INVISIBLE);
			userAvatar.setClickable(false);
		}
		
		initExtraViews();
	}
	public void setFriendParams(int friendId){
		this.friendId = friendId;
	}
	
	private void initExtraViews() {
		if (fragmentName.equals(CHAT_ACTIVITY)) {
			// TODO load image
		}
	}
}
