package com.quickblox.sample.videochatwebrtcnew.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.quickblox.sample.videochatwebrtcnew.Opponent;
import com.quickblox.sample.videochatwebrtcnew.R;
import com.quickblox.sample.videochatwebrtcnew.User;

import java.util.ArrayList;

/**
 * Created by tereha on 27.01.15.
 */
public class OpponentsAdapter extends BaseAdapter {

    private ArrayList<Opponent> opponents;
    private LayoutInflater inflater;

    public OpponentsAdapter(Context context, ArrayList<Opponent> result) {
        opponents = result;
        inflater = LayoutInflater.from(context);
    }

    public int getCount() {
        return opponents.size();
    }

    public Opponent getItem(int position) {
        return opponents.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
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

        holder.opponentsNumber.setText(String.valueOf(opponents.get(position).getOpponentNumber()));
        holder.opponentsName.setText(opponents.get(position).getOpponentName());

        return convertView;
    }

    public static class ViewHolder {
        TextView opponentsNumber;
        TextView opponentsName;
        CheckBox opponentsCheckBox;
    }
}