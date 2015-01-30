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
import com.quickblox.sample.videochatwebrtcnew.R;
import com.quickblox.sample.videochatwebrtcnew.User;
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
    private Context context;
    private QBChatService chatService;


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

    public ArrayList<User> createUsersCollection() {
        ArrayList <User> users = new ArrayList<>();
        users.add(new User("User 1", "user_1", "11111111"));
        users.add(new User("User 2", "user_2", "11111111"));
        users.add(new User("User 3", "user_3", "11111111"));
        users.add(new User("User 4", "user_4", "11111111"));
        users.add(new User("User 5", "user_5", "11111111"));
        users.add(new User("User 6", "user_6", "11111111"));
        users.add(new User("User 7", "user_7", "11111111"));
        users.add(new User("User 8", "user_8", "11111111"));
        users.add(new User("User 9", "user_9", "11111111"));
        users.add(new User("User 10", "user_10", "11111111"));
        return users;
    }

    private void initUsersList() {

        final ArrayList<User> usersCollection = createUsersCollection();

        usersListAdapter = new UsersAdapter(this, usersCollection);
        usersList.setAdapter(usersListAdapter);
        usersList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        usersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String login = usersListAdapter.getItem(position).getLogin();
                String password = usersListAdapter.getItem(position).getPassword();

                createSession(login, password);
           }
        });
    }

    /*private Intent prepareDataToNewActivity(int index, String login) {
        intent = new Intent(ListUsersActivity.this, OpponentsActivity.class);
        intent.putExtra("index", index);
        intent.putExtra("login", login);
        return intent;
    }*/



    /*private void createSession(final User logginedUser) {

        context = ListUsersActivity.this;

        if (!QBChatService.isInitialized()) {
            QBChatService.init(context);
            chatService = QBChatService.getInstance();
        }

        final QBUser user = new QBUser(logginedUser.getLogin(), logginedUser.getPassword());
        QBAuth.createSession(logginedUser.getLogin(), logginedUser.getPassword(), new QBEntityCallbackImpl<QBSession>() {
            @Override
            public void onSuccess(QBSession session, Bundle bundle) {

                user.setId(session.getUserId());

                Log.d("Track", "Level 1");

                //Intent intent = new Intent(ListUsersActivity.this, OpponentsActivity.class);
                //intent.putExtra("logginedUser", logginedUser);
                //intent.putExtra("loginedUserLogin", login);
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
    }*/

    private void createSession(final String login, final String password) {

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

                Intent intent = new Intent(ListUsersActivity.this, OpponentsActivity.class);
                intent.putExtra("login", login);
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
