package com.quickblox.sample.videochat.java.adapters;

import android.content.Context;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.quickblox.sample.videochat.java.R;
import com.quickblox.sample.videochat.java.fragments.BaseConversationFragment;
import com.quickblox.sample.videochat.java.utils.QBRTCSessionUtils;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.quickblox.videochat.webrtc.view.QBRTCSurfaceView;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

/**
 * QuickBlox team
 */
public class OpponentsFromCallAdapter extends RecyclerView.Adapter<OpponentsFromCallAdapter.ViewHolder> {

    private static final String TAG = OpponentsFromCallAdapter.class.getSimpleName();
    private final int itemHeight;
    private final int itemWidth;

    private Context context;
    private BaseConversationFragment baseConversationFragment;
    private List<QBUser> opponents;
    private LayoutInflater inflater;
    private OnAdapterEventListener adapterListener;

    public OpponentsFromCallAdapter(Context context, BaseConversationFragment baseConversationFragment, List<QBUser> users, int width, int height) {
        this.context = context;
        this.baseConversationFragment = baseConversationFragment;
        this.opponents = users;
        this.inflater = LayoutInflater.from(context);
        itemWidth = width;
        itemHeight = height;
        Log.d(TAG, "item width=" + itemWidth + ", item height=" + itemHeight);
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
        notifyItemRangeChanged(index, opponents.size());
    }

    public void replaceUsers(int position, QBUser qbUser) {
        opponents.set(position, qbUser);
        notifyItemChanged(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.list_item_opponent_from_call, null);
        v.findViewById(R.id.innerLayout).setLayoutParams(new FrameLayout.LayoutParams(itemWidth, itemHeight));

        ViewHolder vh = new ViewHolder(v);
        vh.setListener(new ViewHolder.ViewHolderClickListener() {
            @Override
            public void onShowOpponent(int callerId) {
                adapterListener.onItemClick(callerId);
            }
        });
        vh.showOpponentView(true);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final QBUser user = opponents.get(position);
        int userID = user.getId();
        holder.opponentsName.setText(user.getFullName());

        holder.getOpponentView().setId(user.getId());
        holder.setUserId(userID);
        QBRTCTypes.QBRTCConnectionState state = baseConversationFragment.getConnectionState(userID);
        if (state != null) {
            Log.d(TAG, "State ordinal= " + state.ordinal());
            holder.setStatus(context.getString(QBRTCSessionUtils.getStatusDescriptionResource(state)));
        }
        if (position == (opponents.size() - 1)) {
            adapterListener.OnBindLastViewHolder(holder, position);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements OnClickListener {
        TextView opponentsName;
        TextView connectionStatus;
        QBRTCSurfaceView opponentView;
        ProgressBar progressBar;
        private int userId;
        private ViewHolderClickListener viewHolderClickListener;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            opponentsName = (TextView) itemView.findViewById(R.id.opponentName);
            connectionStatus = (TextView) itemView.findViewById(R.id.connectionStatus);
            opponentView = (QBRTCSurfaceView) itemView.findViewById(R.id.opponentView);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progress_bar_adapter);
        }

        private void setListener(ViewHolderClickListener viewHolderClickListener) {
            this.viewHolderClickListener = viewHolderClickListener;
        }

        public void setStatus(String status) {
            connectionStatus.setText(status);
        }

        public void setUserName(String userName) {
            opponentsName.setText(userName);
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

        public QBRTCSurfaceView getOpponentView() {
            return opponentView;
        }

        public void showOpponentView(boolean show) {
            Log.d("UsersAdapter", "show? " + show);
            opponentView.setVisibility(show ? View.VISIBLE : View.GONE);
        }

        @Override
        public void onClick(View v) {
            viewHolderClickListener.onShowOpponent(getAdapterPosition());
        }

        public interface ViewHolderClickListener {
            void onShowOpponent(int callerId);
        }
    }

    public interface OnAdapterEventListener {
        void OnBindLastViewHolder(ViewHolder holder, int position);

        void onItemClick(int position);
    }
}