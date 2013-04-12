package com.quickblox.chat_v2.ui.activities;

import android.app.Activity;
import android.widget.ListView;
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
public class RoomsActivity extends Activity {

    ListView roomListView;
    RoomListAdapter roomListAdapter;

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
//        View view = inflater.inflate(R.layout.room_list_fragment, null);
//        roomListView = (ListView) view.findViewById(R.id.room_list);
//        applyRoomList();
//        return view;
//    }

    private void applyRoomList() {
        Collection<QBChatRoom> roomList = QBChat.requestAllRooms();
        if (roomList != null) {
            roomListAdapter = new RoomListAdapter(MainActivity.getContext(), roomList);
            roomListView.setAdapter(roomListAdapter);
        }
    }


}
