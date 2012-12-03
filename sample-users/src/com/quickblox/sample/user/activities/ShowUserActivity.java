package com.quickblox.sample.user.activities;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;
import com.quickblox.sample.user.R;
import com.quickblox.sample.user.helper.DataHolder;

import static com.quickblox.sample.user.definitions.Consts.POSITION;

/**
 * Created with IntelliJ IDEA.
 * User: android
 * Date: 20.11.12
 * Time: 15:35
 */
public class ShowUserActivity extends Activity {

    EditText login;
    EditText email;
    EditText fullName;
    EditText phone;
    EditText webSite;
    EditText tags;
    int position;

    @Override
    public void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);
        setContentView(R.layout.show_user);
        initialize();
    }

    private void initialize() {
        position = getIntent().getIntExtra(POSITION, 0);
        login = (EditText) findViewById(R.id.login);
        email = (EditText) findViewById(R.id.email);
        fullName = (EditText) findViewById(R.id.full_name);
        phone = (EditText) findViewById(R.id.phone);
        webSite = (EditText) findViewById(R.id.web_site);
        tags = (EditText) findViewById(R.id.tags);
        fillAllFields();
    }


    private void fillAllFields() {

        login.setText(DataHolder.getDataHolder().getQBUser(position).getLogin());
        email.setText(DataHolder.getDataHolder().getQBUser(position).getEmail());
        fullName.setText(DataHolder.getDataHolder().getQBUser(position).getFullName());
        phone.setText(DataHolder.getDataHolder().getQBUser(position).getPhone());
        webSite.setText(DataHolder.getDataHolder().getQBUser(position).getWebsite());
        if (DataHolder.getDataHolder().getQBUser(position).getTags() != null) {
            tags.append(DataHolder.getDataHolder().getQBUser(position).getTags().toString());
        }
    }

}
