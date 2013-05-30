package com.quickblox.chat_v2.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.core.ChatApplication;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.module.content.QBContent;
import com.quickblox.module.content.result.QBFileResult;
import com.quickblox.module.users.model.QBUser;

import java.util.ArrayList;


public class NewDialogAdapter extends ArrayAdapter<QBUser> {

    public NewDialogAdapter(Context context, ArrayList<QBUser> userList) {
        super(context, 0, userList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = View.inflate(getContext(), R.layout.new_dialog_list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.userAvatar = (ImageView) convertView.findViewById(R.id.user_avatar);
            viewHolder.userName = (TextView) convertView.findViewById(R.id.user_name);
            viewHolder.container = (RelativeLayout) convertView.findViewById(R.id.container);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        QBUser qbUser = getItem(position);
        viewHolder.userName.setText(qbUser.getFullName());
        if (qbUser.getFileId() != null) {
            applyImage(qbUser.getFileId(), viewHolder.userAvatar);
        }
        return convertView;
    }

    private void applyImage(int fileId, final ImageView userAvatar) {
        QBContent.getFile(fileId, new QBCallbackImpl() {
            @Override
            public void onComplete(Result result) {
                if (result.isSuccess()) {
                    applyImage(userAvatar, ((QBFileResult) result).getFile().getPublicUrl());
                }
            }
        });
    }

    private void applyImage(ImageView userAvatar, String userAvatarUrl) {

        // Load and display image
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getContext())
                .defaultDisplayImageOptions(ChatApplication.getInstance().getOptions()).build();
        ImageLoader.getInstance().init(config);
        ImageLoader.getInstance().displayImage(userAvatarUrl, userAvatar);
    }


    private static class ViewHolder {
        ImageView userAvatar;
        TextView userName;
        RelativeLayout container;
    }

}
