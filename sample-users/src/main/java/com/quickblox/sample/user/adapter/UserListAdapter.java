package com.quickblox.sample.user.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.view.View;
import android.view.ViewGroup;

import com.quickblox.sample.core.ui.adapter.BaseListAdapter;
import com.quickblox.sample.user.R;
import com.quickblox.sample.user.databinding.ListItemUserBinding;
import com.quickblox.users.model.QBUser;

import java.util.List;

public class UserListAdapter extends BaseListAdapter<QBUser> {

    public UserListAdapter(Context context, List<QBUser> qbUsersList) {
        super(context, qbUsersList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_user, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        QBUser qbUser = getItem(position);
        viewHolder.userBinding.setUser(qbUser);
        return convertView;
    }

    public static class ViewHolder {
        ListItemUserBinding userBinding;

        public ViewHolder(View v) {
            userBinding = DataBindingUtil.bind(v);
        }
    }
}