package com.quickblox.sample.chat.qblist;

import android.support.v7.widget.RecyclerView;

import java.util.List;

public interface QBBaseAdapter<T> {
    T getItem(int position);

    int getItemViewType(int position);

    void add(T item);

    List<T> getList();

    void addList(List<T> items);

    T getItem(RecyclerView.ViewHolder viewHolder);
}
