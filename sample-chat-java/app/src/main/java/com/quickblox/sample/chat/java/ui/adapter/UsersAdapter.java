package com.quickblox.sample.chat.java.ui.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.quickblox.chat.QBChatService;
import com.quickblox.sample.chat.java.R;
import com.quickblox.sample.chat.java.utils.ResourceUtils;
import com.quickblox.sample.chat.java.utils.UiUtils;
import com.quickblox.users.model.QBUser;

import java.util.List;

public class UsersAdapter extends BaseAdapter {

    protected List<QBUser> userList;
    protected QBUser currentUser;
    private Context context;

    public UsersAdapter(Context context, List<QBUser> users) {
        this.context = context;
        currentUser = QBChatService.getInstance().getUser();
        userList = users;
    }

    public void addNewList(List<QBUser> users) {
        userList.clear();
        userList.addAll(users);
        for (QBUser user : users) {
            if (isUserMe(user)) {
                userList.remove(user);
            }
        }
        notifyDataSetChanged();
    }

    public void addUsers(List<QBUser> users) {
        for (QBUser user : users) {
            if (!userList.contains(user)) {
                userList.add(user);
            }
        }
        notifyDataSetChanged();
    }

    public void removeUsers(List<QBUser> users) {
        for (QBUser user : users) {
            userList.remove(user);
        }
        notifyDataSetChanged();
    }

    public void clearList() {
        userList.clear();
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        QBUser user = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_user, parent, false);
            holder = new ViewHolder();
            holder.rootLayout = convertView.findViewById(R.id.item_root_layout);
            holder.userImageView = convertView.findViewById(R.id.image_user);
            holder.loginTextView = convertView.findViewById(R.id.text_user_login);
            holder.userCheckBox = convertView.findViewById(R.id.checkbox_user);
            holder.userAvatarTitle = convertView.findViewById(R.id.text_user_avatar_title);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String userName = TextUtils.isEmpty(user.getFullName()) ? user.getLogin() : user.getFullName();

        if (isUserMe(user)) {
            holder.loginTextView.setText(context.getString(R.string.placeholder_username_you, userName));
        } else {
            holder.loginTextView.setText(userName);
        }

        if (isAvailableForSelection(user)) {
            holder.loginTextView.setTextColor(ResourceUtils.getColor(R.color.text_color_black));
        } else {
            holder.loginTextView.setTextColor(ResourceUtils.getColor(R.color.text_color_medium_grey));
        }

        holder.userImageView.setBackgroundDrawable(UiUtils.getColorCircleDrawable(user.getId().hashCode()));
        holder.userCheckBox.setVisibility(View.GONE);

        if (!TextUtils.isEmpty(user.getFullName())) {
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

    private boolean isUserMe(QBUser user) {
        return currentUser != null && currentUser.getId().equals(user.getId());
    }

    protected boolean isAvailableForSelection(QBUser user) {
        return currentUser == null || !currentUser.getId().equals(user.getId());
    }

    protected static class ViewHolder {
        LinearLayout rootLayout;
        ImageView userImageView;
        TextView loginTextView;
        CheckBox userCheckBox;
        TextView userAvatarTitle;
    }
}