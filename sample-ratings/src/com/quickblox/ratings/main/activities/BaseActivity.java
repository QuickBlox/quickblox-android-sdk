package com.quickblox.ratings.main.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import com.quickblox.core.QBCallback;
import com.quickblox.module.ratings.QBRatings;
import com.quickblox.module.ratings.model.QBGameMode;
import com.quickblox.ratings.main.core.DataHolder;
import com.quickblox.ratings.main.definitions.QBQueries;
import com.quickblox.ratings.main.utils.DialogUtils;

public class BaseActivity extends ActionBarActivity  {

    protected final int NONE_SCORE_CHANGE = -1;
    protected final String POSITION = "position";
    protected int currentPosition = -1;

    protected Context context;
    protected Resources resources;
    protected ProgressDialog progressDialog;
    protected ActionBar actionBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        actionBar = getSupportActionBar();
        progressDialog = DialogUtils.getProgressDialog(this);
        resources = getResources();
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

    // Get average by all score for game mode
    protected void getAverageRatingForMovie(int index, QBQueries queryName, QBCallback qbCallback) {
        QBGameMode qbGameMode = new QBGameMode();
        qbGameMode.setId(DataHolder.getDataHolder().getMovieGameModeId(index));
        QBRatings.getAverageByGameMode(qbGameMode, qbCallback, queryName);
    }
}