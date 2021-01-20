package com.quickblox.sample.videochat.conference.java.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.quickblox.conference.ConferenceSession;
import com.quickblox.conference.QBConferencePeerConnection;
import com.quickblox.conference.view.QBConferenceSurfaceView;
import com.quickblox.sample.videochat.conference.java.App;
import com.quickblox.sample.videochat.conference.java.R;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCTypes;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class OpponentsFromCallAdapter extends RecyclerView.Adapter<OpponentsFromCallAdapter.ViewHolder> {

    private static final String TAG = OpponentsFromCallAdapter.class.getSimpleName();

    private int layoutHeight;
    private Context context;
    private ConferenceSession session;
    private List<QBUser> opponents;
    private LayoutInflater inflater;
    private OnAdapterEventListener adapterListener;

    public OpponentsFromCallAdapter(Context context, ConferenceSession session, List<QBUser> users, int layoutHeight) {
        this.context = context;
        this.session = session;
        this.opponents = users;
        this.inflater = LayoutInflater.from(context);
        this.layoutHeight = layoutHeight;
    }

    public void clearOpponents(List<QBUser> opponents) {
        opponents.clear();
    }

    public void setAdapterListener(OnAdapterEventListener adapterListener) {
        this.adapterListener = adapterListener;
    }

    @Override
    public int getItemCount() {
        return opponents.size();
    }

    public Integer getItem(int position) {
        return opponents.get(position).getId();
    }

    public List<QBUser> getOpponents() {
        return opponents;
    }

    public void removeItem(int index) {
        opponents.remove(index);
        notifyItemRemoved(index);
        notifyItemRangeChanged(0, opponents.size());
    }

    public void updateUserFullName(QBUser user) {
        int position = opponents.indexOf(user);
        QBUser userInList = opponents.get(position);
        userInList.setFullName(user.getFullName());
        notifyItemChanged(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.list_item_conference_opponent, null);
        v.findViewById(R.id.innerLayout).setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, getItemHeight()));

        final ViewHolder vh = new ViewHolder(v);
        vh.toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                int position = vh.getAdapterPosition();
                Integer userID = getItem(position);
                adapterListener.onToggleButtonItemClick(userID, isChecked);
            }
        });

        vh.flItemContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = vh.getAdapterPosition();
                Integer userID = getItem(position);
                adapterListener.onOpponentViewItemClick(userID);
            }
        });
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final QBUser user = opponents.get(position);
        Integer userID = user.getId();
        String username = TextUtils.isEmpty(user.getFullName()) ? user.getLogin() : user.getFullName();
        QBUser currentUser = ((App) context.getApplicationContext()).getSharedPrefsHelper().getQbUser();
        if (userID.equals(currentUser.getId())) {
            holder.opponentName.setText(R.string.you);
        } else {
            holder.opponentName.setText(username);
        }

        boolean isAudioEnabled = true;
        if (session.getMediaStreamManager() != null) {
            try {
                isAudioEnabled = session.getMediaStreamManager().getAudioTrack(userID).enabled();
            } catch (Exception e) {
                if (e.getMessage() != null) {
                    Log.d(TAG, e.getMessage());
                }
            }
            holder.toggleButton.setChecked(isAudioEnabled);
        }

        holder.innerLayout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, getItemHeight()));
        holder.getSurfaceView().setId(user.getId());
        holder.setUserId(userID);

        if (isAudioEnabled) {
            holder.ivMutedOpponentIndicator.setVisibility(View.GONE);
        } else {
            holder.ivMutedOpponentIndicator.setVisibility(View.VISIBLE);
        }

        QBConferencePeerConnection peerConnection = session.getPeerConnection(userID);
        if (peerConnection != null) {
            QBRTCTypes.QBRTCConnectionState state = peerConnection.getState();
            Log.d(TAG, "state ordinal= " + state.ordinal());
            //holder.setStatus(context.getResources().getString(QBRTCSessionUtils.getStatusDescriptionResource(state)));
        }
        if (position == (opponents.size() - 1)) {
            adapterListener.OnBindLastViewHolder(holder, position);
        }
    }

    private int getItemHeight() {
        int itemsCount = opponents.size();
        int itemHeight = layoutHeight;

        if (itemsCount >= 2 && itemsCount <= 6) {
            itemHeight = layoutHeight / 2;
        }
        if (itemsCount >= 7 && itemsCount <= 9) {
            itemHeight = layoutHeight / 3;
        }
        if (itemsCount >= 10 && itemsCount <= 12) {
            itemHeight = layoutHeight / 4;
        }

        return itemHeight;
    }

    public void add(QBUser item) {
        opponents.add(item);
        notifyItemRangeChanged(0, opponents.size());
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public interface OnAdapterEventListener {

        void OnBindLastViewHolder(ViewHolder holder, int position);

        void onToggleButtonItemClick(Integer userID, boolean isChecked);

        void onOpponentViewItemClick(Integer userID);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        FrameLayout flItemContainer;
        RelativeLayout innerLayout;
        ToggleButton toggleButton;
        TextView opponentName;
        QBConferenceSurfaceView opponentView;
        ProgressBar progressBar;
        ImageView ivMutedOpponentIndicator;
        private int userId;

        ViewHolder(View itemView) {
            super(itemView);
            flItemContainer = itemView.findViewById(R.id.fl_item_container);
            toggleButton = itemView.findViewById(R.id.opponent_toggle_mic);
            opponentName = itemView.findViewById(R.id.opponentName);
            opponentView = itemView.findViewById(R.id.opponentView);
            progressBar = itemView.findViewById(R.id.progress_bar_adapter);
            innerLayout = itemView.findViewById(R.id.innerLayout);
            ivMutedOpponentIndicator = itemView.findViewById(R.id.iv_muted_indicator);
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        public int getUserId() {
            return userId;
        }

        public ProgressBar getProgressBar() {
            return progressBar;
        }

        public QBConferenceSurfaceView getSurfaceView() {
            return opponentView;
        }
    }
}