package com.quickblox.chat_v2.ui.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.adapters.DialogsAdapter;
import com.quickblox.chat_v2.core.ChatApplication;
import com.quickblox.chat_v2.interfaces.OnDialogListRefresh;
import com.quickblox.chat_v2.utils.GlobalConsts;
import com.quickblox.module.custom.model.QBCustomObject;

import java.util.ArrayList;

public class DialogsActivity extends Activity implements OnDialogListRefresh, AdapterView.OnItemClickListener {

    private ProgressDialog progressDialog;

    private ListView dialogsListView;
    private DialogsAdapter dialogsAdapter;
    private Button newDialogButton;
    private TextView mEmptyDialogLabel;

    private ChatApplication app;

    private static final int REQUEST_NEW_DIALOG = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_list_layout);
        app = ChatApplication.getInstance();
        initialize();
    }

    @Override
    public void onResume() {
        super.onResume();
        app.getMsgManager().setDialogRefreshListener(this);
        app.getMsgManager().downloadDialogList(true);
        switchViewVisibility();
    }

    private void initialize() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getString(R.string.loading));
        dialogsListView = (ListView) findViewById(R.id.dialogs_listView);
        dialogsListView.setOnItemClickListener(this);
        newDialogButton = (Button) findViewById(R.id.new_dialog_button);
        newDialogButton.setOnClickListener(newDialogButtonClickListener);

        mEmptyDialogLabel = (TextView) findViewById(R.id.empty_dialog_label);

    }

    View.OnClickListener newDialogButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getBaseContext(), NewDialogActivity.class);
            intent.putExtra(GlobalConsts.PREVIOUS_ACTIVITY, GlobalConsts.DIALOG_ACTIVITY);
            startActivityForResult(intent, REQUEST_NEW_DIALOG);
        }
    };

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        QBCustomObject customObject = (QBCustomObject) adapterView.getItemAtPosition(i);
        loadChatActivity(customObject.getFields().get(GlobalConsts.RECEPIENT_ID_FIELD).toString(),
                customObject.getCustomObjectId());
    }


    private void applyDialogList() {
        if (app.getDialogMap() != null) {
            dialogsAdapter = new DialogsAdapter(DialogsActivity.this, new ArrayList<QBCustomObject>(app.getDialogMap().values()));
            dialogsListView.setAdapter(dialogsAdapter);

        } else {
            app.getMsgManager().downloadDialogList(true);
        }

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

    @Override
    public void reSetList() {
        applyDialogList();
        switchViewVisibility();

    }

    @Override
    public void reFreshList() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switchViewVisibility();
                dialogsAdapter.notifyDataSetChanged();


            }
        });

    }

    private void switchViewVisibility() {
        if (app.getDialogMap() != null && app.getDialogMap().size() > 0) {
            mEmptyDialogLabel.setVisibility(View.INVISIBLE);
            dialogsListView.setVisibility(View.VISIBLE);
        } else {
            dialogsListView.setVisibility(View.INVISIBLE);
            mEmptyDialogLabel.setVisibility(View.VISIBLE);
        }
    }

    private void loadChatActivity(String userId, String dialogId) {

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(GlobalConsts.PREVIOUS_ACTIVITY, GlobalConsts.DIALOG_ACTIVITY);
        intent.putExtra(GlobalConsts.USER_ID, userId);
        intent.putExtra(GlobalConsts.DIALOG_ID, dialogId);
        startActivity(intent);
    }
}
