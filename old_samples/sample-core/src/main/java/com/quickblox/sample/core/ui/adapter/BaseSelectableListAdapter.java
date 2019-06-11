package com.quickblox.sample.core.ui.adapter;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class BaseSelectableListAdapter<T> extends BaseListAdapter<T> {

    protected List<T> selectedItems;

    public BaseSelectableListAdapter(Context context, List<T> objectsList) {
        super(context, objectsList);
        selectedItems = new ArrayList<>();
    }

    public void toggleSelection(int position) {
        T item = getItem(position);
        toggleSelection(item);
    }

    public void toggleSelection(T item) {
        if (selectedItems.contains(item)) {
            selectedItems.remove(item);
        } else {
            selectedItems.add(item);
        }
        notifyDataSetChanged();
    }

    public void selectItem(int position) {
        T item = getItem(position);
        selectItem(item);
    }

    public void selectItem(T item) {
        if (selectedItems.contains(item)) {
            return;
        }
        selectedItems.add(item);
        notifyDataSetChanged();
    }

    public Collection<T> getSelectedItems() {
        return selectedItems;
    }

    protected boolean isItemSelected(int position) {
        return !selectedItems.isEmpty() && selectedItems.contains(getItem(position));
    }

    public void clearSelection() {
        selectedItems.clear();
        notifyDataSetChanged();
    }
}