package com.quickblox.sample.videochatwebrtcnew.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.chat.QBChatService;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.QBSettings;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.sample.videochatwebrtcnew.R;
import com.quickblox.sample.videochatwebrtcnew.Users;
import com.quickblox.sample.videochatwebrtcnew.adapters.UsersAdapter;
import com.quickblox.sample.videochatwebrtcnew.definitions.Consts;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tereha on 25.01.15.
 */
public class ListUsersActivity extends Activity {

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


        QBSettings.getInstance().fastConfigInit(Consts.APP_ID, Consts.AUTH_KEY, Consts.AUTH_SECRET);
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

    public ArrayList<Users> createCollection() {
        ArrayList <Users> users = new ArrayList<>();
        users.add(new Users("User 1", "user_1", "11111111"));
        users.add(new Users("User 2", "user_2", "11111111"));
        users.add(new Users("User 3", "user_3", "11111111"));
        users.add(new Users("User 4", "user_4", "11111111"));
        users.add(new Users("User 5", "user_5", "11111111"));
        users.add(new Users("User 6", "user_6", "11111111"));
        users.add(new Users("User 7", "user_7", "11111111"));
        users.add(new Users("User 8", "user_8", "11111111"));
        users.add(new Users("User 9", "user_9", "11111111"));
        users.add(new Users("User 10", "user_10", "11111111"));
        return users;
    }

    private void initUsersList() {

        usersListAdapter = new UsersAdapter(this, createArrayUsers());
        usersList.setAdapter(usersListAdapter);
        usersList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        final ArrayList<Users> usersCollection = createCollection();


        usersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {



                String userName = usersListAdapter.getItem(position);
                String login;
                String password;


                for(Users usr : usersCollection){
                     if (userName.equals(usr.getUserName())){
                         login = usr.getLogin();
                         password = usr.getPassword();

                         createSession(login, password);
                         Log.d("Track", "Session created");

                    }

                }
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

                Log.d("Track", "Level 1");

                Intent intent = new Intent(ListUsersActivity.this, InterlocutorsActivity.class);
                startActivity(intent);


                chatService.login(user, new QBEntityCallbackImpl() {
                    @Override
                    public void onSuccess() {
                        Log.d("Track", "Level 2");

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
