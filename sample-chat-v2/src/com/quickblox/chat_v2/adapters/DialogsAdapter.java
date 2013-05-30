package com.quickblox.chat_v2.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.core.ChatApplication;
import com.quickblox.chat_v2.utils.GlobalConsts;
import com.quickblox.module.custom.model.QBCustomObject;

import java.util.ArrayList;

public class DialogsAdapter extends ArrayAdapter<QBCustomObject> {


    public DialogsAdapter(Context context, ArrayList<QBCustomObject> dialogList) {
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

        viewHolder.dialogName.setText(getItem(position).getFields().get(GlobalConsts.ROOM_NAME).toString());

       // viewHolder.container.setTag(position);
//        viewHolder.container.setOnClickListener(dialogClickListener);
//
//        Object lastMsg = dialogList.get(position).getFields().get(GlobalConsts.LAST_MSG);
//        if (lastMsg != null) {
//            viewHolder.dialogLastMsg.setText(lastMsg.toString());
//        }
//        Object userAvatarUrl = dialogList.get(position).getFields().get(GlobalConsts.RECEPIENT_AVATAR);
//        if (userAvatarUrl != null) {
//            applyAvatar(viewHolder.userAvatar, userAvatarUrl.toString());
//        }
        return convertView;
    }

    private void applyAvatar(ImageView userAvatar, String userAvatarUrl) {

        // Load and display image
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getContext())
                .defaultDisplayImageOptions(ChatApplication.getInstance().getOptions()).build();
        ImageLoader.getInstance().init(config);
        ImageLoader.getInstance().displayImage(userAvatarUrl, userAvatar);
    }

    public static class ViewHolder {
        ImageView userAvatar;
        TextView dialogName;
        TextView dialogLastMsg;
        RelativeLayout container;
    }
}
