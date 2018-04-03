package com.quickblox.sample.conference.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.quickblox.sample.conference.R;
import com.quickblox.sample.core.ui.adapter.BaseListAdapter;
import com.quickblox.sample.core.utils.ResourceUtils;
import com.quickblox.sample.core.utils.UiUtils;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Roman on 05.03.2017.
 */

public class CheckboxUsersAdapter extends BaseListAdapter<QBUser> {
    private QBUser currentUser;

    private List<Integer> initiallySelectedUsers;
    private List<QBUser> selectedUsers;

    public CheckboxUsersAdapter(Context context, List<QBUser> users, QBUser currentUser) {
        super(context, users);
        this.currentUser = currentUser;
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
        final QBUser user = getItem(position);

        final ViewHolder holder;
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
            holder.loginTextView.setText(context.getString(R.string.placeholder_username_you, user.getFullName()));
        } else {
            holder.loginTextView.setText(user.getFullName());
        }

        if (isAvailableForSelection(user)) {
            holder.loginTextView.setTextColor(ResourceUtils.getColor(R.color.text_color_black));
        } else {
            holder.loginTextView.setTextColor(ResourceUtils.getColor(R.color.text_color_medium_grey));
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
