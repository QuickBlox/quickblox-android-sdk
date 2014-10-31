package com.quickblox.sample.customobjects.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.sample.customobjects.R;
import com.quickblox.sample.customobjects.helper.DataHolder;
import com.quickblox.sample.customobjects.utils.DialogUtils;
import com.quickblox.customobjects.QBCustomObjects;
import com.quickblox.customobjects.model.QBCustomObject;

import java.util.HashMap;
import java.util.List;

import static com.quickblox.sample.customobjects.definition.Consts.CLASS_NAME;
import static com.quickblox.sample.customobjects.definition.Consts.COMMENTS;
import static com.quickblox.sample.customobjects.definition.Consts.STATUS;
import static com.quickblox.sample.customobjects.definition.Consts.STATUS_NEW;
import static com.quickblox.sample.customobjects.definition.Consts.TITLE;

public class AddNewNoteActivity extends BaseActivity {

    private EditText noteEditText;
    private EditText commentsEditText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);
        initUI();
    }

    private void initUI() {
        actionBar.setDisplayHomeAsUpEnabled(true);
        noteEditText = (EditText) findViewById(R.id.note_edittext);
        commentsEditText = (EditText) findViewById(R.id.comments_edittext);
    }



    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.save_button:
                createNewNote();
                break;
        }
    }

    private void createNewNote() {
        // create new score in activity_note class

        String note = noteEditText.getText().toString();
        String comments = commentsEditText.getText().toString();

        if(!isValidData(note, comments)) {
            DialogUtils.showLong(baseActivity, baseActivity.getResources().getString(R.string.error_fields_is_empty));
            return;
        }

        progressDialog.show();

        HashMap<String, Object> fields = new HashMap<String, Object>();
        fields.put(TITLE, note);
        fields.put(COMMENTS, comments);
        fields.put(STATUS, STATUS_NEW);
        QBCustomObject qbCustomObject = new QBCustomObject();
        qbCustomObject.setClassName(CLASS_NAME);
        qbCustomObject.setFields(fields);

        QBCustomObjects.createObject(qbCustomObject, new QBEntityCallbackImpl<QBCustomObject>() {
            @Override
            public void onSuccess(QBCustomObject qbCustomObject, Bundle bundle) {
                progressDialog.dismiss();

                DataHolder.getDataHolder().addNoteToList(qbCustomObject);
                finish();
            }

            @Override
            public void onError(List<String> errors) {
                progressDialog.dismiss();
                DialogUtils.showLong(baseActivity, errors.get(0));
            }
        });
    }

    private boolean isValidData(String note, String comments) {
        return (!TextUtils.isEmpty(note) && !TextUtils.isEmpty(comments));
    }
}