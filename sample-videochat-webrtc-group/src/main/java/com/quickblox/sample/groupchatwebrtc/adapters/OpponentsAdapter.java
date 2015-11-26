package com.quickblox.sample.groupchatwebrtc.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.quickblox.sample.groupchatwebrtc.activities.ListUsersActivity;
import com.quickblox.sample.groupchatwebrtc.R;
import com.quickblox.sample.groupchatwebrtc.User;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tereha on 27.01.15.
 */
public class OpponentsAdapter extends BaseAdapter {

    private List<User> opponents;
    private LayoutInflater inflater;
    public static int i;
    public List<User> selected = new ArrayList<>();
    private String TAG = "OpponentsAdapte";

    public OpponentsAdapter(Context context, List<User> users) {
        Log.d(TAG, "On crate i:" + i);
        this.opponents = users;
        this.inflater = LayoutInflater.from(context);

    }

    public List<User> getSelected() {
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

    private int getNumber(List<QBUser> opponents, QBUser user) {
        return opponents.indexOf(user);
    }


    public View getView(final int position, View convertView, final ViewGroup parent) {

        final ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_opponents, null);
            holder = new ViewHolder();
            holder.opponentsNumber = (TextView) convertView.findViewById(R.id.opponentsNumber);
            holder.opponentsName = (TextView) convertView.findViewById(R.id.opponentsName);
            holder.opponentsRadioButton = (CheckBox) convertView.findViewById(R.id.opponentsCheckBox);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final User user = opponents.get(position);


        if (user != null) {

            holder.opponentsNumber.setText(String.valueOf(user.getUserNumber()));

            holder.opponentsNumber.setBackgroundResource(ListUsersActivity.resourceSelector
                    (user.getUserNumber()));
            holder.opponentsName.setText(user.getFullName());

            holder.opponentsRadioButton.setOnCheckedChangeListener(null);
            holder.opponentsRadioButton.setChecked(selected.contains(user));

            holder.opponentsRadioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if (isChecked) {
                        i = user.getId();
                        Log.d(TAG, "Button state:" + isChecked + " i:" + i);
                        selected.add(user);
                        Log.d(TAG, "Selected " + user.getFullName());
                    } else {
                        if (i == user.getId()) {
                            i = 0;
                        }
                        Log.d(TAG, "Button state:" + isChecked + " i:" + i);
                        selected.remove(user);
                        Log.d(TAG, "Deselected " + user.getFullName());
                    }
                }
            });


        }

        return convertView;
    }


    public static class ViewHolder {
        TextView opponentsNumber;
        TextView opponentsName;
        CheckBox opponentsRadioButton;
    }
}
