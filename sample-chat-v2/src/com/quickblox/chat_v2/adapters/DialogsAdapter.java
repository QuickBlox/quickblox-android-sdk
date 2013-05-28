package com.quickblox.chat_v2.adapters;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.core.ChatApplication;
import com.quickblox.chat_v2.ui.activities.ChatActivity;
import com.quickblox.chat_v2.utils.GlobalConsts;
import com.quickblox.module.custom.model.QBCustomObject;
import com.quickblox.module.users.model.QBUser;

/**
 * Created with IntelliJ IDEA. User: Andrew Dmitrenko Date: 4/11/13 Time: 2:27
 * PM
 */
public class DialogsAdapter extends BaseAdapter {
	
	private LayoutInflater layoutInflater;
	private List<QBCustomObject> dialogList;
	private Context context;
	
	public DialogsAdapter(Context context, List<QBCustomObject> dialogList) {
		layoutInflater = LayoutInflater.from(context);
		this.dialogList = dialogList;
		this.context = context;
	}
	
	@Override
	public int getCount() {
		return dialogList.size();
	}
	
	@Override
	public Object getItem(int position) {
		return null;
	}
	
	@Override
	public long getItemId(int position) {
		return 0;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			convertView = layoutInflater.inflate(R.layout.dialog_list_item, parent, false);
			viewHolder = new ViewHolder();
			viewHolder.userAvatar = (ImageView) convertView.findViewById(R.id.user_avatar);
			viewHolder.dialogName = (TextView) convertView.findViewById(R.id.dialog_name);
			viewHolder.dialogLastMsg = (TextView) convertView.findViewById(R.id.dialog_last_msg);
			viewHolder.container = (RelativeLayout) convertView.findViewById(R.id.container);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		
		viewHolder.dialogName.setText(dialogList.get(position).getFields().get(GlobalConsts.ROOM_NAME).toString());
		
		viewHolder.container.setTag(position);
		viewHolder.container.setOnClickListener(dialogClickListener);
		
		Object lastMsg = dialogList.get(position).getFields().get(GlobalConsts.LAST_MSG);
		if (lastMsg != null) {
			viewHolder.dialogLastMsg.setText(lastMsg.toString());
		}
		Object userAvatarUrl = dialogList.get(position).getFields().get(GlobalConsts.RECEPIENT_AVATAR);
		if (userAvatarUrl != null) {
			applyAvatar(viewHolder.userAvatar, userAvatarUrl.toString());
		}
		return convertView;
	}
	
	View.OnClickListener dialogClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			int position = (Integer) v.getTag();

            String userId = dialogList.get(position).getFields().get(GlobalConsts.RECEPIENT_ID_FIELD).toString();
			loadChatActivity(userId, dialogList.get(position).getCustomObjectId());
		}
	};
	
	private void loadChatActivity(String userId, String dialogId) {
		Intent intent = new Intent(context, ChatActivity.class);
		intent.putExtra(GlobalConsts.PREVIOUS_ACTIVITY, GlobalConsts.DIALOG_ACTIVITY);
		intent.putExtra(GlobalConsts.USER_ID, userId);
		intent.putExtra(GlobalConsts.DIALOG_ID, dialogId);
		context.startActivity(intent);
	}
	
	private void applyAvatar(ImageView userAvatar, String userAvatarUrl) {
		DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory().cacheOnDisc().imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
				.displayer(new RoundedBitmapDisplayer(20)).build();
		// Load and display image
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context).defaultDisplayImageOptions(options).build();
		ImageLoader.getInstance().init(config);
		ImageLoader.getInstance().displayImage(userAvatarUrl, userAvatar);
	}
	
	public static class ViewHolder {
		ImageView userAvatar;
		TextView dialogName;
		TextView dialogLastMsg;
		RelativeLayout container;
	}
}
