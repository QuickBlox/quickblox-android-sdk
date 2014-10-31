package com.quickblox.sample.chat.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.quickblox.users.model.QBUser;
import com.quickblox.sample.chat.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by igorkhomenko on 9/12/14.
 */
public class UsersAdapter extends BaseAdapter {

    private List<QBUser> dataSource;
    private LayoutInflater inflater;
    private List<QBUser> selected = new ArrayList<QBUser>();

    public UsersAdapter(List<QBUser> dataSource, Context ctx) {
        this.dataSource = dataSource;
        this.inflater = LayoutInflater.from(ctx);
    }

    public List<QBUser> getSelected() {
        return selected;
    }

    @Override
    public int getCount() {
        return dataSource.size();
    }

    @Override
    public Object getItem(int position) {
        return dataSource.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_user, null);
            holder = new ViewHolder();
            holder.login = (TextView) convertView.findViewById(R.id.userLogin);
            holder.add = (CheckBox) convertView.findViewById(R.id.addCheckBox);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final QBUser user = dataSource.get(position);
        if (user != null) {
            holder.login.setText(user.getLogin());
            holder.add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ((((CheckBox) v).isChecked())) {
                        selected.add(user);
                    } else {
                        selected.remove(user);
                    }
                }
            });
            holder.add.setChecked(selected.contains(user));
        }
        return convertView;
    }

    private static class ViewHolder {
        TextView login;
        CheckBox add;
    }
}
