package com.quickblox.sample.videochatwebrtcnew.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.quickblox.sample.videochatwebrtcnew.R;
import com.quickblox.sample.videochatwebrtcnew.User;
import com.quickblox.sample.videochatwebrtcnew.activities.ListUsersActivity;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by tereha on 27.01.15.
 */
public class OpponentsAdapter extends BaseAdapter {

    private ArrayList<QBUser> opponents;
    private LayoutInflater inflater;
    private int counter;
    public List<QBUser> selected = new ArrayList<>();
    String lologinnedUser;

    public OpponentsAdapter(Context context, ArrayList<QBUser> users, String loginnedUser) {
        this.opponents = users;
        inflater = LayoutInflater.from(context);
        this.lologinnedUser = loginnedUser;

    }

    public List<QBUser> getSelected() {
        return selected;
    }

    public int getCount() {
        return opponents.size();
    }

    public QBUser getItem(int position) {
        return opponents.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    private int getNumber (ArrayList<QBUser> opponents, QBUser user) {
        int i;
        i = opponents.indexOf(user);

        return i;

    }




    public View getView(final int position, View convertView, final ViewGroup parent) {

        final ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_opponents, null);
            holder = new ViewHolder();
            holder.opponentsNumber = (TextView) convertView.findViewById(R.id.opponentsNumber);
            holder.opponentsName = (TextView) convertView.findViewById(R.id.opponentsName);
            holder.opponentsCheckBox = (CheckBox) convertView.findViewById(R.id.opponentsCheckBox);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final QBUser user = opponents.get(position);

        counter = getNumber(opponents, user) + 1;

        if (user != null) {


            holder.opponentsNumber.setText(String.valueOf(counter));

            holder.opponentsNumber.setBackgroundResource(ListUsersActivity.resourceSelector(counter));
            holder.opponentsName.setText(user.getFullName());
            holder.opponentsCheckBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ((((CheckBox) v).isChecked())) {
                        selected.add(user);
                    } else {
                        selected.remove(user);
                    }
                }
            });

            holder.opponentsCheckBox.setChecked(selected.contains(user));
        }

        return convertView;
    }


    public static class ViewHolder {
        TextView opponentsNumber;
        TextView opponentsName;
        CheckBox opponentsCheckBox;
    }
}