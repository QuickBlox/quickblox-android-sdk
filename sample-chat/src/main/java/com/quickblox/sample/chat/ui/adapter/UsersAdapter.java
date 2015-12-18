package com.quickblox.sample.chat.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.quickblox.sample.chat.R;
import com.quickblox.sample.chat.utils.ChatUtils;
import com.quickblox.sample.chat.utils.UiUtils;
import com.quickblox.sample.core.utils.ResourceUtils;
import com.quickblox.users.model.QBUser;

import java.util.List;

public class UsersAdapter extends BaseAdapter {
    protected Context context;
    protected List<QBUser> users;
    protected LayoutInflater inflater;

    public UsersAdapter(Context context, List<QBUser> users) {
        this.context = context;
        this.users = users;
        this.inflater = LayoutInflater.from(context);
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
        QBUser user = users.get(position);

        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_user, parent, false);
            holder = new ViewHolder();
            holder.userImageView = (ImageView) convertView.findViewById(R.id.image_user);
            holder.loginTextView = (TextView) convertView.findViewById(R.id.text_user_login);
            holder.userCheckBox = (CheckBox) convertView.findViewById(R.id.checkbox_user);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (isUserMe(user)) {
            holder.loginTextView.setTextColor(ResourceUtils.getColor(R.color.text_color_medium_grey));
            holder.loginTextView.setText(context.getString(R.string.placeholder_username_you, user.getLogin()));
        } else {
            holder.loginTextView.setTextColor(ResourceUtils.getColor(R.color.text_color_black));
            holder.loginTextView.setText(user.getLogin());
        }
        holder.userImageView.setBackgroundDrawable(UiUtils.getRandomColorCircleDrawable());
        holder.userCheckBox.setVisibility(View.GONE);

        return convertView;
    }

    protected boolean isUserMe(QBUser user) {
        QBUser currentUser = ChatUtils.getCurrentUser();
        return currentUser != null && currentUser.getId().equals(user.getId());
    }

    protected static class ViewHolder {
        ImageView userImageView;
        TextView loginTextView;
        CheckBox userCheckBox;
    }
}
