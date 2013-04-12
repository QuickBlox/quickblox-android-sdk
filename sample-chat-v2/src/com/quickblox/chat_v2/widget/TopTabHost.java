package com.quickblox.chat_v2.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import com.quickblox.chat_v2.R;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew Dmitrenko
 * Date: 4/12/13
 * Time: 10:32 AM
 */
public class TopTabHost extends RelativeLayout {


    public TopTabHost(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.top_tabhost, null);
        this.addView(view);

    }

//    private void updateTab(String tabTitle) {
//        if (tabTitle.equals(getString(R.string.TAB_DIALOGS_TITLE))) {
//            loadDialogScreen();
//        } else if (tabTitle.equals(getString(R.string.TAB_ROOMS_TITLE))) {
//            loadRoomListScreen();
//        } else if (tabTitle.equals(getString(R.string.TAB_CONTACTS_TITLE))) {
//            loadContactsScreen();
//        } else if (tabTitle.equals(getString(R.string.TAB_PROFILE_TITLE))) {
//            loadProfileScreen();
//        }
//    }
//
//    @Override
//    public void onTabChanged(String tabTitle) {
//        if (DataHolder.getInstance().getQbUser() != null) {
//            updateTab(tabTitle);
//        }
//    }
//
//    public static void loadDialogScreen() {
//        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
//        DialogsActivity dialogsFragment = new DialogsActivity();
//        fragmentTransaction.replace(R.id.main, dialogsFragment);
//        fragmentTransaction.commitAllowingStateLoss();
//    }
//
//    public static void loadRoomListScreen() {
//        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
//        RoomsActivity roomsFragment = new RoomsActivity();
//        fragmentTransaction.replace(R.id.main, roomsFragment);
//        fragmentTransaction.commitAllowingStateLoss();
//    }
//
//    public static void loadContactsScreen() {
//        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
//        ContactsFragment contactsFragment = new ContactsFragment();
//        fragmentTransaction.replace(R.id.main, contactsFragment);
//        fragmentTransaction.commitAllowingStateLoss();
//    }
//
//    public static void loadProfileScreen() {
//        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
//        ProfileActivity profileFragment = new ProfileActivity();
//        fragmentTransaction.replace(R.id.main, profileFragment);
//        fragmentTransaction.commitAllowingStateLoss();
//    }
//
//    public static void loadNewRoomScreen() {
//        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
//        CreateNewRoomFragment createNewRoomFragment = new CreateNewRoomFragment();
//        fragmentTransaction.replace(R.id.main, createNewRoomFragment);
//        fragmentTransaction.commitAllowingStateLoss();
//    }
}
