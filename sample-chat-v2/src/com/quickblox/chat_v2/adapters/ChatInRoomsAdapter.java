package com.quickblox.chat_v2.adapters;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.quickblox.chat_v2.R;
import com.quickblox.module.users.model.QBUser;

public class ChatInRoomsAdapter extends BaseAdapter {
	
	private Context context;
	private LayoutInflater inflater;
	
	private List<QBUser> incomeUserList; 
	private boolean isContacts;
	
	private ChatHolder chatHolder;
	
	public ChatInRoomsAdapter(Context context, ArrayList<QBUser> qbuserArray, boolean isContacts) {
		this.context = context;
		incomeUserList = qbuserArray;
		this.isContacts = isContacts;
	}
	
	static class ChatHolder {
	
		public ImageView userPic;
		public TextView userName;
		
		public Button accept;
		public Button reject;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return incomeUserList.size();
	}
	
	@Override
	public Object getItem(int num) {
		return incomeUserList.get(num);
	}
	
	@Override
	public long getItemId(int arg0) {
		return arg0;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View chatView = convertView;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		QBUser currentUser = incomeUserList.get(position);
		
		if (chatView == null) {
			
			chatHolder = new ChatHolder();
			
			chatView = inflater.inflate(R.layout.fragment_contacts_inside, null, true);
			chatHolder.userPic = (ImageView) chatView.findViewById(R.id.contacts_inside_userpic);
			chatHolder.userName = (TextView) chatView.findViewById(R.id.contacts_inside_username);
			
			chatHolder.accept = (Button) chatView.findViewById(R.id.contact_iside_accept);
			chatHolder.reject = (Button) chatView.findViewById(R.id.contact_inside_reject);
			
				
			chatHolder.userName.setText(currentUser.getFullName() != null ? currentUser.getFullName() : currentUser.getLogin());

			chatView.setTag(chatHolder);
			
			
		} else {
			
			chatHolder = (ChatHolder) chatView.getTag();			
			chatHolder.userName.setText(currentUser.getFullName() != null ? currentUser.getFullName() : currentUser.getLogin());

		}
		
		
		if (isContacts) {
			LinearLayout insideLayout = (LinearLayout) chatView.findViewById(R.id.contactslinearlayout);
			insideLayout.setVisibility(View.GONE);
		} else{
			LinearLayout insideLayout = (LinearLayout) chatView.findViewById(R.id.contactslinearlayout);
			insideLayout.setVisibility(View.VISIBLE);
		}		
		
		
		return chatView;
	}
}