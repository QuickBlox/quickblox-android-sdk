package com.quickblox.sample.core.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseListAdapter<T> extends BaseAdapter {

    protected LayoutInflater inflater;
    protected Context context;
    protected List<T> objectsList;

    public BaseListAdapter(Context context) {
        this(context, new ArrayList<T>());
    }

    public BaseListAdapter(Context context, List<T> objectsList) {
        this.context = context;
        this.objectsList = objectsList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return objectsList.size();
    }

    @Override
    public T getItem(int position) {
        return objectsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void updateList(List<T> newData) {
        objectsList = newData;
        notifyDataSetChanged();
    }

    public void add(T item) {
        objectsList.add(item);
        notifyDataSetChanged();
    }

    public void addList(List<T> items) {
        objectsList.addAll(0, items);
        notifyDataSetChanged();
    }

    public List<T> getList() {
        return objectsList;
    }

    public void remove(T item) {
        objectsList.remove(item);
        notifyDataSetChanged();
    }
}