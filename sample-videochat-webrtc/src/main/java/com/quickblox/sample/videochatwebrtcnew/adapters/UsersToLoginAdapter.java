package com.quickblox.sample.videochatwebrtcnew.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.quickblox.sample.videochatwebrtcnew.R;
import com.quickblox.sample.videochatwebrtcnew.activities.BaseActivity;
import com.quickblox.sample.videochatwebrtcnew.holder.DataHolder;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;

/**
 * Created by tereha on 25.01.15.
 */

public class UsersToLoginAdapter extends BaseAdapter {
    private ArrayList<QBUser> user;

    private LayoutInflater inflater;

    public UsersToLoginAdapter(Context context, ArrayList<QBUser> results) {
        user = results;
        inflater = LayoutInflater.from(context);
    }

    public int getCount() {
        return user.size();
    }

    public QBUser getItem(int position) {
        return user.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_user, null);
            holder = new ViewHolder();
            holder.userNumber = (TextView) convertView.findViewById(R.id.userNumber);
            holder.loginAs = (TextView) convertView.findViewById(R.id.loginAs);
            holder.fullName = (TextView) convertView.findViewById(R.id.fullName);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.userNumber.setText(String.valueOf(
                DataHolder.getUserIndexByID(user.get(position).getId()) + 1));
        holder.userNumber.setBackgroundResource(BaseActivity.resourceSelector(
                DataHolder.getUserIndexByID(user.get(position).getId()) + 1));
        holder.loginAs.setText(R.string.login_as);
        holder.fullName.setText(user.get(position).getFullName());

        return convertView;
    }

    public static class ViewHolder {
        TextView userNumber;
        TextView loginAs;
        TextView fullName;
    }
}