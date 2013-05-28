package com.quickblox.chat_v2.adapters;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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
import com.quickblox.chat_v2.interfaces.OnDialogCreateComplete;
import com.quickblox.chat_v2.interfaces.OnUserProfileDownloaded;
import com.quickblox.chat_v2.ui.activities.ChatActivity;
import com.quickblox.chat_v2.ui.activities.NewDialogActivity;
import com.quickblox.chat_v2.utils.GlobalConsts;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.module.content.QBContent;
import com.quickblox.module.content.result.QBFileResult;
import com.quickblox.module.users.model.QBUser;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA. User: Andrew Dmitrenko Date: 4/11/13 Time: 5:31
 * PM
 */
public class NewDialogAdapter extends BaseAdapter implements OnDialogCreateComplete, OnUserProfileDownloaded {

    private LayoutInflater layoutInflater;
    private ProgressDialog progress;

    private Context context;
    private ChatApplication app;

    private ArrayList<QBUser> userList;
    private String createdDialogId;
    private int tUserId;


    public NewDialogAdapter(Context context, ArrayList<QBUser> userList) {
        this.context = context;
        this.userList = userList;
        layoutInflater = LayoutInflater.from(context);
        app = ChatApplication.getInstance();
        app.getMsgManager().setDialogCreateListener(this);
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
            app.getMsgManager().setDialogCreateListener(NewDialogAdapter.this);
            app.getMsgManager().createDialog(userList.get(position), true);
            blockUi(true);
        }
    };

    private void loadChatActivity(int userId, String dialogId) {

        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(GlobalConsts.USER_ID, userId);
        intent.putExtra(GlobalConsts.DIALOG_ID, dialogId);
        intent.putExtra(GlobalConsts.PREVIOUS_ACTIVITY, GlobalConsts.DIALOG_ACTIVITY);
        context.startActivity(intent);
        blockUi(false);
        ((NewDialogActivity) context).finish();
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
        DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory().cacheOnDisc().imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                .displayer(new RoundedBitmapDisplayer(20)).build();
        // Load and display image
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context).defaultDisplayImageOptions(options).build();
        ImageLoader.getInstance().init(config);
        ImageLoader.getInstance().displayImage(userAvatarUrl, userAvatar);
    }


    private static class ViewHolder {
        ImageView userAvatar;
        TextView userName;
        RelativeLayout container;
    }

    @Override
    public void dialogCreate(int userId, String customObjectUid) {
        createdDialogId = customObjectUid;
        tUserId = userId;

        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                app.getQbm().setUserProfileListener(NewDialogAdapter.this);
                app.getQbm().getSingleUserInfo(tUserId);
            }
        });

    }

    @Override
    public void downloadComlete(QBUser friend) {
        app.getDialogsUsers().put(String.valueOf(tUserId), friend);
        loadChatActivity(tUserId, createdDialogId);
    }


    public void blockUi(boolean enable) {
        if (progress == null) {
            return;
        }

        if (enable) {
            progress = ProgressDialog.show(context, context.getResources().getString(R.string.app_name), context.getResources().getString(R.string.new_dialog_activity_create_dialog), true);
        } else {
            progress.dismiss();
        }
    }
}
