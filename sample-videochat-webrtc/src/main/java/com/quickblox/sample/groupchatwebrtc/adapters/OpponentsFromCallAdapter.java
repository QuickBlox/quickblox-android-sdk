package com.quickblox.sample.groupchatwebrtc.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.quickblox.sample.groupchatwebrtc.R;
import com.quickblox.sample.groupchatwebrtc.utils.QBRTCSessionUtils;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBPeerChannel;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;

import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;

import java.util.List;

/**
 * QuickBlox team
 */
public class OpponentsFromCallAdapter extends RecyclerView.Adapter<OpponentsFromCallAdapter.ViewHolder> {

    private static final String TAG = OpponentsFromCallAdapter.class.getSimpleName();
    private final int itemHeight;
    private final int itemWidth;
    private int paddingLeft = 0;

    private Context context;
    private final QBRTCSession qbrtcSession;
    private List<QBUser> opponents;
    private int gridWidth;
    private boolean showVideoView;
    private LayoutInflater inflater;
    private int columns;

    public OpponentsFromCallAdapter(Context context, QBRTCSession qbrtcSession,
                                    List<QBUser> users, int width, int height,
                                    int gridWidth, int columns, int itemMargin,
                                    boolean showVideoView) {
        this.context = context;
        this.qbrtcSession = qbrtcSession;
        this.opponents = users;
        this.gridWidth = gridWidth;
        this.columns = columns;
        this.showVideoView = showVideoView;
        this.inflater = LayoutInflater.from(context);
        itemWidth = width;
        itemHeight = height;
        setPadding(itemMargin);
        Log.d(TAG, "item width=" + itemWidth + ", item height=" + itemHeight);
    }

    private void setPadding(int itemMargin) {
        int allCellWidth = (itemWidth + (itemMargin * 2)) * columns;
        if ((allCellWidth < gridWidth) && ((gridWidth - allCellWidth) > (itemMargin * 2))) { //set padding if it makes sense to do it
            paddingLeft = (gridWidth - allCellWidth) / 2;
        }
    }

    @Override
    public int getItemCount() {
        return opponents.size();
    }

    public Integer getItem(int position) {
        return opponents.get(position).getId();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.list_item_opponent_from_call, null);
        v.findViewById(R.id.innerLayout).setLayoutParams(new FrameLayout.LayoutParams(itemWidth, itemHeight));
        if (paddingLeft != 0) {
            v.setPadding(paddingLeft, v.getPaddingTop(), v.getPaddingRight(), v.getPaddingBottom());
        }
        SurfaceViewRenderer opponentView = (SurfaceViewRenderer) v.findViewById(R.id.opponentView);
        updateVideoView(opponentView, false);

        ViewHolder vh = new ViewHolder(v);
        vh.showOpponentView(showVideoView);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final QBUser user = opponents.get(position);
        holder.opponentsName.setText(user.getFullName());
        holder.setUserId(user.getId());
        QBPeerChannel peerChannel = qbrtcSession.getPeerChannel(user.getId());
        Integer statusRes = QBRTCSessionUtils.getStatusDescriptionReosuurce(
                QBRTCTypes.QBRTCConnectionState.QB_RTC_CONNECTION_CLOSED == peerChannel.getState() ?
                        peerChannel.getDisconnectReason() : peerChannel.getState());
        if (statusRes == null) {
            statusRes = R.string.unDefined;
        }
        holder.setStatus(context.getString(statusRes));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    protected void updateVideoView(SurfaceViewRenderer surfaceViewRenderer,  boolean mirror){
        surfaceViewRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        surfaceViewRenderer.setMirror(mirror);
        surfaceViewRenderer.requestLayout();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView opponentsName;
        TextView connectionStatus;
        SurfaceViewRenderer opponentView;
        private int userId;

        public ViewHolder(View itemView) {
            super(itemView);
            opponentsName = (TextView) itemView.findViewById(R.id.opponentName);
            connectionStatus = (TextView) itemView.findViewById(R.id.connectionStatus);
            opponentView = (SurfaceViewRenderer) itemView.findViewById(R.id.opponentView);
        }

        public void setStatus(String status) {
            connectionStatus.setText(status);
        }

        public TextView getConnectionStatus() {
            return connectionStatus;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        public int getUserId() {
            return userId;
        }

        public SurfaceViewRenderer getOpponentView() {
            return opponentView;
        }

        public void showOpponentView(boolean show) {
            opponentView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}
