package com.quickblox.sample.groupchatwebrtc.adapters;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.quickblox.sample.core.ui.adapter.BaseSelectableListAdapter;
import com.quickblox.sample.core.utils.ResourceUtils;
import com.quickblox.sample.core.utils.UiUtils;
import com.quickblox.sample.groupchatwebrtc.R;
import com.quickblox.users.model.QBUser;

import java.util.List;

/**
 * QuickBlox team
 */
public class OpponentsAdapter extends BaseSelectableListAdapter<QBUser> {

    public OpponentsAdapter(Context context, List<QBUser> users) {
        super(context, users);
    }

    @TargetApi(Build.VERSION_CODES.M)
    public View getView(final int position, View convertView, final ViewGroup parent) {

        final ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_opponents_list, null);
            holder = new ViewHolder();
            holder.opponentIcon = (ImageView) convertView.findViewById(R.id.image_opponent_icon);
            holder.opponentName = (TextView) convertView.findViewById(R.id.opponentsName);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final QBUser user = getItem(position);

        if (user != null) {
            holder.opponentName.setText(user.getFullName());

            if (selectedItems.contains(user)){
                convertView.setBackgroundColor(context.getColor(R.color.selected_user_item_background_color));
                holder.opponentIcon.setBackgroundDrawable(
                        UiUtils.getColoredCircleDrawable(ResourceUtils.getColor(R.color.selected_user_icon_background_color)));
                holder.opponentIcon.setImageDrawable(context.getDrawable(R.drawable.ic_checkmark));
            } else {
                convertView.setBackgroundColor(context.getColor(R.color.normal_user_item_background_color));
                holder.opponentIcon.setBackgroundDrawable(UiUtils.getColorCircleDrawable(position));
                holder.opponentIcon.setImageDrawable(context.getDrawable(R.drawable.ic_person));
            }
        }

        return convertView;
    }

    public static class ViewHolder {
        ImageView opponentIcon;
        TextView opponentName;
    }
}
