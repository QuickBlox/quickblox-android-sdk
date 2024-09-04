package com.quickblox.sample.videochat.java.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.quickblox.sample.videochat.java.R;
import com.quickblox.users.model.QBUser;

import java.util.List;

public class AudioCallAdapter extends RecyclerView.Adapter<AudioCallAdapter.ViewHolder> {
    private List<ReconnectingUserModel> usersList;
    private final LayoutInflater inflater;

    public AudioCallAdapter(Context context, List<ReconnectingUserModel> usersList) {
        this.usersList = usersList;
        this.inflater = LayoutInflater.from(context);
    }

    public void updateList(List<ReconnectingUserModel> usersList) {
        this.usersList = usersList;
        notifyDataSetChanged();
    }

    public ReconnectingUserModel getItemByUserId(int userId) {
        for (ReconnectingUserModel item : usersList) {
            if (item.getUser().getId().equals(userId)) {
                return item;
            }
        }
        return null;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.audio_call_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(AudioCallAdapter.ViewHolder holder, int position) {
        QBUser user = usersList.get(position).getUser();
        String name;
        if (TextUtils.isEmpty(user.getFullName())) {
            name = user.getLogin();
        } else {
            name = user.getFullName();
        }
        holder.nameView.setText(name);

        if(!TextUtils.isEmpty(usersList.get(position).getReconnectingState())){
            holder.statusView.setText(usersList.get(position).getReconnectingState());
        }
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameView;
        private final TextView statusView;

        public ViewHolder(View itemView) {
            super(itemView);
            nameView = (TextView) itemView.findViewById(R.id.name);
            statusView = (TextView) itemView.findViewById(R.id.status);
        }

        public void setStatus(String status) {
            statusView.setText(status);
        }

        public void setName(String name) {
            nameView.setText(name);
        }
    }
}