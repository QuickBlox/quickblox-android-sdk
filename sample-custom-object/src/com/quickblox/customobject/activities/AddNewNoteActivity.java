package com.quickblox.customobject.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.quickblox.core.QBCallback;
import com.quickblox.core.result.Result;
import com.quickblox.customobject.R;
import com.quickblox.customobject.definition.QBQueries;
import com.quickblox.customobject.helper.DataHolder;
import com.quickblox.module.custom.QBCustomObjects;
import com.quickblox.module.custom.model.QBCustomObject;
import com.quickblox.module.custom.result.QBCustomObjectResult;

import java.util.HashMap;

import static com.quickblox.customobject.definition.Consts.CLASS_NAME;
import static com.quickblox.customobject.definition.Consts.COMMENTS;
import static com.quickblox.customobject.definition.Consts.STATUS;
import static com.quickblox.customobject.definition.Consts.STATUS_NEW;
import static com.quickblox.customobject.definition.Consts.TITLE;

public class AddNewNoteActivity extends Activity implements QBCallback {

    private EditText note;
    private EditText comments;
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_note);
        initialize();
    }

    @Override
    public void onComplete(Result result, Object context) {
        QBQueries qbQueryType = (QBQueries) context;
        if (result.isSuccess()) {
            switch (qbQueryType) {
                case CREATE_NOTE:
                    // return QBCustomObjectResult for createObject() query
                    QBCustomObjectResult qbCustomObjectResult = (QBCustomObjectResult) result;
                    QBCustomObject customObject= qbCustomObjectResult.getCustomObject();
                    DataHolder.getDataHolder().addNoteToList(customObject);
                    finish();
                    break;

            }
            progressDialog.dismiss();
        } else {
            // print errors that came from server
            Toast.makeText(getBaseContext(), result.getErrors().get(0), Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }
    }

    @Override
    public void onComplete(Result result) {
    }

    private void initialize() {
        note = (EditText) findViewById(R.id.note);
        comments = (EditText) findViewById(R.id.comments);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.save:
                createNewNote();
                break;
        }
    }

    private void createNewNote() {
        // create new score in note class
        showProgressDialog();
        HashMap<String, Object> fields = new HashMap<String, Object>();
        fields.put(TITLE, note.getText().toString());
        fields.put(COMMENTS, comments.getText().toString());
        fields.put(STATUS, STATUS_NEW);
        QBCustomObject qbCustomObject = new QBCustomObject();
        qbCustomObject.setClassName(CLASS_NAME);
        qbCustomObject.setFields(fields);
        QBCustomObjects.createObject(qbCustomObject, this, QBQueries.CREATE_NOTE);
    }

    private void showProgressDialog() {
        progressDialog = ProgressDialog.show(this, null, getResources().getString(R.string.please_wait), false, false);
    }
}
