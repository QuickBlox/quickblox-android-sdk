package com.quickblox.sample.groupchatwebrtc.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
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
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.QBSettings;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.sample.groupchatwebrtc.R;
import com.quickblox.sample.groupchatwebrtc.adapters.UsersAdapter;
import com.quickblox.sample.groupchatwebrtc.definitions.Consts;
import com.quickblox.sample.groupchatwebrtc.holder.DataHolder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.fabric.sdk.android.Fabric;

/**
 * QuickBlox team
 */
public class LoginActivity extends BaseActivity {

    private static final String TAG = "LoginActivity";

    private static final long ON_ITEM_CLICK_DELAY = TimeUnit.SECONDS.toMillis(10);

    private UsersAdapter usersListAdapter;
    private ListView usersList;
    private ProgressBar progressBar;
    private static ArrayList<QBUser> users = new ArrayList<>();
    private volatile boolean resultReceived = true;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_login);

        initUI();

        // Set application credentials
        //
        QBSettings.getInstance().init(getApplicationContext(), Consts.APP_ID, Consts.AUTH_KEY, Consts.AUTH_SECRET);
        QBSettings.getInstance().setAccountKey(Consts.ACCOUNT_KEY);

        if (getActionBar() != null) {
            getActionBar().setTitle(getResources().getString(R.string.opponentsListActionBarTitle));
        }

        QBChatService.setDebugEnabled(true);
        chatService = QBChatService.getInstance();

        createAppSession();
    }

    private void createAppSession() {
        showProgress(true);
        QBAuth.createSession(new QBEntityCallback<QBSession>() {
            @Override
            public void onSuccess(QBSession qbSession, Bundle bundle) {
                showProgress(false);
                loadUsers();
            }

            @Override
            public void onError(List<String> list) {
                Toast.makeText(LoginActivity.this, "Error while loading users", Toast.LENGTH_SHORT).show();
                showProgress(false);
            }
        });
    }

    private void initUI() {
        usersList = (ListView) findViewById(R.id.usersListView);
        progressBar = (ProgressBar) findViewById(R.id.loginPB);
        progressBar.setVisibility(View.INVISIBLE);

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

        for (QBUser usr : users) {
            if (usr.getId().equals(id)) {
                index = (users.indexOf(usr)) + 1;
                break;
            }
        }
        return index;
    }

    private void initUsersList() {
        usersListAdapter = new UsersAdapter(this, users);
        usersList.setAdapter(usersListAdapter);
        usersList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        usersList.setOnItemClickListener(clicklistener);
    }

    private void loadUsers(){
        loadUsers(getString(R.string.users_tag));
    }

    private void loadUsers(String tag){
        showProgress(true);

        QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder();
        requestBuilder.setPerPage(getResources().getInteger(R.integer.users_count));
        List<String> tags = new LinkedList<>();
        tags.add(tag);
        QBUsers.getUsersByTags(tags, requestBuilder, new QBEntityCallbackImpl<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                showProgress(false);

                users.clear();
                users.addAll(DataHolder.createUsersList(qbUsers));
                initUsersList();
            }

            @Override
            public void onError(QBResponseException exc) {
                showProgress(false);

                Toast.makeText(LoginActivity.this, "Error while loading users", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onError()");
            }
        });
    }

    private void showProgress(boolean show){
        progressBar.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    private long upTime = 0l;

    AdapterView.OnItemClickListener clicklistener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            if(!LoginActivity.this.isConnectivityExists){
                showToast(R.string.internet_not_connected);
            }else{
                if (!resultReceived || (SystemClock.uptimeMillis() - upTime) < ON_ITEM_CLICK_DELAY){
                    return;
                }
                resultReceived = false;
                upTime = SystemClock.uptimeMillis();
                QBUser user = usersListAdapter.getItem(position);

                startIncomeCallListenerService(user.getLogin(), user.getPassword(), Consts.LOGIN);            }
        }
    };
}
