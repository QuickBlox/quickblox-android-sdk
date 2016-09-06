package com.quickblox.sample.core.adapter;

import java.util.List;

import android.support.v7.widget.RecyclerView;

public interface QBBaseAdapter<T> {

    T getItem(int position);

    int getItemViewType(int position);

    void add(T item);

    List<T> getList();

    void addList(List<T> items);

    void showAttachment(QBMessagesAdapter.QBMessagesAdapterViewHolder holder, int position);

//    T getItem(RecyclerView.ViewHolder viewHolder);
}
