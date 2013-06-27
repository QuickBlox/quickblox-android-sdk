package com.quickblox.chat_v2.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.core.ChatApplication;
import com.quickblox.chat_v2.core.CustomCheckBoxListener;
import com.quickblox.module.users.model.QBUser;

import java.util.ArrayList;

public class ContactsAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;

    private ArrayList<QBUser> incomeUserList;
    private boolean isContacts;
    private boolean isInviteList;

    private ContactHolder contactsHolder;
    private ChatApplication app;

    public ContactsAdapter(Context context, ArrayList<QBUser> qbuserArray, boolean isInviteList) {
        this.context = context;
        incomeUserList = qbuserArray;
        this.isContacts = isContacts;
        this.isInviteList = isInviteList;
        app = ChatApplication.getInstance();
    }

    static class ContactHolder {

        public ImageView userPic;
        public TextView userName;

        public Button accept;
        public Button reject;

        public CheckBox selected;
    }

    @Override
    public int getCount() {
        return incomeUserList.size();
    }

    @Override
    public Object getItem(int num) {
        return incomeUserList.get(num);
    }

    @Override
    public long getItemId(int id) {
        return id;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        View contactView = convertView;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        QBUser currentUser = incomeUserList.get(position);

        if (contactView == null) {

            contactsHolder = new ContactHolder();
            contactView = inflater.inflate(R.layout.contacts_list_inside, parent, false);
            contactsHolder.userPic = (ImageView) contactView.findViewById(R.id.contacts_inside_userpic);
            contactsHolder.userName = (TextView) contactView.findViewById(R.id.contacts_inside_username);

            contactsHolder.selected = (CheckBox) contactView.findViewById(R.id.is_selected_to_invite);

            contactsHolder.userName.setText(currentUser.getFullName() != null ? currentUser.getFullName() : currentUser.getLogin());

            contactView.setTag(contactsHolder);

        } else {

            contactsHolder = (ContactHolder) contactView.getTag();

            contactsHolder.userName.setText(currentUser.getFullName() != null ? currentUser.getFullName() : currentUser.getLogin());

        }

        CustomCheckBoxListener onCheck = new CustomCheckBoxListener() {

            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                super.onCheckedChanged(compoundButton, b);
                int tUserId = incomeUserList.get(this.getPosition()).getId();
                if (b) {
                    app.getInviteUserList().add(String.valueOf(tUserId));
                } else {
                    app.getInviteUserList().remove(String.valueOf(tUserId));
                }
            }
        };

        return contactView;
    }
}