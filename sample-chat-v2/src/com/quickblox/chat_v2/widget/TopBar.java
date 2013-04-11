package com.quickblox.chat_v2.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.danikula.aibolit.Aibolit;
import com.danikula.aibolit.annotation.InjectOnClickListener;
import com.danikula.aibolit.annotation.InjectView;
import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.activitys.MainActivity;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew Dmitrenko
 * Date: 4/8/13
 * Time: 3:38 PM
 */
public class TopBar extends RelativeLayout {

    public static final String FRAGMENT_NEW_ROOM = "New Room";
    public static final String FRAGMENT_DIALOGS = "Dialogs";
    public static final String FRAGMENT_ROOMS = "Rooms";


    @InjectView(R.id.right_button)
    private Button rightButton;
    @InjectView(R.id.left_button)
    private Button leftButton;
    @InjectView(R.id.screen_title)
    private TextView screenTitle;

    private String fragmentName;

    public TopBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.top_bar, null);
        this.addView(view);

        Aibolit.doInjections(this);
    }

    public void setFragmentName(String fragmentName) {
        this.fragmentName = fragmentName;
        screenTitle.setText(fragmentName);
        applyButtons();
    }

    private void applyButtons() {
        if (fragmentName.equals(FRAGMENT_DIALOGS)) {
            leftButton.setVisibility(View.GONE);
            rightButton.setText(MainActivity.getContext().getString(R.string.NEW));
        } else if (fragmentName.equals(FRAGMENT_ROOMS)) {
            leftButton.setVisibility(View.GONE);
            rightButton.setText(MainActivity.getContext().getString(R.string.NEW));
        } else if (fragmentName.equals(FRAGMENT_NEW_ROOM)) {
            leftButton.setText(MainActivity.getContext().getString(R.string.back));
            rightButton.setVisibility(View.INVISIBLE);
        }
    }

    @InjectOnClickListener(R.id.left_button)
    public void onLeftButtonClick(View view) {
        if (fragmentName.equals(FRAGMENT_NEW_ROOM)) {
            MainActivity.loadRoomListScreen();
            MainActivity.showTabs();
        }
    }

    @InjectOnClickListener(R.id.right_button)
    public void onRightButtonClick(View view) {
        if (fragmentName.equals(FRAGMENT_DIALOGS)) {


        } else if (fragmentName.equals(FRAGMENT_ROOMS)) {
            MainActivity.loadNewRoomScreen();
            MainActivity.hideTabs();
        }

    }
}
