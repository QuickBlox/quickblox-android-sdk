package com.quickblox.chat_v2.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.quickblox.chat_v2.R;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew Dmitrenko
 * Date: 4/8/13
 * Time: 3:38 PM
 */
public class TopBar extends RelativeLayout {


    public static final String CHAT_ACTIVITY = "Chat";
    public static final String NEW_DIALOG_ACTIVITY = "New Dialog";

    private TextView screenTitle;
    private ImageView userAvatar;

    private String fragmentName;

    public TopBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.top_bar, null);
        this.addView(view);

        initViews(view);
    }

    private void initViews(View view) {
        screenTitle = (TextView) view.findViewById(R.id.screen_title);
        userAvatar = (ImageView) view.findViewById(R.id.user_avatar_iv);
    }

    public void setFragmentName(String fragmentName) {
        this.fragmentName = fragmentName;
        screenTitle.setText(fragmentName);
        initExtraViews();
    }

    private void initExtraViews() {
        if (fragmentName.equals(CHAT_ACTIVITY)) {
            // TODO load image
        }
    }

}
