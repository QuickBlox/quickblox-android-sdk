package com.quickblox.chat_v2.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.adapters.DialogsAdapter;
import com.quickblox.chat_v2.core.DataHolder;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.internal.module.custom.request.QBCustomObjectRequestBuilder;
import com.quickblox.module.custom.QBCustomObjects;
import com.quickblox.module.custom.model.QBCustomObject;
import com.quickblox.module.custom.result.QBCustomObjectLimitedResult;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew Dmitrenko
 * Date: 11.04.13
 * Time: 9:58
 */
public class DialogsActivity extends Activity {

    private static final String DIALOGS = "dialogs";
    private static final String USER_ID_FIELD = "user_id";

    private ListView dialogsListView;
    private DialogsAdapter dialogsAdapter;
    private Button newDialogButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_list_layout);
        initialize();
    }

    private void initialize() {
        applyDialogList();
        dialogsListView = (ListView) findViewById(R.id.dialogs_listView);
        newDialogButton = (Button) findViewById(R.id.new_dialog_button);
        newDialogButton.setOnClickListener(newDialogButtonClickListener);
    }

    View.OnClickListener newDialogButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getBaseContext(), NewDialogActivity.class);
            startActivity(intent);
        }
    };


    private void applyDialogList() {
        QBCustomObjectRequestBuilder requestBuilder = new QBCustomObjectRequestBuilder();
        requestBuilder.eq(USER_ID_FIELD, DataHolder.getInstance().getQbUser().getId());
        QBCustomObjects.getObjects(DIALOGS, requestBuilder, new QBCallbackImpl() {
            @Override
            public void onComplete(Result result) {
                if (result.isSuccess()) {
                    List<QBCustomObject> dialogList = ((QBCustomObjectLimitedResult) result).getCustomObjects();
                    dialogsAdapter = new DialogsAdapter(MainActivity.getContext(), dialogList);
                    dialogsListView.setAdapter(dialogsAdapter);
                }
            }
        });
    }

}
