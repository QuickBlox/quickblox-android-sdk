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

import com.crashlytics.android.Crashlytics;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.chat.QBChatService;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.QBSettings;
import com.quickblox.sample.videochatwebrtcnew.R;
import com.quickblox.sample.videochatwebrtcnew.User;
import com.quickblox.sample.videochatwebrtcnew.adapters.UsersAdapter;
import com.quickblox.sample.videochatwebrtcnew.definitions.Consts;
import com.quickblox.sample.videochatwebrtcnew.holder.DataHolder;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

import io.fabric.sdk.android.Fabric;


/**
 * Created by tereha on 25.01.15.
 */
public class ListUsersActivity extends Activity {

    private static final String TAG = "ListUsersActivity";
    private UsersAdapter usersListAdapter;
    private ListView usersList;
    private ProgressBar loginPB;
    private Context context;
    private static QBChatService chatService;
    private static ArrayList<User> users = DataHolder.createUsersList();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_login);

        initUI();

        QBSettings.getInstance().fastConfigInit(Consts.APP_ID, Consts.AUTH_KEY, Consts.AUTH_SECRET);

        if (getActionBar() != null) {
            getActionBar().setTitle(getResources().getString(R.string.opponentsListActionBarTitle));
        }

        if (!QBChatService.isInitialized()) {
            QBChatService.init(this);
            chatService = QBChatService.getInstance();
        }
        initUsersList();

    }

    private void initUI() {
        usersList = (ListView) findViewById(R.id.usersListView);
        loginPB = (ProgressBar) findViewById(R.id.loginPB);
        loginPB.setVisibility(View.INVISIBLE);

    }

    public static int resourceSelector(int number) {
        int resStr = -1;
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
                resStr = resourceSelector(number % 10);
        }
        return resStr;
    }

    public static int selectBackgrounForOpponent(int number) {
        int resStr = -1;
        switch (number) {
            case 0:
                resStr = R.drawable.rectangle_rounded_spring_bud;
                break;
            case 1:
                resStr = R.drawable.rectangle_rounded_orange;
                break;
            case 2:
                resStr = R.drawable.rectangle_rounded_water_bondi_beach;
                break;
            case 3:
                resStr = R.drawable.rectangle_rounded_blue_green;
                break;
            case 4:
                resStr = R.drawable.rectangle_rounded_lime;
                break;
            case 5:
                resStr = R.drawable.rectangle_rounded_mauveine;
                break;
            case 6:
                resStr = R.drawable.rectangle_rounded_gentianaceae_blue;
                break;
            case 7:
                resStr = R.drawable.rectangle_rounded_blue;
                break;
            case 8:
                resStr = R.drawable.rectangle_rounded_blue_krayola;
                break;
            case 9:
                resStr = R.drawable.rectangle_rounded_coral;
                break;
            default:
                resStr = selectBackgrounForOpponent(number % 10);
        }
        return resStr;
    }

    public static int getUserIndex(int id) {
        int index = 0;

        for (User usr : users) {
            if (usr.getId().equals(id)) {
                index = (users.indexOf(usr)) + 1;
                break;
            }
        }
        return index;
    }

    private void initUsersList() {

//        users = DataHolder.createUsersList();

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

        final QBUser user = new QBUser(login, password);
        QBAuth.createSession(login, password, new QBEntityCallbackImpl<QBSession>() {
            @Override
            public void onSuccess(QBSession session, Bundle bundle) {
                Log.d(TAG, "onSuccess create session with params");
                user.setId(session.getUserId());

                loginPB.setVisibility(View.INVISIBLE);

                chatService.login(user, new QBEntityCallbackImpl<QBUser>() {

                    @Override
                    public void onSuccess(QBUser result, Bundle params) {
                        Log.d(TAG, "onSuccess login to chat with params");
                        Intent intent = new Intent(ListUsersActivity.this, CallActivity.class);
                        intent.putExtra("login", login);

                    }

                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "onSuccess login to chat");
                        Intent intent = new Intent(ListUsersActivity.this, CallActivity.class);
                        intent.putExtra("login", login);
                        startActivity(intent);
                    }

                    @Override
                    public void onError(List errors) {
                        loginPB.setVisibility(View.INVISIBLE);
                        Toast.makeText(ListUsersActivity.this, "Error when login", Toast.LENGTH_SHORT).show();
                        for (Object error : errors) {
                            Log.d(TAG, error.toString());
                        }
                    }
                });

            }

            @Override
            public void onSuccess() {
                super.onSuccess();
                Log.d(TAG, "onSuccess create session");
            }

            @Override
            public void onError(List<String> errors) {
                loginPB.setVisibility(View.INVISIBLE);
                Toast.makeText(ListUsersActivity.this, "Error when login, check test users login and password", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}
