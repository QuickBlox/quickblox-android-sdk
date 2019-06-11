package com.quickblox.sample.conference.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.sample.conference.R;
import com.quickblox.sample.core.ui.adapter.BaseSelectableListAdapter;
import com.quickblox.sample.core.utils.ResourceUtils;
import com.quickblox.sample.core.utils.UiUtils;

import java.util.List;


public class DialogsAdapter extends BaseSelectableListAdapter<QBChatDialog> {

    public DialogsAdapter(Context context, List<QBChatDialog> dialogs) {
        super(context, dialogs);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_dialog, parent, false);

            holder = new ViewHolder();
            holder.rootLayout = (ViewGroup) convertView.findViewById(R.id.root);
            holder.nameTextView = (TextView) convertView.findViewById(R.id.text_dialog_name);
            holder.dialogImageView = (ImageView) convertView.findViewById(R.id.image_dialog_icon);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        QBChatDialog dialog = getItem(position);
        holder.dialogImageView.setBackgroundDrawable(UiUtils.getGreyCircleDrawable());

        holder.nameTextView.setText(dialog.getName());

        holder.rootLayout.setBackgroundColor(isItemSelected(position) ? ResourceUtils.getColor(R.color.selected_list_item_color) :
                ResourceUtils.getColor(android.R.color.transparent));


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