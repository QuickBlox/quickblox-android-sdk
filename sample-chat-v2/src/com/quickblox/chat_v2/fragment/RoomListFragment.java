package com.quickblox.chat_v2.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.activitys.MainActivity;
import com.quickblox.chat_v2.adapters.RoomListAdapter;
import com.quickblox.module.chat.QBChat;
import com.quickblox.module.chat.model.QBChatRoom;

import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew Dmitrenko
 * Date: 11.04.13
 * Time: 9:58
 */
public class RoomListFragment extends Fragment {

    ListView roomListView;
    RoomListAdapter roomListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        View view = inflater.inflate(R.layout.room_list_fragment, null);
        roomListView = (ListView) view.findViewById(R.id.room_list);
        applyRoomList();
        return view;
    }

    private void applyRoomList() {
        Collection<QBChatRoom> roomList = QBChat.requestAllRooms();
        if (roomList != null) {
            roomListAdapter = new RoomListAdapter(MainActivity.getContext(), roomList);
            roomListView.setAdapter(roomListAdapter);
        }
    }


}
