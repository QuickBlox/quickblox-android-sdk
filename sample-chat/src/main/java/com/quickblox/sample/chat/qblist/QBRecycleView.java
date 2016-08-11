package com.quickblox.sample.chat.qblist;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public abstract class QBRecycleView<T> extends RecyclerView{

    public QBRecycleView(Context context) {
        super(context);

    }

    @Override
    public void addItemDecoration(ItemDecoration decor, int index) {

    }
}
