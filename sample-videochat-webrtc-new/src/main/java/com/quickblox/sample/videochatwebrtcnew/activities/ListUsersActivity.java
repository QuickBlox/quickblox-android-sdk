package com.quickblox.sample.videochatwebrtcnew.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.chat.QBChatService;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.QBSettings;
import com.quickblox.sample.videochatwebrtcnew.R;
import com.quickblox.sample.videochatwebrtcnew.VideoChatApplication;
import com.quickblox.sample.videochatwebrtcnew.adapters.UsersAdapter;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.core.QBVideoChatController;

import org.jivesoftware.smack.XMPPException;

import java.util.List;

/**
 * Created by tereha on 25.01.15.
 */
public class ListUsersActivity extends Activity /*implements View.OnClickListener*/ {

    private UsersAdapter usersListAdapter;
    private ListView usersList;
    Context context;
    QBChatService chatService;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initUI();
        initUsersList();

        QBSettings.getInstance().fastConfigInit("18846", "64JzC2cuLkSMUq7", "s4VCJZq4uWNer7H");
    }

    private void initUI() {
        usersList = (ListView) findViewById(R.id.usersListView);

    }

    private String [] createArrayUsers(){
        String [] users = new String[10];
        users[0] = "User 1";
        users[1] = "User 2";
        users[2] = "User 3";
        users[3] = "User 4";
        users[4] = "User 5";
        users[5] = "User 6";
        users[6] = "User 7";
        users[7] = "User 8";
        users[8] = "User 9";
        users[9] = "User 10";

        return users;
    }

    private void initUsersList() {

        usersListAdapter = new UsersAdapter(this, createArrayUsers());
        usersList.setAdapter(usersListAdapter);
        usersList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);


        usersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                createSession("user_1", "11111111");
           }
        });
    }

    private void createSession(String login, final String password) {

        context = ListUsersActivity.this;

        if (!QBChatService.isInitialized()) {
            QBChatService.init(context);
            chatService = QBChatService.getInstance();
        }

        final QBUser user = new QBUser(login, password);
        QBAuth.createSession(login, password, new QBEntityCallbackImpl<QBSession>() {
            @Override
            public void onSuccess(QBSession session, Bundle bundle) {

                user.setId(session.getUserId());


                chatService.login(user, new QBEntityCallbackImpl() {
                    @Override
                    public void onSuccess() {

                        Intent intent = new Intent(ListUsersActivity.this, InterlocutorsActivity.class);
                        startActivity(intent);
                    }

                    @Override
                    public void onError(List errors) {
                        Toast.makeText(ListUsersActivity.this, "Error when login", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onError(List<String> errors) {
                Toast.makeText(ListUsersActivity.this, "Error when login, check test users login and password", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
