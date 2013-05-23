package com.quickblox.chat_v2.ui.activities;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.adapters.RoomListAdapter;
import com.quickblox.chat_v2.core.ChatApplication;
import com.quickblox.chat_v2.interfaces.OnRoomListDownloaded;
import com.quickblox.chat_v2.utils.GlobalConsts;
import com.quickblox.module.custom.model.QBCustomObject;

/**
 * Created with IntelliJ IDEA. User: Andrew Dmitrenko Date: 11.04.13 Time: 9:58
 */
public class RoomsActivity extends Activity implements OnRoomListDownloaded {
	
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
	
	@Override
	protected void onResume() {
		super.onResume();
		app.getRstManager().setRoomDownloadedListener(this);
        initViews();
        Log.w("ROOM ACTIVITY", "quant = "+app.getUserPresentRoomList().size());

	}

	private void initViews() {
		roomListLv = (ListView) findViewById(R.id.room_list_lv);
		roomListLv.setOnItemClickListener(itemClick);
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
	
	private void applyRoomList(List<QBCustomObject> roomList) {
		roomListAdapter = new RoomListAdapter(this, roomList);
		roomListLv.setAdapter(roomListAdapter);
		refreshData();
	}
	
	private OnItemClickListener itemClick = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
			
			Intent i = new Intent(RoomsActivity.this, ChatActivity.class);
			i.putExtra(GlobalConsts.PREVIOUS_ACTIVITY, GlobalConsts.ROOM_ACTIVITY);
			i.putExtra(GlobalConsts.ROOM_NAME, app.getUserPresentRoomList().get(position).getFields().get(GlobalConsts.ROOM_LIST_NAME).toString());
			i.putExtra(GlobalConsts.IS_NEW_ROOM, false);
			RoomsActivity.this.startActivity(i);
		}
		
		
	};
	
	private void refreshData() {
		this.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				roomListAdapter.notifyDataSetChanged();
			}
		});
	}

	@Override
	public void roomListDownloaded() {
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				applyRoomList(app.getUserPresentRoomList());
			}
		});
		
	}
}
