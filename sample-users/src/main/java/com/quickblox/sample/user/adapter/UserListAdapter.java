package com.quickblox.sample.user.adapter;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.quickblox.sample.user.R;
import com.quickblox.users.model.QBUser;

public class UserListAdapter extends BaseAdapter {

    private LayoutInflater layoutInflater;
    private SparseArray<QBUser> qbUsersSparseArray;

    public UserListAdapter(Context context, SparseArray<QBUser> qbUsersSparseArray) {
        layoutInflater = LayoutInflater.from(context);
        this.qbUsersSparseArray = qbUsersSparseArray;
    }

    @Override
    public int getCount() {
        return qbUsersSparseArray.size();
    }

    @Override
    public QBUser getItem(int position) {
        return qbUsersSparseArray.valueAt(position);
    }

    @Override
    public long getItemId(int position) {
        return qbUsersSparseArray.keyAt(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_item_user, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.userName = (TextView) convertView.findViewById(R.id.user_name_textview);
            viewHolder.fullName = (TextView) convertView.findViewById(R.id.full_name_textview);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        QBUser qbUser = getItem(position);
        viewHolder.userName.setText(qbUser.getLogin());
        viewHolder.fullName.setText(qbUser.getFullName());
        return convertView;
    }

    public void updateData(SparseArray<QBUser> qbUsersSparseArray) {
        this.qbUsersSparseArray = qbUsersSparseArray;
        notifyDataSetChanged();
    }

    public static class ViewHolder {
        TextView userName;
        TextView fullName;
    }
}