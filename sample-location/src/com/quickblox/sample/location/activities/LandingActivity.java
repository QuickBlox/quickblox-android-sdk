package com.quickblox.sample.location.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.quickblox.sample.location.R;

public class LandingActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);
    }

    public void onItemClick(View view) {
        if (R.id.atm_near == view.getId()){
            MapActivity.start(this, R.drawable.map_marker_my);
        } else if (R.id.branch_near == view.getId()){
            MapActivity.start(this, R.drawable.map_marker_other);
        }
    }
}
