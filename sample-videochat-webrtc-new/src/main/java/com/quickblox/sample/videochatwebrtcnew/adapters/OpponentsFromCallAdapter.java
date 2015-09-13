package com.quickblox.sample.videochatwebrtcnew.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.quickblox.sample.videochatwebrtcnew.R;
import com.quickblox.sample.videochatwebrtcnew.activities.BaseLogginedUserActivity;
import com.quickblox.sample.videochatwebrtcnew.activities.ListUsersActivity;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.view.QBGLVideoView;

import java.util.List;

/**
 * Created by tereha on 24.02.15.
 */
public class OpponentsFromCallAdapter extends BaseAdapter {

    private List<QBUser> opponents;
    private LayoutInflater inflater;

    public OpponentsFromCallAdapter(Context context, List<QBUser> users) {
        this.opponents = users;
        this.inflater = LayoutInflater.from(context);
    }


    @Override
    public int getCount() {
        return opponents.size();
    }

    @Override
    public QBUser getItem(int position) {
        return opponents.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_opponents, null);
            holder = new ViewHolder();
            holder.opponentsNumber = (TextView) convertView.findViewById(R.id.opponentsNumber);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final QBUser user = opponents.get(position);
        if (user != null) {
            holder.opponentsNumber.setText("");
            holder.opponentsNumber.setBackgroundResource(BaseLogginedUserActivity.resourceSelector(position));
        }
        return convertView;
    }


    public static class ViewHolder {
        TextView opponentsNumber;
        TextView connectionStatus;
        QBGLVideoView opponentLittleCamera;
        ImageView opponentAvatar;


    }
}
