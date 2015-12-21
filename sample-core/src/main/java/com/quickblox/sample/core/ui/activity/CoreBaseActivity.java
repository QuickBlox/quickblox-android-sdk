package com.quickblox.sample.core.ui.activity;

import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

public class CoreBaseActivity extends AppCompatActivity {

    @SuppressWarnings("unchecked")
    public <T extends View> T _findViewById(int viewId) {
        return (T) findViewById(viewId);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
