package com.quickblox.chat_v2.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.quickblox.chat_v2.R;
import com.quickblox.module.chat.model.QBChatRoom;
import org.jivesoftware.smack.XMPPException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew Dmitrenko
 * Date: 4/11/13
 * Time: 11:45 AM
 */
public class RoomListAdapter extends BaseAdapter {

    LayoutInflater layoutInflater;
    List<QBChatRoom> roomList;
    Context context;

    public RoomListAdapter(Context context, Collection<QBChatRoom> roomList) {
        layoutInflater = LayoutInflater.from(context);
        this.roomList = new ArrayList(roomList);
    }

    @Override
    public int getCount() {
        return roomList.size();
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
            viewHolder = new ViewHolder();
            convertView = layoutInflater.inflate(R.layout.room_list_item, parent, false);
            viewHolder.roomName = (TextView) convertView.findViewById(R.id.room_name_tv);
            viewHolder.userCount = (TextView) convertView.findViewById(R.id.user_count_tv);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.roomName.setText(roomList.get(position).getJid());
        try {
            int onlineUserCount = roomList.get(position).getOnlineRoomUsers().size();
            viewHolder.userCount.setText(onlineUserCount + " " + context.getString(R.string.users_text));
        } catch (XMPPException e) {
            e.printStackTrace();
            viewHolder.userCount.setVisibility(View.GONE);
        }

        return convertView;
    }

    public static class ViewHolder {
        TextView roomName;
        TextView userCount;
    }
}
