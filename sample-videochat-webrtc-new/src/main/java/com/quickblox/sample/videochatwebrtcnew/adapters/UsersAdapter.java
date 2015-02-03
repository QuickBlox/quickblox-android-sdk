package com.quickblox.sample.videochatwebrtcnew.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.quickblox.sample.videochatwebrtcnew.R;
import com.quickblox.sample.videochatwebrtcnew.User;
import com.quickblox.sample.videochatwebrtcnew.activities.ListUsersActivity;

import java.util.ArrayList;


/**
 * Created by tereha on 25.01.15.
 */

public class UsersAdapter extends BaseAdapter {
    private ArrayList<User> user;

    private LayoutInflater inflater;

    public UsersAdapter(Context context, ArrayList<User> results) {
        user = results;
        inflater = LayoutInflater.from(context);
    }

    public int getCount() {
        return user.size();
    }

    public User getItem(int position) {
        return user.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_user, null);
            holder = new ViewHolder();
            holder.numberOfList = (TextView) convertView.findViewById(R.id.numberOfList);
            holder.loginAs = (TextView) convertView.findViewById(R.id.loginAs);
            holder.userName = (TextView) convertView.findViewById(R.id.userName);

            holder.numberOfList.setBackgroundResource(ListUsersActivity.resourceSelector((position+1)));

            convertView.setTag(holder);



            /*if (position==1){
                holder.numberOfList.setBackgroundResource(R.drawable.shape_oval_blue);

            } else  if (position==1){
                holder.numberOfList.setBackgroundResource(R.drawable.shape_oval_coral);

            } else  if (position==2){
                holder.numberOfList.setBackgroundResource(R.drawable.shape_oval_blue_green);

            } else  if (position==3){
                holder.numberOfList.setBackgroundResource(R.drawable.shape_oval_gentianaceae_blue);

            } else if (position==4){
                holder.numberOfList.setBackgroundResource(R.drawable.shape_oval_lime);

            } else if (position==5){
                holder.numberOfList.setBackgroundResource(R.drawable.shape_oval_mauveine);

            } else if (position==6){
                holder.numberOfList.setBackgroundResource(R.drawable.shape_oval_may_green);

            } else if (position==7){
                holder.numberOfList.setBackgroundResource(R.drawable.shape_oval_orange);

            } else if (position==8){
                holder.numberOfList.setBackgroundResource(R.drawable.shape_oval_spring_bud);

            } else if (position==9){
                holder.numberOfList.setBackgroundResource(R.drawable.shape_oval_water_bondi_beach);

            } else if (position==10){
                holder.numberOfList.setBackgroundResource(R.drawable.shape_oval_lime);

            }*/


        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.numberOfList.setText(String.valueOf(position +1));
        holder.loginAs.setText(R.string.login_as);
        holder.userName.setText(user.get(position).getUserName());

        return convertView;
    }

    public static class ViewHolder {
        TextView numberOfList;
        TextView loginAs;
        TextView userName;
    }
}