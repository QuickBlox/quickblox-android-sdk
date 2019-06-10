package com.quickblox.sample.videochat.java.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.quickblox.sample.videochat.java.R;
import com.quickblox.sample.videochat.java.utils.UiUtils;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

/**
 * QuickBlox team
 */
public class OpponentsAdapter extends BaseAdapter {

    private List<QBUser> selectedItems;

    private LayoutInflater inflater;
    private Context context;
    private List<QBUser> userList;

    private SelectedItemsCountsChangedListener selectedItemsCountChangedListener;

    public OpponentsAdapter(Context context, List<QBUser> users) {
        selectedItems = new ArrayList<>();
        inflater = LayoutInflater.from(context);
        this.context = context;
        userList = users;
    }

    @Override
    public int getCount() {
        return userList != null ? userList.size() : 0;
    }

    @Override
    public QBUser getItem(int position) {
        return userList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void add(QBUser item) {
        userList.add(item);
        notifyDataSetChanged();
    }

    public List<QBUser> getList() {
        return userList;
    }

    public List<QBUser> getSelectedItems() {
        return selectedItems;
    }

    public void remove(QBUser item) {
        userList.remove(item);
        notifyDataSetChanged();
    }

    public void updateUsersList(List<QBUser> userList) {
        this.userList = userList;
        notifyDataSetChanged();
    }

    private void toggleSelection(int position) {
        QBUser item = getItem(position);
        if (selectedItems.contains(item)) {
            selectedItems.remove(item);
        } else {
            selectedItems.add(item);
        }
        notifyDataSetChanged();
    }

    public View getView(final int position, View convertView, final ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_opponents_list, null);
            holder = new ViewHolder();
            holder.opponentIcon = convertView.findViewById(R.id.image_opponent_icon);
            holder.opponentName = convertView.findViewById(R.id.opponentsName);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final QBUser user = getItem(position);

        if (user != null) {
            holder.opponentName.setText(user.getFullName());

            if (selectedItems.contains(user)) {
                convertView.setBackgroundResource(R.color.background_color_selected_user_item);
                holder.opponentIcon.setBackgroundDrawable(
                        UiUtils.getColoredCircleDrawable(context.getResources().getColor(R.color.icon_background_color_selected_user)));
                holder.opponentIcon.setImageResource(R.drawable.ic_checkmark);
            } else {
                convertView.setBackgroundResource(R.color.background_color_normal_user_item);
                holder.opponentIcon.setBackgroundDrawable(UiUtils.getColorCircleDrawable(user.getId()));
                holder.opponentIcon.setImageResource(R.drawable.ic_person);
            }
        }

        convertView.setOnClickListener(v -> {
            toggleSelection(position);
            selectedItemsCountChangedListener.onCountSelectedItemsChanged(selectedItems.size());
        });

        return convertView;
    }

    public static class ViewHolder {
        ImageView opponentIcon;
        TextView opponentName;
    }

    public void setSelectedItemsCountsChangedListener(SelectedItemsCountsChangedListener selectedItemsCountsChanged) {
        if (selectedItemsCountsChanged != null) {
            this.selectedItemsCountChangedListener = selectedItemsCountsChanged;
        }
    }

    public interface SelectedItemsCountsChangedListener {
        void onCountSelectedItemsChanged(int count);
    }
}