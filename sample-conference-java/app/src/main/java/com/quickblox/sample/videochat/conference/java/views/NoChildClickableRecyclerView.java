package com.quickblox.sample.videochat.conference.java.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class NoChildClickableRecyclerView extends RecyclerView {

    private OnNoChildClickListener listener;

    public NoChildClickableRecyclerView(@NonNull Context context) {
        super(context);
    }

    public NoChildClickableRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public NoChildClickableRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setOnNoChildClickListener(OnNoChildClickListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && findChildViewUnder(event.getX(), event.getY()) == null && listener != null) {
            listener.onNoChildClick();
        }
        return super.dispatchTouchEvent(event);
    }

    public interface OnNoChildClickListener {

        void onNoChildClick();
    }
}