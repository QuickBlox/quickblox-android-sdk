package com.quickblox.chat_v2.ui.activities;

import android.app.Activity;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.adapters.DialogsAdapter;
import com.quickblox.chat_v2.core.ChatApplication;
import com.quickblox.chat_v2.utils.GlobalConsts;
import com.quickblox.module.custom.model.QBCustomObject;

import java.util.ArrayList;
import java.util.List;

public class DialogsActivity extends ListActivity {

    private DialogsAdapter dialogsAdapter;
    private ChatApplication app;

    private static final int REQUEST_NEW_DIALOG = 0;

    private List<QBCustomObject> items = new ArrayList<QBCustomObject>();

    private BroadcastReceiver dialogRefreshReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context pContext, Intent pIntent) {
            refreshList();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_list_layout);


        findViewById(R.id.new_dialog_button).setOnClickListener(newDialogButtonClickListener);

        View textView = findViewById(android.R.id.empty);
        getListView().setEmptyView(textView);

        app = ChatApplication.getInstance();

        dialogsAdapter = new DialogsAdapter(this, items);
        setListAdapter(dialogsAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        app.getMsgManager().getDialogs(true);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(GlobalConsts.DIALOG_CREATED_ACTION);
        intentFilter.addAction(GlobalConsts.DIALOG_REFRESHED_ACTION);
        registerReceiver(dialogRefreshReceiver, intentFilter);
        refreshList();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(dialogRefreshReceiver);
    }

    private View.OnClickListener newDialogButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startCreateChatActivity();
        }
    };

    private void startCreateChatActivity() {
        Intent intent = new Intent(this, NewDialogActivity.class);
        intent.putExtra(GlobalConsts.PREVIOUS_ACTIVITY, GlobalConsts.DIALOG_ACTIVITY);
        startActivityForResult(intent, REQUEST_NEW_DIALOG);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        QBCustomObject customObject = (QBCustomObject) l.getItemAtPosition(position);
        startChatActivity(customObject.getFields().get(GlobalConsts.RECEPIENT_ID_FIELD).toString(),
                customObject.getCustomObjectId());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_NEW_DIALOG:
                if (resultCode != Activity.RESULT_CANCELED) {
                    Intent intent = new Intent(this, ChatActivity.class);
                    intent.putExtras(data.getExtras());
                    startActivity(intent);
                }

                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }

    }

    private void startChatActivity(String userId, String dialogId) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(GlobalConsts.PREVIOUS_ACTIVITY, GlobalConsts.DIALOG_ACTIVITY);
        intent.putExtra(GlobalConsts.USER_ID, userId);
        intent.putExtra(GlobalConsts.DIALOG_ID, dialogId);
        startActivity(intent);
    }


    private void refreshList() {
        items.clear();
        items.addAll(app.getDialogMap().values());
        dialogsAdapter.notifyDataSetChanged();
    }
}
