package com.quickblox.sample.videochatwebrtcnew.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.quickblox.sample.videochatwebrtcnew.Opponent;
import com.quickblox.sample.videochatwebrtcnew.R;
import com.quickblox.sample.videochatwebrtcnew.User;
import com.quickblox.sample.videochatwebrtcnew.activities.ListUsersActivity;

import java.util.ArrayList;

/**
 * Created by tereha on 27.01.15.
 */
public class OpponentsAdapter extends BaseAdapter {

    private ArrayList<User> opponents;
    private LayoutInflater inflater;

    public static ArrayList<String> positions;

    public OpponentsAdapter(Context context, ArrayList<User> result) {
        opponents = result;
        inflater = LayoutInflater.from(context);
    }

    public int getCount() {
        return opponents.size();
    }

    public User getItem(int position) {
        return opponents.get(position);
    }

    public long getItemId(int position) {
        return position;
    }




    public View getView(final int position, View convertView, final ViewGroup parent) {
        positions = new ArrayList<>();
        final ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_opponents, null);
            holder = new ViewHolder();
            holder.opponentsNumber = (TextView) convertView.findViewById(R.id.opponentsNumber);
            holder.opponentsName = (TextView) convertView.findViewById(R.id.opponentsName);
            holder.opponentsCheckBox = (CheckBox) convertView.findViewById(R.id.opponentsCheckBox);

            convertView.setTag(holder);

            holder.opponentsCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if (buttonView.isChecked()) {
                        Log.d("Track", "Checked " + opponents.get(position).getUserNumber());
                        positions.add(String.valueOf(opponents.get(position).getUserNumber()));
                    } else if (!buttonView.isChecked()) {
                        positions.remove(String.valueOf(opponents.get(position).getUserNumber()));
                        Log.d("Track", "Remove " + opponents.get(position).getUserNumber());
                    }
                }
            });

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.opponentsNumber.setText(String.valueOf(opponents.get(position).getUserNumber()));
        holder.opponentsNumber.setBackgroundResource(ListUsersActivity.resourceSelector(opponents.get(position).getUserNumber()));
        holder.opponentsName.setText(opponents.get(position).getFullName());

        return convertView;
    }

    public static class ViewHolder {
        TextView opponentsNumber;
        TextView opponentsName;
        CheckBox opponentsCheckBox;
    }
}