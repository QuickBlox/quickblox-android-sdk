package com.quickblox.sample.videochatwebrtcnew.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
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
import com.quickblox.sample.videochatwebrtcnew.helper.DataHolder;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tereha on 25.01.15.
 */
public class ListUsersActivity extends Activity {

    private UsersAdapter usersListAdapter;
    private ListView usersList;
    private ProgressBar loginPB;
    private Context context;
    private QBChatService chatService;
    private ArrayList<User> users;


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
        loginPB = (ProgressBar) findViewById(R.id.loginPB);
        loginPB.setVisibility(View.INVISIBLE);

    }

    public static int resourceSelector (int number){
        int resStr=-1;
        switch (number) {
            case 0:
                resStr = R.drawable.shape_oval_spring_bud;
                break;
            case 1:
                resStr = R.drawable.shape_oval_orange;
                break;
            case 2:
                resStr = R.drawable.shape_oval_water_bondi_beach;
                break;
            case 3:
                resStr = R.drawable.shape_oval_blue_green;
                break;
            case 4:
                resStr = R.drawable.shape_oval_lime;
                break;
            case 5:
                resStr = R.drawable.shape_oval_mauveine;
                break;
            case 6:
                resStr = R.drawable.shape_oval_gentianaceae_blue;
                break;
            case 7:
                resStr = R.drawable.shape_oval_blue;
                break;
            case 8:
                resStr = R.drawable.shape_oval_blue_krayola;
                break;
            case 9:
                resStr = R.drawable.shape_oval_coral;
                break;
            default:
                resStr= resourceSelector(number%10);
        }
        return  resStr;
    }

    private void initUsersList() {

        users = DataHolder.createUsersList();

        usersListAdapter = new UsersAdapter(this, users);
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

    private void createSession(final String login, final String password) {

        loginPB.setVisibility(View.VISIBLE);

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

                loginPB.setVisibility(View.INVISIBLE);

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
