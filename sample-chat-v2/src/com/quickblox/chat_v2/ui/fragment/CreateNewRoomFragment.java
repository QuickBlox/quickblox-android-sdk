package com.quickblox.chat_v2.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.core.DataHolder;
import com.quickblox.chat_v2.widget.TopBar;
import com.quickblox.module.chat.QBChat;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew Dmitrenko
 * Date: 4/11/13
 * Time: 9:05 AM
 */
public class CreateNewRoomFragment extends Fragment {

    TopBar topBar;
    EditText roomName;
    CheckBox persistentCheckBox;
    CheckBox onlyMembersCheckBox;
    Button joinButton;

    private boolean persistent;
    private boolean onlyMembers;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        View view = inflater.inflate(R.layout.create_new_room_fragment, null);

        topBar = (TopBar) view.findViewById(R.id.top_bar);
        roomName = (EditText) view.findViewById(R.id.room_editText);
        persistentCheckBox = (CheckBox) view.findViewById(R.id.persistent_checkbox);
        persistentCheckBox.setOnCheckedChangeListener(persistentCheckedListener);
        onlyMembersCheckBox = (CheckBox) view.findViewById(R.id.only_members_checkbox);
        onlyMembersCheckBox.setOnCheckedChangeListener(onlyMembersCheckedListener);
        joinButton = (Button) view.findViewById(R.id.join_button);
        joinButton.setOnClickListener(joinClickListener);
        topBar.setFragmentName(TopBar.FRAGMENT_NEW_ROOM);

        return view;
    }

    CompoundButton.OnCheckedChangeListener persistentCheckedListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                persistent = true;
            }
        }
    };

    CompoundButton.OnCheckedChangeListener onlyMembersCheckedListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                onlyMembers = true;
            }
        }
    };

    View.OnClickListener joinClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            QBChat.createRoom(roomName.getText().toString(), DataHolder.getInstance().getQbUser(), onlyMembers, persistent);
        }
    };
}
