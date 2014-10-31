package com.quickblox.ratings.main.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.quickblox.core.QBCallback;
import com.quickblox.core.result.Result;
import com.quickblox.ratings.result.QBAverageResult;
import com.quickblox.ratings.main.R;
import com.quickblox.ratings.main.adapter.MoviesListAdapter;
import com.quickblox.ratings.main.core.DataHolder;
import com.quickblox.ratings.main.definitions.QBQueries;
import com.quickblox.ratings.main.utils.DialogUtils;

public class MoviesListActivity extends BaseActivity implements AdapterView.OnItemClickListener, QBCallback {

    private ListView moviesListView;
    private MoviesListAdapter moviesListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies_list);
        initUI();
        initUIMoviesList();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(currentPosition != NONE_SCORE_CHANGE) {
            getAverageRatingForMovie(currentPosition, QBQueries.GET_AVERAGE_FOR_GAME_MODE, this);
        }
    }

    private void initUI() {
        moviesListView = (ListView) findViewById(R.id.movies_listview);
    }

    private void initUIMoviesList() {
        moviesListAdapter = new MoviesListAdapter(this);
        moviesListView.setAdapter(moviesListAdapter);
        moviesListView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Intent intent = new Intent(this, MovieActivity.class);
        intent.putExtra(POSITION, position);
        currentPosition = position;
        startActivity(intent);
    }

    @Override
    public void onComplete(Result result) {
    }

    @Override
    public void onComplete(Result result, Object context) {
        QBQueries qbQueries = (QBQueries) context;
        if (result.isSuccess()) {
            switch (qbQueries) {
                case GET_AVERAGE_FOR_GAME_MODE:
                    QBAverageResult qbAverageResult1 = (QBAverageResult) result;
                    if (qbAverageResult1.getAverage().getValue() != null) {
                        DataHolder.getDataHolder().setMovieRating(
                                currentPosition,
                                qbAverageResult1.getAverage().getValue());
                        DataHolder.getDataHolder().setChosenMoviePosition(NONE_SCORE_CHANGE);
                        moviesListAdapter.notifyDataSetChanged();
                    }
                    break;
            }
        } else {
            DialogUtils.showLong(this, result.getErrors().get(0));
        }
        progressDialog.hide();
    }
}