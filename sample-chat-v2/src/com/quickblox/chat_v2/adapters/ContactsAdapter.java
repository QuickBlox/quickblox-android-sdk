package com.quickblox.chat_v2.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.core.CustomCheckBoxListener;
import com.quickblox.module.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

public class ContactsAdapter extends ArrayAdapter<QBUser> {

    private final boolean isInviteList;

    private final List<Integer> checkedUsers = new ArrayList<Integer>();

    public ContactsAdapter(Context context, List<QBUser> pList, boolean isInviteList) {
        super(context, 0, pList);
        this.isInviteList = isInviteList;
    }

    private static class ContactHolder {

        public ImageView userPic;
        public TextView userName;
        public CheckBox selected;
    }

    public List<Integer> getCheckedUsers() {
        return checkedUsers;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {

        QBUser currentUser = getItem(position);

        ContactHolder contactsHolder;
        if (convertView == null) {
            contactsHolder = new ContactHolder();
            convertView = View.inflate(getContext(), R.layout.contacts_list_inside, null);
            contactsHolder.userPic = (ImageView) convertView.findViewById(R.id.contacts_inside_userpic);
            contactsHolder.userName = (TextView) convertView.findViewById(R.id.contacts_inside_username);
            contactsHolder.selected = (CheckBox) convertView.findViewById(R.id.is_selected_to_invite);
            convertView.setTag(contactsHolder);
        } else {
            contactsHolder = (ContactHolder) convertView.getTag();
        }
        contactsHolder.userName.setText(currentUser.getFullName() != null ? currentUser.getFullName() : currentUser.getLogin());

        CustomCheckBoxListener onCheck = new CustomCheckBoxListener() {

            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                super.onCheckedChanged(compoundButton, b);
                Integer tUserId = getItem(position).getId();
                if (b) {
                    checkedUsers.add(tUserId);
                } else {
                    checkedUsers.remove(tUserId);
                }
            }
        };

        if (!isInviteList) {
            contactsHolder.selected.setVisibility(View.GONE);
        } else {
            contactsHolder.selected.setVisibility(View.VISIBLE);
            contactsHolder.selected.setOnCheckedChangeListener(onCheck);
        }
        return convertView;
    }
}