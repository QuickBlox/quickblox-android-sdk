package com.quickblox.chat_v2.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.utils.GlobalConsts;
import com.quickblox.module.custom.model.QBCustomObject;

import java.util.HashMap;
import java.util.List;

public class DialogsAdapter extends ArrayAdapter<QBCustomObject> {

    public DialogsAdapter(Context context, List<QBCustomObject> dialogList) {
        super(context, 0, dialogList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = View.inflate(getContext(), R.layout.dialog_list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.userAvatar = (ImageView) convertView.findViewById(R.id.user_avatar);
            viewHolder.dialogName = (TextView) convertView.findViewById(R.id.dialog_name);
            viewHolder.dialogLastMsg = (TextView) convertView.findViewById(R.id.dialog_last_msg);
            viewHolder.container = (RelativeLayout) convertView.findViewById(R.id.container);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        HashMap<String, Object> fields = getItem(position).getFields();

        viewHolder.dialogName.setText(fields.get(GlobalConsts.ROOM_NAME).toString());

        viewHolder.container.setTag(position);

        Object msgObject = fields.get(GlobalConsts.LAST_MSG);

        if (msgObject != null) {

            String lastMsg = msgObject.toString();

            if (lastMsg.length() > 13 && lastMsg.substring(0, 13).equals(GlobalConsts.ATTACH_INDICATOR)) {
                lastMsg = GlobalConsts.ATTACH_TEXT_FOR_DIALOGS;
            }

            viewHolder.dialogLastMsg.setText(lastMsg);
        }
        return convertView;
    }

    private static class ViewHolder {
        ImageView userAvatar;
        TextView dialogName;
        TextView dialogLastMsg;
        RelativeLayout container;
    }
}
