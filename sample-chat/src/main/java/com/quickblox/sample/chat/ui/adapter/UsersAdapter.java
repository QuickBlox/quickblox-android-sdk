package com.quickblox.sample.chat.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.quickblox.sample.chat.R;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

public class UsersAdapter extends BaseAdapter {
    private List<QBUser> users;
    private LayoutInflater inflater;
    private List<QBUser> selectedUsers;

    public UsersAdapter(Context context, List<QBUser> users) {
        this.users = users;
        this.selectedUsers = new ArrayList<>();
        this.inflater = LayoutInflater.from(context);
    }

    public List<QBUser> getSelectedUsers() {
        return selectedUsers;
    }

    public List<QBUser> getUsers() {
        return users;
    }

    @Override
    public int getCount() {
        return users.size();
    }

    @Override
    public Object getItem(int position) {
        return users.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_user, parent, false);
            holder = new ViewHolder();
            holder.loginTextView = (TextView) convertView.findViewById(R.id.text_user_login);
            holder.userCheckBox = (CheckBox) convertView.findViewById(R.id.checkbox_user);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final QBUser user = users.get(position);
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.userCheckBox.setChecked(!holder.userCheckBox.isChecked());
                if (holder.userCheckBox.isChecked()) {
                    selectedUsers.add(user);
                } else {
                    selectedUsers.remove(user);
                }
            }
        });

        holder.loginTextView.setText(user.getLogin());
        holder.userCheckBox.setChecked(selectedUsers.contains(user));
        return convertView;
    }

    private static class ViewHolder {
        TextView loginTextView;
        CheckBox userCheckBox;
    }
}
