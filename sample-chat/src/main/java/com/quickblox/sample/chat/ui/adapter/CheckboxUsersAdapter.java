package com.quickblox.sample.chat.ui.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

public class CheckboxUsersAdapter extends UsersAdapter {

    private List<QBUser> selectedUsers;

    public CheckboxUsersAdapter(Context context, List<QBUser> users) {
        super(context, users);
        this.selectedUsers = new ArrayList<>();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        final QBUser user = (QBUser) getItem(position);
        final ViewHolder holder = (ViewHolder) view.getTag();

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isUserMe(user)) {
                    return;
                }

                holder.userCheckBox.setChecked(!holder.userCheckBox.isChecked());
                if (holder.userCheckBox.isChecked()) {
                    selectedUsers.add(user);
                } else {
                    selectedUsers.remove(user);
                }
            }
        });

        holder.userCheckBox.setVisibility(View.VISIBLE);
        holder.userCheckBox.setChecked(selectedUsers.contains(user));

        return view;
    }

    public List<QBUser> getSelectedUsers() {
        return selectedUsers;
    }
}
