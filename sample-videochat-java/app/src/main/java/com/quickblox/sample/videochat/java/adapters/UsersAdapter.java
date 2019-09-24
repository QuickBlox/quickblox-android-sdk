package com.quickblox.sample.videochat.java.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.quickblox.sample.videochat.java.R;
import com.quickblox.sample.videochat.java.utils.UiUtils;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {

    private Context context;
    List<QBUser> usersList;
    List<QBUser> selectedUsers;
    private SelectedItemsCountChangedListener selectedItemsCountChangedListener;


    public UsersAdapter(Context context, List<QBUser> usersList) {
        this.context = context;
        this.usersList = usersList;
        this.selectedUsers = new ArrayList<>();
    }


    public List<QBUser> getSelectedUsers() {
        return selectedUsers;
    }

    public void setSelectedItemsCountsChangedListener(SelectedItemsCountChangedListener selectedItemsCountChanged) {
        if (selectedItemsCountChanged != null) {
            this.selectedItemsCountChangedListener = selectedItemsCountChanged;
        }
    }


    @NonNull
    @Override
    public UsersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_opponents_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull UsersAdapter.ViewHolder holder, int position) {
        QBUser user = usersList.get(position);
        holder.opponentName.setText(user.getFullName());
        if (selectedUsers.contains(user)) {
            holder.rootLayout.setBackgroundResource(R.color.background_color_selected_user_item);
            holder.opponentIcon.setBackgroundDrawable(
                    UiUtils.getColoredCircleDrawable(context.getResources().getColor(R.color.icon_background_color_selected_user)));
            holder.opponentIcon.setImageResource(R.drawable.ic_checkmark);
        } else {
            holder.rootLayout.setBackgroundResource(R.color.background_color_normal_user_item);
            holder.opponentIcon.setBackgroundDrawable(UiUtils.getColorCircleDrawable(user.getId()));
            holder.opponentIcon.setImageResource(R.drawable.ic_person);
        }
        holder.rootLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSelection(user);
                selectedItemsCountChangedListener.onCountSelectedItemsChanged(selectedUsers.size());
            }
        });
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    public void updateUsersList(List<QBUser> usersList) {
        this.usersList = usersList;
        notifyDataSetChanged();
    }

    private void toggleSelection(QBUser qbUser) {
        if (selectedUsers.contains(qbUser)){
            selectedUsers.remove(qbUser);
        } else {
            selectedUsers.add(qbUser);
        }
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView opponentIcon;
        TextView opponentName;
        LinearLayout rootLayout;

        public ViewHolder(@NonNull View view) {
            super(view);
            opponentIcon = view.findViewById(R.id.image_opponent_icon);
            opponentName = view.findViewById(R.id.opponents_name);
            rootLayout = view.findViewById(R.id.root_layout);
        }
    }

    public interface SelectedItemsCountChangedListener {
        void onCountSelectedItemsChanged(Integer count);
    }
}
