package com.quickblox.chat_v2.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.danikula.aibolit.Aibolit;
import com.danikula.aibolit.annotation.InjectView;
import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.widget.TopBar;

/**
 * Created with IntelliJ IDEA.
 * User: nickolas
 * Date: 05.04.13
 * Time: 9:58
 */
public class RoomsFragment extends Fragment {

    private static final String FRAGMENT_NAME = "Rooms";
    @InjectView(R.id.top_bar)
    TopBar topBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        View view = inflater.inflate(R.layout.fragment_rooms, null);
        Aibolit.doInjections(this, view);
        topBar.setFragmentName(FRAGMENT_NAME);
        return view;
    }

}
