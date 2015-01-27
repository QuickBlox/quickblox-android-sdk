package com.quickblox.sample.videochatwebrtcnew.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.quickblox.sample.videochatwebrtcnew.R;
import com.quickblox.sample.videochatwebrtcnew.activities.InterlocutorsActivity;


/**
 * Created by tereha on 25.01.15.
 */
public class UsersAdapter extends ArrayAdapter<String> /*implements View.OnClickListener*/ {


    private final Context context;
    private final String[] users;


    public UsersAdapter(Context context, String[] users) {
        super(context, R.layout.list_item_user, users);
        this.context = context;
        this.users = users;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View userListItem = inflater.inflate(R.layout.list_item_user, parent, false);

        TextView numberOfList = (TextView) userListItem.findViewById(R.id.numberOfList);
        numberOfList.setText(String.valueOf(position +1));

        TextView loginAs = (TextView) userListItem.findViewById(R.id.loginAs);
        loginAs.setText(R.string.login_as);

        TextView userName = (TextView) userListItem.findViewById(R.id.userName);
        userName.setText(users[position]);

        return userListItem;

    }


}