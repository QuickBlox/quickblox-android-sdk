package com.quickblox.sample.user.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.sample.user.R;
import com.quickblox.sample.user.helper.DataHolder;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: android
 * Date: 20.11.12
 * Time: 14:06
 */
public class UserListAdapter extends BaseAdapter {

    LayoutInflater inflater;

    public UserListAdapter(Context ctx) {
        inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return DataHolder.getDataHolder().getQBUserListSize();
    }

    @Override
    public QBUser getItem(int index) {
        return DataHolder.getDataHolder().getQBUser(index);
    }

    @Override
    public long getItemId(int index) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.user_list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.userName = (TextView) convertView.findViewById(R.id.user_name);
            viewHolder.tags = (TextView) convertView.findViewById(R.id.tags);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        applyUserName(viewHolder, DataHolder.getDataHolder().getQBUserName(position));
        applyTags(viewHolder, DataHolder.getDataHolder().getQbUserTags(position));
        return convertView;
    }


    private void applyUserName(ViewHolder viewHolder, String userName) {
        viewHolder.userName.setText(userName);
    }

    private void applyTags(ViewHolder viewHolder, List<String> tags) {
        if (tags != null) {
            viewHolder.tags.setText(tags.toString());
        }
    }

    public static class ViewHolder {
        TextView userName;
        TextView tags;
    }
}
