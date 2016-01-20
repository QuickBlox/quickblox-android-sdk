package com.quickblox.sample.chat.ui.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.sample.chat.R;
import com.quickblox.sample.chat.utils.UiUtils;
import com.quickblox.sample.chat.utils.qb.QbDialogUtils;

import java.util.List;

public class DialogsAdapter extends BaseAdapter {
    private List<QBDialog> dialogs;
    private LayoutInflater inflater;

    public DialogsAdapter(Context context, List<QBDialog> dialogs) {
        this.dialogs = dialogs;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return dialogs.get(position);
    }

    @Override
    public int getCount() {
        return dialogs.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_dialog, parent, false);

            holder = new ViewHolder();
            holder.nameTextView = (TextView) convertView.findViewById(R.id.text_dialog_name);
            holder.lastMessageTextView = (TextView) convertView.findViewById(R.id.text_dialog_last_message);
            holder.dialogImageView = (ImageView) convertView.findViewById(R.id.image_dialog_icon);
            holder.unreadCounterTextView = (TextView) convertView.findViewById(R.id.text_dialog_unread_count);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        QBDialog dialog = dialogs.get(position);
        if (dialog.getType().equals(QBDialogType.GROUP)) {
            holder.dialogImageView.setBackgroundDrawable(UiUtils.getGreyCircleDrawable());
            holder.dialogImageView.setImageResource(R.drawable.ic_chat_group);
        } else {
            holder.dialogImageView.setBackgroundDrawable(UiUtils.getRandomColorCircleDrawable());
            holder.dialogImageView.setImageDrawable(null);
        }

        holder.nameTextView.setText(QbDialogUtils.getDialogName(dialog));
        if (isLastMessageAttachment(dialog)) {
            holder.lastMessageTextView.setText(R.string.chat_attachment);
        } else {
            holder.lastMessageTextView.setText(dialog.getLastMessage());
        }
        holder.unreadCounterTextView.setText(String.valueOf(dialog.getUnreadMessageCount()));

        return convertView;
    }

    private boolean isLastMessageAttachment(QBDialog dialog) {
        String lastMessage = dialog.getLastMessage();
        Integer lastMessageSenderId = dialog.getLastMessageUserId();
        return TextUtils.isEmpty(lastMessage) && lastMessageSenderId != null;
    }

    private static class ViewHolder {
        ImageView dialogImageView;
        TextView nameTextView;
        TextView lastMessageTextView;
        TextView unreadCounterTextView;
    }
}
