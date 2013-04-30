package com.quickblox.chat_v2.ui.activities;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.adapters.RoomListAdapter;
import com.quickblox.chat_v2.core.ChatApplication;

/**
 * Created with IntelliJ IDEA. User: Andrew Dmitrenko Date: 11.04.13 Time: 9:58
 */
public class RoomsActivity extends Activity {
	
	private ListView roomListLv;
	private Button newRoomBtn;
	
	private RoomListAdapter roomListAdapter;
	
	private ChatApplication app;
	
	@Override
	public void onCreate(Bundle savedInstanceBundle) {
		super.onCreate(savedInstanceBundle);
		setContentView(R.layout.rooms_layout);
		app = ChatApplication.getInstance();
		initViews();
	}

	private void initViews() {
		roomListLv = (ListView) findViewById(R.id.room_list_lv);
		newRoomBtn = (Button) findViewById(R.id.new_room_btn);
		newRoomBtn.setOnClickListener(newRoomBtnClickListener);
		applyRoomList(app.getUserPresentRoomList());
	}
	
	private View.OnClickListener newRoomBtnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(RoomsActivity.this, NewRoomActivity.class);
			startActivity(intent);
		}
	};
	
	private void applyRoomList(List<String> roomList) {
		roomListAdapter = new RoomListAdapter(this, roomList);
		roomListLv.setAdapter(roomListAdapter);
		refreshData();
	}
	
	private void refreshData() {
		this.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				System.out.println("refresh");
				roomListAdapter.notifyDataSetChanged();
			}
		});
	}
}
