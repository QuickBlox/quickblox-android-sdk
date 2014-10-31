package com.quickblox.sample.customobjects.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.sample.customobjects.R;
import com.quickblox.sample.customobjects.helper.DataHolder;
import com.quickblox.sample.customobjects.model.Note;
import com.quickblox.sample.customobjects.utils.DialogUtils;
import com.quickblox.customobjects.QBCustomObjects;
import com.quickblox.customobjects.model.QBCustomObject;

import java.util.HashMap;
import java.util.List;

import static com.quickblox.sample.customobjects.definition.Consts.CLASS_NAME;
import static com.quickblox.sample.customobjects.definition.Consts.COMMENTS;
import static com.quickblox.sample.customobjects.definition.Consts.STATUS;
import static com.quickblox.sample.customobjects.definition.Consts.STATUS_DONE;
import static com.quickblox.sample.customobjects.definition.Consts.STATUS_IN_PROCESS;
import static com.quickblox.sample.customobjects.definition.Consts.STATUS_NEW;

public class ShowNoteActivity extends BaseActivity {

    private final String POSITION = "position";
    private TextView titleTextView;
    private TextView statusTextView;
    private TextView commentsTextView;
    private int position;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        initUI();
        fillFields();
    }

    private void initUI() {
        actionBar.setDisplayHomeAsUpEnabled(true);
        position = getIntent().getIntExtra(POSITION, 0);
        titleTextView = (TextView) findViewById(R.id.note_textview);
        statusTextView = (TextView) findViewById(R.id.status_textview);
        commentsTextView = (TextView) findViewById(R.id.comments_textview);
    }

    private void fillFields() {
        titleTextView.setText(DataHolder.getDataHolder().getNoteTitle(position));
        statusTextView.setText(DataHolder.getDataHolder().getNoteStatus(position));
        applyComment();
    }

    private void applyComment() {
        String commentsStr = "";
        for (int i = 0; i < DataHolder.getDataHolder().getNoteComments(position).size(); ++i) {
            commentsStr += "#" + i + "-" + DataHolder.getDataHolder().getNoteComments(position).get(
                    i) + "\n\n";
        }
        commentsTextView.setText(commentsStr);
    }


    private void setNewNote(QBCustomObject qbCustomObject) {
        Note note = new Note(qbCustomObject);
        DataHolder.getDataHolder().setNoteToNoteList(position, note);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add_comment_button:
                showAddNewCommentDialog();
                break;
            case R.id.change_status_button:
                showSetNewStatusDialog();
                break;
            case R.id.delete_button:
                progressDialog.show();

                // Delete note
                QBCustomObjects.deleteObject(CLASS_NAME, DataHolder.getDataHolder().getNoteId(position), new QBEntityCallbackImpl() {

                    @Override
                    public void onSuccess() {
                        progressDialog.dismiss();

                        DataHolder.getDataHolder().removeNoteFromList(position);
                        DialogUtils.showLong(baseActivity, baseActivity.getResources().getString(
                                R.string.note_successfully_deleted));
                        finish();
                    }

                    @Override
                    public void onError(List list) {
                        DialogUtils.showLong(baseActivity, list.get(0).toString());

                        progressDialog.dismiss();
                    }
                });

                break;
        }
    }

    private void showAddNewCommentDialog() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(getResources().getString(R.string.new_comment));
        alert.setMessage(getResources().getString(R.string.write_new_comment));
        final EditText input = new EditText(this);
        input.setTextColor(getResources().getColor(R.color.white));
        alert.setView(input);
        input.setSingleLine();
        alert.setPositiveButton(getBaseContext().getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int buttonNumber) {
                        progressDialog.show();
                        addNewComment(input.getText().toString());
                        dialog.cancel();
                    }
                }
        );

        alert.setNegativeButton(getResources().getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                }
        );
        alert.show();
    }

    private void showSetNewStatusDialog() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final String[] statusList = {STATUS_NEW, STATUS_IN_PROCESS, STATUS_DONE};
        alert.setTitle(getBaseContext().getResources().getString(R.string.choose_new_status));

        alert.setItems(statusList, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                progressDialog.show();
                String status;
                if (item == 0) {
                    status = STATUS_NEW;
                } else if (item == 1) {
                    status = STATUS_IN_PROCESS;
                } else {
                    status = STATUS_DONE;
                }
                updateNoteStatus(status);
            }
        });
        alert.show();
    }

    private void addNewComment(String comment) {
        DataHolder.getDataHolder().addNewComment(position, comment);
        // create query for update activity_note status
        // set class name
        // add new comments
        HashMap<String, Object> fields = new HashMap<String, Object>();
        fields.put(COMMENTS, DataHolder.getDataHolder().getComments(position));
        QBCustomObject qbCustomObject = new QBCustomObject();
        qbCustomObject.setCustomObjectId(DataHolder.getDataHolder().getNoteId(position));
        qbCustomObject.setClassName(CLASS_NAME);
        qbCustomObject.setFields(fields);

        QBCustomObjects.updateObject(qbCustomObject, new QBEntityCallbackImpl<QBCustomObject>() {
            @Override
            public void onSuccess(QBCustomObject qbCustomObject, Bundle bundle) {
                progressDialog.dismiss();

                setNewNote(qbCustomObject);
                applyComment();
            }

            @Override
            public void onError(List<String> strings) {
                progressDialog.dismiss();

                DialogUtils.showLong(baseActivity, strings.get(0).toString());
            }
        });
    }

    private void updateNoteStatus(String status) {

        HashMap<String, Object> fields = new HashMap<String, Object>();
        fields.put(STATUS, status);
        QBCustomObject qbCustomObject = new QBCustomObject();
        qbCustomObject.setCustomObjectId(DataHolder.getDataHolder().getNoteId(position));
        qbCustomObject.setClassName(CLASS_NAME);
        qbCustomObject.setFields(fields);

        QBCustomObjects.updateObject(qbCustomObject, new QBEntityCallbackImpl<QBCustomObject>() {
            @Override
            public void onSuccess(QBCustomObject qbCustomObject, Bundle bundle) {
                progressDialog.dismiss();

                setNewNote(qbCustomObject);
                statusTextView.setText(DataHolder.getDataHolder().getNoteStatus(position));
            }

            @Override
            public void onError(List<String> strings) {
                progressDialog.dismiss();

                DialogUtils.showLong(baseActivity, strings.get(0).toString());
            }
        });
    }
}