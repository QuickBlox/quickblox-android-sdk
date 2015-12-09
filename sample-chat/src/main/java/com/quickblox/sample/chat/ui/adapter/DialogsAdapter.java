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
import com.quickblox.sample.chat.utils.ChatUtils;
import com.quickblox.sample.chat.utils.UiUtils;
import com.quickblox.sample.chat.utils.chat.ChatHelper;
import com.quickblox.users.model.QBUser;

import java.util.List;

import vc908.stickerfactory.StickersManager;

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
            holder.lastMessageTextView = (TextView) convertView.findViewById(R.id.text_room_last_message);
            holder.dialogImageView = (ImageView) convertView.findViewById(R.id.image_dialog_icon);
            holder.unreadCounterTextView = (TextView) convertView.findViewById(R.id.text_unread_message_count);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        QBDialog dialog = dialogs.get(position);
        if (dialog.getType().equals(QBDialogType.GROUP)) {
            holder.nameTextView.setText(dialog.getName());
            holder.dialogImageView.setBackgroundDrawable(UiUtils.getGreyCircleDrawable());
            holder.dialogImageView.setImageResource(R.drawable.ic_chat_group);
        } else {
            // It's a private dialog, let's use opponent's name as chat name
            Integer opponentId = ChatUtils.getOpponentIdForPrivateDialog(dialog);
            QBUser user = ChatHelper.getInstance().getQbUserById(opponentId);
            if (user != null) {
                holder.nameTextView.setText(user.getLogin() == null ? user.getFullName() : user.getLogin());
            }
            holder.dialogImageView.setBackgroundDrawable(UiUtils.getRandomColorCircleDrawable());
            holder.dialogImageView.setImageDrawable(null);
        }

        String lastMessage = dialog.getLastMessage();
        if (!TextUtils.isEmpty(lastMessage) && StickersManager.isSticker(lastMessage)) {
            holder.lastMessageTextView.setText(R.string.chat_sticker);
        } else {
            holder.lastMessageTextView.setText(lastMessage);
        }

        holder.unreadCounterTextView.setText(String.valueOf(dialog.getUnreadMessageCount()));

        return convertView;
    }

    private static class ViewHolder {
        ImageView dialogImageView;
        TextView nameTextView;
        TextView lastMessageTextView;
        TextView unreadCounterTextView;
    }
}
