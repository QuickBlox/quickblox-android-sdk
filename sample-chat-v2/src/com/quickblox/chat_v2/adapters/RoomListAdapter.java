package com.quickblox.chat_v2.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.utils.GlobalConsts;
import com.quickblox.module.custom.model.QBCustomObject;

import java.util.ArrayList;
import java.util.List;

public class RoomListAdapter extends ArrayAdapter<QBCustomObject> {

    private LayoutInflater inflater;
    private List<QBCustomObject> roomList;

    public RoomListAdapter(Context context, ArrayList<QBCustomObject> roomList) {
        super(context, 0, roomList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = View.inflate(getContext(), R.layout.room_list_item, null);
            viewHolder = new ViewHolder();


            viewHolder.roomName = (TextView) convertView.findViewById(R.id.room_name_tv);
            viewHolder.onlineUsersCount = (TextView) convertView.findViewById(R.id.online_user_count_tv);

            convertView.setTag(viewHolder);
        } else {

            viewHolder = (ViewHolder) convertView.getTag();
        }

        applyRoomName(viewHolder.roomName, position);
        return convertView;
    }

    private void applyRoomName(TextView roomNameTv, int position) {
        roomNameTv.setText(getItem(position).getFields().get(GlobalConsts.ROOM_LIST_NAME).toString());
    }

    private static class ViewHolder {
        TextView roomName;
        TextView onlineUsersCount;
    }
}
