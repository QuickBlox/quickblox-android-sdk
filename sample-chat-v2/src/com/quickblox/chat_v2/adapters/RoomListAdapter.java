package com.quickblox.chat_v2.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.utils.GlobalConsts;
import com.quickblox.module.custom.model.QBCustomObject;

import java.util.ArrayList;

public class RoomListAdapter extends ArrayAdapter<QBCustomObject> {

    private RoomViewHolder viewHolder;

    public RoomListAdapter(Context context, ArrayList<QBCustomObject> roomList) {
        super(context, 0, roomList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        if (convertView == null) {
            convertView = View.inflate(getContext(), R.layout.room_list_item, null);
            viewHolder = new RoomViewHolder();


            viewHolder.roomName = (TextView) convertView.findViewById(R.id.room_name_tv);
            viewHolder.connectinRoomProgress = (ProgressBar) convertView.findViewById(R.id.room_tap_progress);

            convertView.setTag(viewHolder);
        } else {

            viewHolder = (RoomViewHolder) convertView.getTag();
        }

        applyRoomName(viewHolder.roomName, position);
        return convertView;
    }

    private void applyRoomName(TextView roomNameTv, int position) {
        roomNameTv.setText(getItem(position).getFields().get(GlobalConsts.ROOM_LIST_NAME).toString());
    }

    public static class RoomViewHolder {
        TextView roomName;
        public ProgressBar connectinRoomProgress;
    }
}
