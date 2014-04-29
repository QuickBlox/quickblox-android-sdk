package com.quickblox.customobjects.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.quickblox.core.QBCallback;
import com.quickblox.core.result.Result;
import com.quickblox.customobjects.R;
import com.quickblox.customobjects.definition.QBQueries;
import com.quickblox.customobjects.helper.DataHolder;
import com.quickblox.customobjects.utils.DialogUtils;
import com.quickblox.module.custom.QBCustomObjects;
import com.quickblox.module.custom.model.QBCustomObject;
import com.quickblox.module.custom.result.QBCustomObjectResult;

import java.util.HashMap;

import static com.quickblox.customobjects.definition.Consts.CLASS_NAME;
import static com.quickblox.customobjects.definition.Consts.COMMENTS;
import static com.quickblox.customobjects.definition.Consts.STATUS;
import static com.quickblox.customobjects.definition.Consts.STATUS_NEW;
import static com.quickblox.customobjects.definition.Consts.TITLE;

public class AddNewNoteActivity extends BaseActivity implements QBCallback {

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

    @Override
    public void onComplete(Result result) {
    }

    @Override
    public void onComplete(Result result, Object context) {
        QBQueries qbQueryType = (QBQueries) context;
        if (result.isSuccess()) {
            switch (qbQueryType) {
                case CREATE_NOTE:
                    // return QBCustomObjectResult for createObject() query
                    QBCustomObjectResult qbCustomObjectResult = (QBCustomObjectResult) result;
                    QBCustomObject customObject = qbCustomObjectResult.getCustomObject();
                    DataHolder.getDataHolder().addNoteToList(customObject);
                    finish();
                    break;
            }
            progressDialog.dismiss();
        } else {
            // print errors that came from server
            DialogUtils.showLong(baseActivity, result.getErrors().get(0));
            progressDialog.dismiss();
        }
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
        QBCustomObjects.createObject(qbCustomObject, this, QBQueries.CREATE_NOTE);
    }

    private boolean isValidData(String note, String comments) {
        return (!TextUtils.isEmpty(note) && !TextUtils.isEmpty(comments));
    }
}