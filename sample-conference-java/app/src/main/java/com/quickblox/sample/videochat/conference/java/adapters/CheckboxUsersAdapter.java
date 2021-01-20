package com.quickblox.sample.videochat.conference.java.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.quickblox.sample.videochat.conference.java.R;
import com.quickblox.users.model.QBUser;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class CheckboxUsersAdapter extends UsersAdapter {

    private Set<QBUser> selectedUsers;
    private Context context;

    public CheckboxUsersAdapter(Context context, List<QBUser> users) {
        super(context, users);
        selectedUsers = new HashSet<>();
        this.context = context;
    }

    public Set<QBUser> getSelectedUsers() {
        return Collections.unmodifiableSet(selectedUsers);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        final QBUser user = getItem(position);
        final ViewHolder holder = (ViewHolder) view.getTag();

        holder.userCheckBox.setVisibility(View.VISIBLE);
        boolean containsUser = selectedUsers.contains(user);
        holder.userCheckBox.setChecked(containsUser);

        if (containsUser) {
            holder.rootLayout.setBackgroundColor(context.getResources().getColor(R.color.selected_list_item_color));
        } else {
            holder.rootLayout.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
        }

        return view;
    }

    public void onItemClicked(int position, View convertView) {
        QBUser user = getItem(position);
        ViewHolder holder = (UsersAdapter.ViewHolder) convertView.getTag();

        if (isUserMe(user)) {
            return;
        }

        holder.userCheckBox.setChecked(!holder.userCheckBox.isChecked());
        if (holder.userCheckBox.isChecked()) {
            selectedUsers.add(user);
        } else {
            selectedUsers.remove(user);
        }
        notifyDataSetChanged();
    }

    @Override
    public QBUser getItem(int position) {
        return userList.get(position);
    }
}