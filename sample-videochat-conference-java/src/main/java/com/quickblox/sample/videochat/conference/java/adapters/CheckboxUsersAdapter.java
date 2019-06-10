package com.quickblox.sample.videochat.conference.java.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.quickblox.sample.videochat.conference.java.R;
import com.quickblox.sample.videochat.conference.java.utils.UiUtils;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;


public class CheckboxUsersAdapter extends BaseAdapter {

    private QBUser currentUser;
    private List<Integer> initiallySelectedUsers;
    private List<QBUser> selectedUsers;
    private LayoutInflater inflater;
    private Context context;
    private List<QBUser> userList;

    public CheckboxUsersAdapter(Context context, List<QBUser> users, QBUser currentUser) {
        this.context = context;
        userList = users;
        inflater = LayoutInflater.from(context);
        this.currentUser = currentUser;
        selectedUsers = new ArrayList<>();
        selectedUsers.add(currentUser);
        initiallySelectedUsers = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return userList != null ? userList.size() : 0;
    }

    @Override
    public QBUser getItem(int position) {
        return userList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void updateList(List<QBUser> newData) {
        userList = newData;
        notifyDataSetChanged();
    }

    public void add(QBUser item) {
        userList.add(item);
        notifyDataSetChanged();
    }

    public List<QBUser> getList() {
        return userList;
    }

    public void addList(List<QBUser> items) {
        userList.addAll(0, items);
        notifyDataSetChanged();
    }

    public void remove(QBUser item) {
        userList.remove(item);
        notifyDataSetChanged();
    }

    public void addSelectedUsers(List<Integer> userIds) {
        for (QBUser user : userList) {
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
        final QBUser user = getItem(position);

        final ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_user, parent, false);
            holder = new ViewHolder();
            holder.userImageView = convertView.findViewById(R.id.image_user);
            holder.loginTextView = convertView.findViewById(R.id.text_user_login);
            holder.userCheckBox = convertView.findViewById(R.id.checkbox_user);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (isUserMe(user)) {
            holder.loginTextView.setText(context.getString(R.string.placeholder_username_you, user.getFullName()));
        } else {
            holder.loginTextView.setText(user.getFullName());
        }

        if (isAvailableForSelection(user)) {
            holder.loginTextView.setTextColor(context.getResources().getColor(R.color.text_color_black));
        } else {
            holder.loginTextView.setTextColor(context.getResources().getColor(R.color.text_color_medium_grey));
        }

        holder.userImageView.setBackgroundDrawable(UiUtils.getColorCircleDrawable(position));
        holder.userCheckBox.setVisibility(View.GONE);

        convertView.setOnClickListener(new View.OnClickListener() {
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

        return convertView;
    }

    protected boolean isUserMe(QBUser user) {
        return currentUser != null && currentUser.getId().equals(user.getId());
    }

    public List<QBUser> getSelectedUsers() {
        return selectedUsers;
    }

    protected boolean isAvailableForSelection(QBUser user) {
        return currentUser == null || !currentUser.getId().equals(user.getId()) && !initiallySelectedUsers.contains(user.getId());
    }

    protected static class ViewHolder {
        ImageView userImageView;
        TextView loginTextView;
        CheckBox userCheckBox;
    }
}