package com.quickblox.sample.videochatwebrtcnew.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;

import com.quickblox.sample.videochatwebrtcnew.R;
import com.quickblox.sample.videochatwebrtcnew.activities.ListUsersActivity;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tereha on 27.01.15.
 */
public class OpponentsAdapter extends BaseAdapter {

    private List<QBUser> opponents;
    private LayoutInflater inflater;
    public static int i;
    public List<QBUser> selected = new ArrayList<>();
    private String TAG = "OpponentsAdapte";

    public OpponentsAdapter(Context context, List<QBUser> users) {
        Log.d(TAG, "On crate i:" + i);
        this.opponents = users;
        this.inflater = LayoutInflater.from(context);

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
            // REMOVE COMMENT BELOW FOR 1 to 1 calls
//            holder.opponentsRadioButton = (RadioButton) convertView.findViewById(R.id.opponentsCheckBox);
            holder.opponentsCheckBoxButton = (CheckBox) convertView.findViewById(R.id.opponentsCheckBox);


            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final QBUser user = opponents.get(position);


        if (user != null) {

            holder.opponentsNumber.setText(String.valueOf(ListUsersActivity.getUserIndex(user.getId())));

            holder.opponentsNumber.setBackgroundResource(ListUsersActivity.resourceSelector
                    (ListUsersActivity.getUserIndex(user.getId())));
            holder.opponentsName.setText(user.getFullName());

            holder.opponentsCheckBoxButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selected.add(user);
                }
            });


            // REMOVE COMMENT BELOW FOR 1 to 1 calls
//            holder.opponentsRadioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//
//                    if (isChecked) {
//                        i = user.getId();
//                        Log.d(TAG, "Button state:" + isChecked + " i:" + i);
//                        selected.removeAll(selected);
//                        selected.add(user);
//                        Log.d(TAG, "Selected " + user.getFullName());
//                    } else {
//                        if (i == user.getId()) {
//                            i = 0;
//                        }
//                        Log.d(TAG, "Button state:" + isChecked + " i:" + i);
//                        selected.remove(user);
//                        holder.opponentsRadioButton.setChecked(false);
////                        selected.removeAll(selected);
//                        Log.d(TAG, "Deselected " + user.getFullName());
//                    }
//                    notifyDataSetChanged();
//                }
//            });
            Log.d(TAG, "Method getView. i = " + i + "");
            Log.d(TAG, "Method getView. User id" + user.getId() + "");
            // REMOVE COMMENT BELOW FOR 1 to 1 calls
//            holder.opponentsRadioButton.setChecked(i == user.getId());

            if (selected.contains(user)){
                holder.opponentsCheckBoxButton.setSelected(true);
            }

        }

        return convertView;
    }


    public static class ViewHolder {
        TextView opponentsNumber;
        TextView opponentsName;
        // REMOVE COMMENT BELOW FOR 1 to 1 calls
//        RadioButton opponentsRadioButton;
        public CheckBox opponentsCheckBoxButton;
    }
}
