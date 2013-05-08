package com.quickblox.chat_v2.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.utils.GlobalConsts;
import com.quickblox.module.chat.model.QBChatRoom;
import org.jivesoftware.smack.XMPPException;

import java.util.Collection;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew Dmitrenko
 * Date: 4/16/13
 * Time: 8:55 AM
 */
public class RoomListAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private List<QBChatRoom> roomList;

    public RoomListAdapter(Context context, Collection<QBChatRoom> roomList) {
        inflater = LayoutInflater.from(context);
        this.roomList = (List<QBChatRoom>) roomList;
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
            convertView = inflater.inflate(R.layout.room_list_item, parent, false);
            viewHolder.roomName = (TextView) convertView.findViewById(R.id.room_name_tv);
            viewHolder.onlineUsersCount = (TextView) convertView.findViewById(R.id.online_user_count_tv);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        applyRoomName(viewHolder.roomName, position);
        applyOnlineUserCount(viewHolder.onlineUsersCount, position);

        return convertView;
    }

    private void applyOnlineUserCount(TextView onlineUsersCountTv, int position) {
        try {
            int onlineUserCount = roomList.get(position).getOnlineRoomUsers().size();
            onlineUsersCountTv.setText(onlineUserCount + GlobalConsts.USERS_TEXT);
        } catch (XMPPException e) {
            e.printStackTrace();
        }
    }

    private void applyRoomName(TextView roomNameTv, int position) {
        roomNameTv.setText(roomList.get(position).getJid());
    }

    private static class ViewHolder {
        TextView roomName;
        TextView onlineUsersCount;
    }
}
