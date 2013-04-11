package com.quickblox.chat_v2.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.quickblox.chat_v2.R;

/**
 * Created with IntelliJ IDEA.
 * User: nickolas
 * Date: 05.04.13
 * Time: 9:58
 */
public class DialogsFragment extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        View view = inflater.inflate(R.layout.dialogs_fragment, null);
        return view;
    }
}
