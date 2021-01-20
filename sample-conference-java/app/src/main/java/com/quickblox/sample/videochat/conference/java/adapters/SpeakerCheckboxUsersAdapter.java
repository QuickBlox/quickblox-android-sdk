package com.quickblox.sample.videochat.conference.java.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.quickblox.sample.videochat.conference.java.App;
import com.quickblox.sample.videochat.conference.java.R;
import com.quickblox.sample.videochat.conference.java.utils.UiUtils;
import com.quickblox.users.model.QBUser;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SpeakerCheckboxUsersAdapter extends BaseAdapter {

    private List<QBUser> userList;
    private QBUser currentUser;
    private Context context;
    private Set<QBUser> selectedUsers;
    private Map<Integer, Boolean> audioEnabledMap;

    public SpeakerCheckboxUsersAdapter(Context context, List<QBUser> users, Map<Integer, Boolean> audioEnabledMap) {
        this.context = context;
        selectedUsers = new HashSet<>();
        this.audioEnabledMap = audioEnabledMap;
        currentUser = ((App) context.getApplicationContext()).getChatHelper().getCurrentUser();
        userList = users;
    }

    public void addUser(QBUser user, boolean enabled) {
        if (!userList.contains(user)) {
            userList.add(user);
            audioEnabledMap.put(user.getId(), enabled);
        }
        notifyDataSetChanged();
    }

    public void removeUser(QBUser user) {
        userList.remove(user);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SpeakerCheckboxUsersAdapter.ViewHolder holder;
        QBUser user = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_mute_user, parent, false);
            holder = new SpeakerCheckboxUsersAdapter.ViewHolder();
            holder.userImageView = convertView.findViewById(R.id.image_user);
            holder.loginTextView = convertView.findViewById(R.id.text_user_login);
            holder.userCheckBox = convertView.findViewById(R.id.checkbox_user);
            holder.userAvatarTitle = convertView.findViewById(R.id.text_user_avatar_title);
            convertView.setTag(holder);
        } else {
            holder = (SpeakerCheckboxUsersAdapter.ViewHolder) convertView.getTag();
        }

        String userName = TextUtils.isEmpty(user.getFullName()) ? user.getLogin() : user.getFullName();

        if (isUserMe(user)) {
            holder.loginTextView.setText(context.getString(R.string.placeholder_username_you, userName));
            holder.loginTextView.setTextColor(context.getResources().getColor(R.color.text_color_medium_grey));
            holder.userCheckBox.setVisibility(View.GONE);
        } else {
            holder.loginTextView.setText(userName);
            holder.loginTextView.setTextColor(context.getResources().getColor(R.color.text_color_black));
        }

        holder.userImageView.setBackground(UiUtils.getColorCircleDrawable(context, user.getId().hashCode()));

        boolean isEnabled = audioEnabledMap.get(user.getId());
        holder.userCheckBox.setChecked(!isEnabled);

        if (!TextUtils.isEmpty(userName)) {
            String avatarTitle = String.valueOf(user.getFullName().charAt(0)).toUpperCase();
            holder.userAvatarTitle.setText(avatarTitle);
        }

        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return userList.size();
    }

    @Override
    public QBUser getItem(int position) {
        return userList.get(position);
    }

    public void onItemClicked(int position, View convertView) {
        QBUser user = getItem(position);
        ViewHolder holder = (SpeakerCheckboxUsersAdapter.ViewHolder) convertView.getTag();

        if (isUserMe(user)) {
            return;
        }

        boolean isChecked = holder.userCheckBox.isChecked();
        audioEnabledMap.remove(user.getId());
        audioEnabledMap.put(user.getId(), isChecked);
        holder.userCheckBox.setChecked(!isChecked);
        if (holder.userCheckBox.isChecked()) {
            selectedUsers.add(user);
        } else {
            selectedUsers.remove(user);
        }
        notifyDataSetChanged();
    }

    private boolean isUserMe(QBUser user) {
        return currentUser != null && currentUser.getId().equals(user.getId());
    }

    class ViewHolder {
        ImageView userImageView;
        TextView loginTextView;
        CheckBox userCheckBox;
        TextView userAvatarTitle;
    }
}