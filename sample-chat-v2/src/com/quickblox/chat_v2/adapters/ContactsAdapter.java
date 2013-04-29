package com.quickblox.chat_v2.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.quickblox.chat_v2.R;
import com.quickblox.module.users.model.QBUser;

public class ContactsAdapter extends BaseAdapter {
	
	private Context context;
	private LayoutInflater inflater;
	
	private List<QBUser> incomeUserList; 
	private boolean isContacts;
	
	private ContactHolder chatHolder;
	
	public ContactsAdapter(Context context, ArrayList<QBUser> qbuserArray, boolean isContacts) {
		this.context = context;
		incomeUserList = qbuserArray;
		this.isContacts = isContacts;
		
	}
	
	static class ContactHolder {
	
		public ImageView userPic;
		public TextView userName;
		
		public Button accept;
		public Button reject;
	}
	
	@Override
	public int getCount() {
		return incomeUserList.size();
	}
	
	@Override
	public Object getItem(int num) {
		return incomeUserList.get(num);
	}
	
	@Override
	public long getItemId(int id) {
		return id;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View contactView = convertView;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		QBUser currentUser = incomeUserList.get(position);
		
		if (contactView == null) {
			
			chatHolder = new ContactHolder();
			contactView = inflater.inflate(R.layout.contacts_list_inside, parent, false);
			chatHolder.userPic = (ImageView) contactView.findViewById(R.id.contacts_inside_userpic);
			chatHolder.userName = (TextView) contactView.findViewById(R.id.contacts_inside_username);
			
			chatHolder.accept = (Button) contactView.findViewById(R.id.contact_iside_accept);
			chatHolder.reject = (Button) contactView.findViewById(R.id.contact_inside_reject);
			
				
			chatHolder.userName.setText(currentUser.getFullName() != null ? currentUser.getFullName() : currentUser.getLogin());

			contactView.setTag(chatHolder);
			
		} else {
			
			chatHolder = (ContactHolder) contactView.getTag();
			
			chatHolder.userName.setText(currentUser.getFullName() != null ? currentUser.getFullName() : currentUser.getLogin());

		}
		
		
		if (isContacts) {
			LinearLayout insideLayout = (LinearLayout) contactView.findViewById(R.id.contacts_linearlayout_two);
			insideLayout.setVisibility(View.GONE);
		} else{
			LinearLayout insideLayout = (LinearLayout) contactView.findViewById(R.id.contacts_linearlayout_two);
			insideLayout.setVisibility(View.VISIBLE);
		}		
		
		
		return contactView;
	}
}