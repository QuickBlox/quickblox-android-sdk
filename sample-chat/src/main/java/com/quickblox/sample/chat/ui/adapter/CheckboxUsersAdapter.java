package com.quickblox.sample.chat.ui.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

public class CheckboxUsersAdapter extends UsersAdapter {

    private List<Integer> initiallySelectedUsers;
    private List<QBUser> selectedUsers;

    public CheckboxUsersAdapter(Context context, List<QBUser> users) {
        super(context, users);
        this.selectedUsers = new ArrayList<>();
        this.selectedUsers.add(currentUser);

        this.initiallySelectedUsers = new ArrayList<>();
    }

    public void addSelectedUsers(List<Integer> userIds) {
        for (QBUser user : objectsList) {
            for (Integer id : userIds) {
                if (user.getId().equals(id)) {
                    selectedUsers.add(user);
                    initiallySelectedUsers.add(user.getId());
                    break;
                }
            }
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        final QBUser user = getItem(position);
        final ViewHolder holder = (ViewHolder) view.getTag();

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isAvailableForSelection(user)) {
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

    @Override
    protected boolean isAvailableForSelection(QBUser user) {
        return super.isAvailableForSelection(user) && !initiallySelectedUsers.contains(user.getId());
    }
}
