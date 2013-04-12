package com.quickblox.chat_v2.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.quickblox.chat_v2.R;
import com.quickblox.module.custom.model.QBCustomObject;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew Dmitrenko
 * Date: 4/11/13
 * Time: 2:27 PM
 */
public class DialogsAdapter extends BaseAdapter {

    private static final String ROOM_NAME = "name";
    private static final String LAST_MSG = "last_msg";
    private static final String RECEPIENT_AVATAR = "recepient_avatar";

    LayoutInflater layoutInflater;
    List<QBCustomObject> dialogList;
    Context context;


    public DialogsAdapter(Context context, List<QBCustomObject> dialogList) {
        layoutInflater = LayoutInflater.from(context);
        this.dialogList = dialogList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return dialogList.size();
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
            convertView = layoutInflater.inflate(R.layout.dialog_list_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.userAvatar = (ImageView) convertView.findViewById(R.id.user_avatar);
            viewHolder.dialogName = (TextView) convertView.findViewById(R.id.dialog_name);
            viewHolder.dialogLastMsg = (TextView) convertView.findViewById(R.id.dialog_last_msg);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.dialogName.setText(dialogList.get(position).getFields().get(ROOM_NAME).toString());
        Object lastMsg = dialogList.get(position).getFields().get(LAST_MSG);
        if (lastMsg != null) {
            viewHolder.dialogLastMsg.setText(lastMsg.toString());
        }
        Object userAvatarUrl = dialogList.get(position).getFields().get(RECEPIENT_AVATAR);
        if (userAvatarUrl != null) {
            applyAvatar(viewHolder.userAvatar, userAvatarUrl.toString());
        }
        return convertView;
    }

    private void applyAvatar(ImageView userAvatar, String userAvatarUrl) {
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
        ImageLoader.getInstance().displayImage(userAvatarUrl, userAvatar);
    }

    public static class ViewHolder {
        ImageView userAvatar;
        TextView dialogName;
        TextView dialogLastMsg;
    }
}
