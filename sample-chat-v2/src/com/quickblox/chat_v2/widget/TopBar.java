package com.quickblox.chat_v2.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.core.ChatApplication;
import com.quickblox.chat_v2.ui.activities.UserProfileActivity;
import com.quickblox.chat_v2.utils.GlobalConsts;
import com.quickblox.module.users.model.QBUser;

import java.util.Random;

public class TopBar extends RelativeLayout {

    public static final String CHAT_ACTIVITY = "Dialog";
    public static final String NEW_DIALOG_ACTIVITY = "New Dialog";
    public static final String PROFILE_ACTIVITY = "Profile";
    public static final String ROOM_ACTIVITY = "Group chat";

    private TextView screenTitle;
    private ImageView userAvatar;
    private String[] data;
    private QBUser friend;

    public TopBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.top_bar, this);
        setBackgroundResource(android.R.color.darker_gray);
        initViews();
    }

    private void initViews() {

        screenTitle = (TextView) findViewById(R.id.screen_title);
        userAvatar = (ImageView) findViewById(R.id.user_avatar_iv);
        userAvatar.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                showDialog(data);
            }

        });

    }

    private void showDialog(String[] data) {
        AlertDialog.Builder adb = new AlertDialog.Builder(getContext());
        adb.setTitle(R.string.chat_dialog_name);

        adb.setItems(data, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                switch (which) {
                    case 0:
                        Intent i = new Intent(getContext(), UserProfileActivity.class);
                        i.putExtra(GlobalConsts.FRIEND_ID, String.valueOf(friend.getId()));
                        getContext().startActivity(i);
                        break;

                    case 1:
                        ChatApplication.getInstance().getRstManager().sendRequestToSubscribe(friend.getId());
                        break;
                }
            }
        });

        adb.create().show();
    }

    public void setFragmentParams(String fragmentName, int isUserPicVisible) {
        if (fragmentName != null) {
            screenTitle.setText(fragmentName);
        }
        userAvatar.setVisibility(isUserPicVisible);
        userAvatar.setClickable(false);
    }

    public void setFriendParams(QBUser friend, boolean isContacts) {
        this.friend = friend;

        if (isContacts) {
            data = new String[]{getContext().getResources().getString(R.string.chat_dialog_view_profile)};
        } else {
            data = new String[]{getContext().getResources().getString(R.string.chat_dialog_view_profile), getContext().getResources().getString(R.string.chat_dialog_add_contact)};
        }
    }


}