package com.quickblox.sample.user.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.quickblox.sample.user.R;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserListAdapter extends BaseAdapter {

    private LayoutInflater layoutInflater;
    private List<QBUser> qbUsersList;

    //TODO if we need List<QBUser> it's better to receive List as parameter to don't do unnecessary work
    public UserListAdapter(Context context, Map<Integer, QBUser> qbUsersMap) {
        layoutInflater = LayoutInflater.from(context);
        updateData(qbUsersMap);
    }

    @Override
    public int getCount() {
        return qbUsersList.size();
    }

    @Override
    public QBUser getItem(int position) {
        return qbUsersList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_item_user, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.userName = (TextView) convertView.findViewById(R.id.user_name_text_item_view);
            viewHolder.fullName = (TextView) convertView.findViewById(R.id.full_name_text_item_view);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        QBUser qbUser = getItem(position);
        viewHolder.userName.setText(qbUser.getLogin());
        viewHolder.fullName.setText(qbUser.getFullName());
        return convertView;
    }

    public void updateData(Map<Integer, QBUser> qbUsersMap) {
        this.qbUsersList = new ArrayList<>(qbUsersMap.values());
        notifyDataSetChanged();
    }

    public static class ViewHolder {
        TextView userName;
        TextView fullName;
    }
}