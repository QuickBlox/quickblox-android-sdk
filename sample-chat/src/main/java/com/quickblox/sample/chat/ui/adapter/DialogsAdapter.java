package com.quickblox.sample.chat.ui.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.sample.chat.R;
import com.quickblox.sample.chat.utils.UiUtils;
import com.quickblox.sample.chat.utils.qb.QbDialogUtils;
import com.quickblox.sample.core.ui.adapter.BaseSelectableListAdapter;
import com.quickblox.sample.core.utils.ResourceUtils;

import java.util.List;

public class DialogsAdapter extends BaseSelectableListAdapter<QBDialog> {

    public DialogsAdapter(Context context, List<QBDialog> dialogs) {
        super(context, dialogs);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_dialog, parent, false);

            holder = new ViewHolder();
            holder.rootLayout = (ViewGroup) convertView.findViewById(R.id.root);
            holder.nameTextView = (TextView) convertView.findViewById(R.id.text_dialog_name);
            holder.lastMessageTextView = (TextView) convertView.findViewById(R.id.text_dialog_last_message);
            holder.dialogImageView = (ImageView) convertView.findViewById(R.id.image_dialog_icon);
            holder.unreadCounterTextView = (TextView) convertView.findViewById(R.id.text_dialog_unread_count);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        QBDialog dialog = getItem(position);
        if (dialog.getType().equals(QBDialogType.GROUP)) {
            holder.dialogImageView.setBackgroundDrawable(UiUtils.getGreyCircleDrawable());
            holder.dialogImageView.setImageResource(R.drawable.ic_chat_group);
        } else {
            holder.dialogImageView.setBackgroundDrawable(UiUtils.getColorCircleDrawable(position));
            holder.dialogImageView.setImageDrawable(null);
        }

        holder.nameTextView.setText(QbDialogUtils.getDialogName(dialog));
        if (isLastMessageAttachment(dialog)) {
            holder.lastMessageTextView.setText(R.string.chat_attachment);
        } else {
            holder.lastMessageTextView.setText(dialog.getLastMessage());
        }

        int unreadMessagesCount = dialog.getUnreadMessageCount();
        if (unreadMessagesCount == 0) {
            holder.unreadCounterTextView.setVisibility(View.GONE);
        } else {
            holder.unreadCounterTextView.setVisibility(View.VISIBLE);
            holder.unreadCounterTextView.setText(String.valueOf(unreadMessagesCount > 99 ? 99 : unreadMessagesCount));
        }

        holder.rootLayout.setBackgroundColor(isItemSelected(position) ? ResourceUtils.getColor(R.color.selected_list_item_color) :
                ResourceUtils.getColor(android.R.color.transparent));

        return convertView;
    }

    private boolean isLastMessageAttachment(QBDialog dialog) {
        String lastMessage = dialog.getLastMessage();
        Integer lastMessageSenderId = dialog.getLastMessageUserId();
        return TextUtils.isEmpty(lastMessage) && lastMessageSenderId != null;
    }

    private static class ViewHolder {
        ViewGroup rootLayout;
        ImageView dialogImageView;
        TextView nameTextView;
        TextView lastMessageTextView;
        TextView unreadCounterTextView;
    }
}
