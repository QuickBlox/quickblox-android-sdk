package com.quickblox.chat_v2.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.ui.activities.ChatActivity;
import com.quickblox.chat_v2.utils.GlobalConsts;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.module.content.QBContent;
import com.quickblox.module.content.result.QBFileResult;
import com.quickblox.module.custom.QBCustomObjects;
import com.quickblox.module.custom.model.QBCustomObject;
import com.quickblox.module.custom.result.QBCustomObjectResult;
import com.quickblox.module.users.model.QBUser;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew Dmitrenko
 * Date: 4/11/13
 * Time: 5:31 PM
 */
public class NewDialogAdapter extends BaseAdapter {

    private static final String DIALOGS_CLASS = "dialogs";
    private static final String NAME_FIELD = "name";
    private static final String RECEPIENT_ID_FIELD = "recepient_id";

    private LayoutInflater layoutInflater;
    private Context context;
    private ArrayList<QBUser> userList;

    public NewDialogAdapter(Context context, ArrayList<QBUser> userList) {
        this.context = context;
        this.userList = userList;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return userList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.new_dialog_list_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.userAvatar = (ImageView) convertView.findViewById(R.id.user_avatar);
            viewHolder.userName = (TextView) convertView.findViewById(R.id.user_name);
            viewHolder.container = (RelativeLayout) convertView.findViewById(R.id.container);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.userName.setText(userList.get(position).getFullName());

        viewHolder.container.setTag(position);
        viewHolder.container.setOnClickListener(userClickListener);

        if (userList.get(position).getFileId() != null) {
            applyImage(userList.get(position).getFileId(), viewHolder.userAvatar);
        }
        return convertView;
    }


    View.OnClickListener userClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (Integer) v.getTag();
            createDialog(position);
        }
    };

    private void createDialog(final int position) {
        QBCustomObject co = new QBCustomObject();
        HashMap<String, Object> fields = new HashMap<String, Object>();
        fields.put(RECEPIENT_ID_FIELD, userList.get(position).getId());
        fields.put(NAME_FIELD, userList.get(position).getFullName());
        co.setFields(fields);
        co.setClassName(DIALOGS_CLASS);
        QBCustomObjects.createObject(co, new QBCallbackImpl() {
            @Override
            public void onComplete(Result result) {
                if (result.isSuccess()) {
                    loadChatActivity(userList.get(position).getId(), ((QBCustomObjectResult) result).getCustomObject().getCustomObjectId());
                }
            }
        });
    }

    private void loadChatActivity(int userId, String dialogId) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(GlobalConsts.USER_ID, userId);
        intent.putExtra(GlobalConsts.DIALOG_ID, dialogId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
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
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory()
                .cacheOnDisc()
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                .displayer(new RoundedBitmapDisplayer(20))
                .build();
        // Load and display image
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .defaultDisplayImageOptions(options)
                .build();
        ImageLoader.getInstance().init(config);
        ;
        ImageLoader.getInstance().displayImage(userAvatarUrl, userAvatar);
    }

    private static class ViewHolder {
        ImageView userAvatar;
        TextView userName;
        RelativeLayout container;
    }
}
