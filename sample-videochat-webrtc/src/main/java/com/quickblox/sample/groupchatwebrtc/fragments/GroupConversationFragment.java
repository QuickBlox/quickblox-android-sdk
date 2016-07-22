package com.quickblox.sample.groupchatwebrtc.fragments;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.quickblox.sample.groupchatwebrtc.R;
import com.quickblox.sample.groupchatwebrtc.adapters.OpponentsFromCallAdapter;

import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;


public class GroupConversationFragment extends ConversationFragment {

    private static final int DEFAULT_ROWS_COUNT = 2;
    private static final int DEFAULT_COLS_COUNT = 3;
    private static final String TAG = GroupConversationFragment.class.getSimpleName();

    private RecyclerView opponentListView;
    private SparseArray<OpponentsFromCallAdapter.ViewHolder> opponentViewHolders;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return onCreateView(inflater, container, savedInstanceState, R.layout.group_conversation_fragment);
    }

    protected SurfaceViewRenderer getVideoViewForOpponent(Integer userID) {
        OpponentsFromCallAdapter.ViewHolder viewHolderForOpponent = getViewHolderForOpponent(userID);
        if (viewHolderForOpponent != null) {
            return viewHolderForOpponent.getOpponentView();
        }
        return null;
    }

    @Override
    protected void initRemoteView() {

    }

    @Override
    protected void initCustomView(View view) {
        opponentViewHolders = new SparseArray<>(opponents.size());

        opponentListView = (RecyclerView) view.findViewById(R.id.grid_opponents);
        opponentListView.addItemDecoration(new DividerItemDecoration(getActivity(), R.dimen.grid_item_divider));
        opponentListView.setHasFixedSize(true);
        final int columnsCount = defineColumnsCount();
        final int rowsCount = defineRowCount();
        opponentListView.setLayoutManager(new GridLayoutManager(getActivity(), columnsCount));
        opponentListView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                setGrid(columnsCount, rowsCount);
                opponentListView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
    }

    @Override
    protected void onVideoScalingUpdated(RendererCommon.ScalingType scalingType) {
        super.onVideoScalingUpdated(scalingType);
        for (int i = 0; i < opponentViewHolders.size(); i++) {
            OpponentsFromCallAdapter.ViewHolder viewHolder = opponentViewHolders.valueAt(i);
            if (viewHolder != null) {
                updateVideoView(viewHolder.getOpponentView(), false, scalingType);
            }
        }

    }

    private void setGrid(int columnsCount, int rowsCount) {
        int gridWidth = opponentListView.getMeasuredWidth();
        float itemMargin = getResources().getDimension(R.dimen.grid_item_divider);
        int cellSize = defineMinSize(gridWidth, opponentListView.getMeasuredHeight(),
                columnsCount, rowsCount, itemMargin);
        Log.i(TAG, "onGlobalLayout : cellSize=" + cellSize);

        //TODO review holding current session in activity
        OpponentsFromCallAdapter opponentsAdapter = new OpponentsFromCallAdapter(getActivity(),
                sessionController.getCurrentSession(),
                opponents, cellSize,
                cellSize, gridWidth, columnsCount, (int) itemMargin,
                isVideoEnabled);
        opponentListView.setAdapter(opponentsAdapter);
    }

    private OpponentsFromCallAdapter.ViewHolder getViewHolderForOpponent(Integer userID) {
        OpponentsFromCallAdapter.ViewHolder holder = opponentViewHolders.get(userID);
        if (holder == null) {
            holder = findHolder(userID);
            if (holder != null) {
                opponentViewHolders.append(userID, holder);
            }
        }
        return holder;
    }

    private OpponentsFromCallAdapter.ViewHolder findHolder(Integer userID) {
        int childCount = opponentListView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = opponentListView.getChildAt(i);
            OpponentsFromCallAdapter.ViewHolder childViewHolder = (OpponentsFromCallAdapter.ViewHolder) opponentListView.getChildViewHolder(childView);
            Log.d(TAG, "getStatusViewForOpponent holder user id is : " + childViewHolder.getUserId());
            if (userID.equals(childViewHolder.getUserId())) {
                return childViewHolder;
            }
        }
        return null;
    }

    @Override
    protected TextView getStatusViewForOpponent(int userId) {
        OpponentsFromCallAdapter.ViewHolder itemHolder = getViewHolderForOpponent(userId);
        if (itemHolder == null) {
            return null;
        }
        return itemHolder.getConnectionStatus();
    }

    private int defineMinSize(int measuredWidth, int measuredHeight, int columnsCount, int rowsCount, float padding) {
        int cellWidth = measuredWidth / columnsCount - (int) (padding * 2);
        int cellHeight = measuredHeight / rowsCount - (int) (padding * 2);
        return Math.min(cellWidth, cellHeight);
    }

    private int defineRowCount() {
        int result = DEFAULT_ROWS_COUNT;
        int opponentsCount = opponents.size();
        if (opponentsCount < 3) {
            result = opponentsCount;
        }
        return result;

    }

    private int defineColumnsCount() {
        int result = DEFAULT_COLS_COUNT;
        int opponentsCount = opponents.size();
        if (opponentsCount == 1 || opponentsCount == 2) {
            result = 1;
        } else if (opponentsCount == 4) {
            result = 2;
        }
        return result;
    }

    class DividerItemDecoration extends RecyclerView.ItemDecoration {

        private int space;

        public DividerItemDecoration(@NonNull Context context, @DimenRes int dimensionDivider) {
            this.space = context.getResources().getDimensionPixelSize(dimensionDivider);
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.set(space, space, space, space);
        }

    }
}
