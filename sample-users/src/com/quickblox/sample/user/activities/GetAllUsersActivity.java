package com.quickblox.sample.user.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import com.quickblox.core.QBCallback;
import com.quickblox.core.result.Result;
import com.quickblox.module.auth.QBAuth;
import com.quickblox.sample.user.R;
import com.quickblox.sample.user.adapter.UserListAdapter;
import com.quickblox.sample.user.definitions.QBQueries;
import com.quickblox.sample.user.helper.DataHolder;
import com.quickblox.sample.user.managers.QBManager;

import static com.quickblox.sample.user.definitions.Consts.POSITION;

public class GetAllUsersActivity extends Activity implements QBCallback, AdapterView.OnItemClickListener {

    UserListAdapter userListAdapter;
    ListView userList;
    ProgressDialog progressDialog;
    Button logOut;
    Button signIn;
    Button selfEdit;
    Button singUp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_list);
        initialize();
        userList.setOnItemClickListener(this);
    }

    private void initialize() {
        logOut = (Button) findViewById(R.id.logout);
        signIn = (Button) findViewById(R.id.sign_in);
        selfEdit = (Button) findViewById(R.id.self_edit);
        singUp = (Button) findViewById(R.id.sign_up);
        userList = (ListView) findViewById(R.id.user_list);
        userListAdapter = new UserListAdapter(this);
        userList.setAdapter(userListAdapter);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getResources().getString(R.string.please_wait));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (DataHolder.getDataHolder().getSignInQbUser() != null) {
            signIn.setVisibility(View.INVISIBLE);
            singUp.setVisibility(View.INVISIBLE);
            logOut.setVisibility(View.VISIBLE);
            selfEdit.setVisibility(View.VISIBLE);
        }
        userListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // destroy session after app close
        QBAuth.deleteSession(this);
    }

    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.sign_in:
                intent = new Intent(this, SignInActivity.class);
                startActivityForResult(intent, 0);
                break;
            case R.id.sign_up:
                intent = new Intent(this, SignUpUserActivity.class);
                startActivity(intent);
                break;
            case R.id.logout:
                progressDialog.show();
                // call query to sign out by current user
                QBManager.signOut(this, QBQueries.QB_QUERY_LOG_OUT_QB_USER);
                break;
            case R.id.self_edit:
                intent = new Intent(this, UpdateUserActivity.class);
                startActivity(intent);
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            signIn.setVisibility(View.INVISIBLE);
            logOut.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onComplete(Result result) {
    }


    @Override
    public void onComplete(Result result, Object context) {
        QBQueries qbQueryType = (QBQueries) context;
        if (result.isSuccess()) {
            switch (qbQueryType) {
                case QB_QUERY_LOG_OUT_QB_USER:
                    Toast.makeText(getBaseContext(), getResources().getString(R.string.user_log_out_msg), Toast.LENGTH_SHORT).show();
                    // set SignInQbUser null after logOut
                    DataHolder.getDataHolder().setSignInQbUser(null);
                    signIn.setVisibility(View.VISIBLE);
                    logOut.setVisibility(View.INVISIBLE);
                    selfEdit.setVisibility(View.INVISIBLE);
                    singUp.setVisibility(View.VISIBLE);
                    progressDialog.hide();
                    break;
            }
        } else {
            // print errors that came from server
            Toast.makeText(getBaseContext(), result.getErrors().get(0), Toast.LENGTH_SHORT).show();
            progressDialog.hide();
        }

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Intent intent = new Intent(this, ShowUserActivity.class);
        intent.putExtra(POSITION, position);
        startActivity(intent);
    }

}
