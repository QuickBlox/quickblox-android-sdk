package com.quickblox.sample.chat.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.widget.ListView;
import android.widget.TextView;

import com.quickblox.sample.chat.R;

public class LoginActivity extends AppCompatActivity {

    private ListView userListView;

    public static void start(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userListView = (ListView) findViewById(R.id.list_login_users);

        TextView listHeader = (TextView) LayoutInflater.from(this).inflate(R.layout.include_list_hint_header, userListView, false);
        listHeader.setText(R.string.login_select_user_for_login);
        userListView.addHeaderView(listHeader);

        buildUserList();
    }

    private void buildUserList() {
        // TODO
    }
}
