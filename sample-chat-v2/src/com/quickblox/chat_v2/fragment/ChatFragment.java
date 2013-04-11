package com.quickblox.chat_v2.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.danikula.aibolit.Aibolit;
import com.quickblox.chat_v2.R;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew Dmitrenko
 * Date: 4/11/13
 * Time: 12:53 PM
 */
public class ChatFragment extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        View view = inflater.inflate(R.layout.room_list_fragment, null);
        Aibolit.doInjections(this, view);
//        topBar.setFragmentName(TopBar.FRAGMENT_ROOMS);
//        applyRoomList();
        return view;
    }
}
