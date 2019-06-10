package com.quickblox.sample.videochat.conference.java.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.sample.videochat.conference.java.R;
import com.quickblox.sample.videochat.conference.java.utils.UiUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class DialogsAdapter extends BaseAdapter {

    private List<QBChatDialog> selectedItems;
    private LayoutInflater inflater;
    private Context context;
    private List<QBChatDialog> chatDialogList;

    public DialogsAdapter(Context context, List<QBChatDialog> dialogs) {
        this.context = context;
        chatDialogList = dialogs;
        inflater = LayoutInflater.from(context);
        selectedItems = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return chatDialogList != null ? chatDialogList.size() : 0;
    }

    @Override
    public QBChatDialog getItem(int position) {
        return chatDialogList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void add(QBChatDialog item) {
        chatDialogList.add(item);
        notifyDataSetChanged();
    }

    public List<QBChatDialog> getList() {
        return chatDialogList;
    }

    public void remove(QBChatDialog item) {
        chatDialogList.remove(item);
        notifyDataSetChanged();
    }

    public Collection<QBChatDialog> getSelectedItems() {
        return selectedItems;
    }

    public void updateList(List<QBChatDialog> newData) {
        chatDialogList = newData;
        notifyDataSetChanged();
    }

    public void clearSelection() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public void toggleSelection(int position) {
        QBChatDialog item = getItem(position);
        toggleSelection(item);
    }

    public void toggleSelection(QBChatDialog item) {
        if (selectedItems.contains(item)) {
            selectedItems.remove(item);
        } else {
            selectedItems.add(item);
        }
        notifyDataSetChanged();
    }

    public void selectItem(int position) {
        QBChatDialog item = getItem(position);
        selectItem(item);
    }

    public void selectItem(QBChatDialog item) {
        if (selectedItems.contains(item)) {
            return;
        }
        selectedItems.add(item);
        notifyDataSetChanged();
    }

    private boolean isItemSelected(int position) {
        return !selectedItems.isEmpty() && selectedItems.contains(getItem(position));
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_dialog, parent, false);

            holder = new ViewHolder();
            holder.rootLayout = convertView.findViewById(R.id.root);
            holder.nameTextView = convertView.findViewById(R.id.text_dialog_name);
            holder.dialogImageView = convertView.findViewById(R.id.image_dialog_icon);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        QBChatDialog dialog = getItem(position);
        holder.dialogImageView.setBackground(UiUtils.getGreyCircleDrawable());
        holder.nameTextView.setText(dialog.getName());
        holder.rootLayout.setBackgroundColor(isItemSelected(position) ?
                context.getResources().getColor(R.color.selected_list_item_color) :
                context.getResources().getColor(android.R.color.transparent));

        return convertView;
    }

    public void toggleOneItem(QBChatDialog item) {
        if (selectedItems.contains(item)) {
            selectedItems.remove(item);
        } else {
            selectedItems.clear();
            selectedItems.add(item);
        }
        notifyDataSetChanged();
    }

    private static class ViewHolder {
        ViewGroup rootLayout;
        ImageView dialogImageView;
        TextView nameTextView;
    }
}