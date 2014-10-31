package com.quickblox.ratings.main.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.quickblox.core.QBCallback;
import com.quickblox.core.QBSettings;
import com.quickblox.core.result.Result;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.result.QBSessionResult;
import com.quickblox.ratings.QBRatings;
import com.quickblox.ratings.model.QBGameMode;
import com.quickblox.ratings.result.QBAverageResult;
import com.quickblox.users.model.QBUser;
import com.quickblox.ratings.main.R;
import com.quickblox.ratings.main.core.DataHolder;
import com.quickblox.ratings.main.definitions.Consts;
import com.quickblox.ratings.main.definitions.QBQueries;
import com.quickblox.ratings.main.model.Movie;
import com.quickblox.ratings.main.utils.DialogUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class SplashActivity extends Activity implements QBCallback {

    private final int NONE_SCORE_CHANGE = -1;

    private ProgressBar progressBar;

    private Resources resources;
    private int index = 0;

    @Override
    public void onComplete(Result result) {

    }

    @Override
    public void onComplete(Result result, Object context) {
        QBQueries qbQueries = (QBQueries) context;
        if (result.isSuccess()) {
            switch (qbQueries) {
                case SIGN_IN:
                    DataHolder.getDataHolder().setQbUserId(
                            ((QBSessionResult) result).getSession().getUserId());

                    getAverageRatingForMovie(index, QBQueries.GET_AVERAGE_FOR_GAME_MODE);
                    break;
                case GET_AVERAGE_FOR_GAME_MODE:
                    // Result  for ---> getAverageByGameMode query
                    QBAverageResult qbAverageResult = (QBAverageResult) result;
                    if (qbAverageResult.getAverage().getValue() != null) {
                        DataHolder.getDataHolder().setMovieRating(index,
                                qbAverageResult.getAverage().getValue());
                    }
                    if (index + 1 < DataHolder.getDataHolder().getMovieListSize()) {
                        getAverageRatingForMovie(++index, QBQueries.GET_AVERAGE_FOR_GAME_MODE);
                    } else {
                        startMoviesListActivity();
                    }
                    break;
            }
        } else {
            DialogUtils.showLong(this, result.getErrors().get(0));
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    // Get average by all score for game mode
    private void getAverageRatingForMovie(int index, QBQueries queryName) {

        // ================= QuickBlox ===== Step 2 =================
        // Get averages
        QBGameMode qbGameMode = new QBGameMode();
        qbGameMode.setId(DataHolder.getDataHolder().getMovieGameModeId(index));
        QBRatings.getAverageByGameMode(qbGameMode, this, queryName);
    }

    private void startMoviesListActivity() {
        Intent intent = new Intent(this, MoviesListActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        resources = getResources();

        initUI();
        initMovies();

        // ================= QuickBlox ===== Step 1 =================
        // Initialize QuickBlox application with credentials.
        // Getting app credentials -- http://quickblox.com/developers/Getting_application_credentials
        QBSettings.getInstance().fastConfigInit(String.valueOf(Consts.APP_ID), Consts.AUTH_KEY,
                Consts.AUTH_SECRET);

        // Sign in by default user
        QBUser qbUser = new QBUser(Consts.USER_LOGIN, Consts.USER_PASSWORD);
        QBAuth.createSession(qbUser, this, QBQueries.SIGN_IN);
    }

    private void initUI() {
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void initMovies() {
        List<Movie> movieList = new ArrayList<Movie>();

        AssetManager assetManager = getAssets();
        InputStream inputStream;

        try {
            inputStream = assetManager.open(resources.getString(R.string.film_ted_cover));
            movieList.add(new Movie(Integer.parseInt(resources.getString(R.string.film_ted_id)),
                    Drawable.createFromStream(inputStream, null), resources.getString(R.string.film_ted_name),
                    resources.getString(R.string.film_ted_description)));

            inputStream = assetManager.open(resources.getString(R.string.film_hachiko_cover));
            movieList.add(new Movie(Integer.parseInt(resources.getString(R.string.film_hachiko_id)),
                    Drawable.createFromStream(inputStream, null), resources.getString(
                    R.string.film_hachiko_name), resources.getString(R.string.film_hachiko_description)
            ));

            inputStream = assetManager.open(resources.getString(R.string.film_godfather_cover));
            movieList.add(new Movie(Integer.parseInt(resources.getString(R.string.film_godfather_id)),
                    Drawable.createFromStream(inputStream, null), resources.getString(
                    R.string.film_godfather_name), resources.getString(R.string.film_godfather_description)
            ));

            inputStream = assetManager.open(resources.getString(R.string.film_shawshank_redemption_cover));
            movieList.add(new Movie(Integer.parseInt(resources.getString(
                    R.string.film_shawshank_redemption_id)), Drawable.createFromStream(inputStream, null),
                    resources.getString(R.string.film_shawshank_redemption_name), resources.getString(
                    R.string.film_shawshank_redemption_description)
            ));

            inputStream = assetManager.open(resources.getString(R.string.film_the_lord_of_the_rings_cover));
            movieList.add(new Movie(Integer.parseInt(resources.getString(
                    R.string.film_the_lord_of_the_rings_id)), Drawable.createFromStream(inputStream, null),
                    resources.getString(R.string.film_the_lord_of_the_rings_name), resources.getString(
                    R.string.film_the_lord_of_the_rings_description)
            ));

            inputStream = assetManager.open(resources.getString(R.string.film_fight_club_cover));
            movieList.add(new Movie(Integer.parseInt(resources.getString(R.string.film_fight_club_id)),
                    Drawable.createFromStream(inputStream, null), resources.getString(
                    R.string.film_fight_club_name), resources.getString(R.string.film_fight_club_description)
            ));

            inputStream = assetManager.open(resources.getString(R.string.film_harry_potter_cover));
            movieList.add(new Movie(Integer.parseInt(resources.getString(R.string.film_harry_potter_id)),
                    Drawable.createFromStream(inputStream, null), resources.getString(
                    R.string.film_harry_potter_name), resources.getString(
                    R.string.film_harry_potter_description)
            ));

            inputStream = assetManager.open(resources.getString(R.string.film_i_robot_cover));
            movieList.add(new Movie(Integer.parseInt(resources.getString(R.string.film_i_robot_id)),
                    Drawable.createFromStream(inputStream, null), resources.getString(
                    R.string.film_i_robot_name), resources.getString(R.string.film_i_robot_description)
            ));

            inputStream = assetManager.open(resources.getString(R.string.film_spider_man_cover));
            movieList.add(new Movie(Integer.parseInt(resources.getString(R.string.film_spider_man_id)),
                    Drawable.createFromStream(inputStream, null), resources.getString(
                    R.string.film_spider_man_name), resources.getString(R.string.film_spider_man_description)
            ));

            inputStream = assetManager.open(resources.getString(R.string.film_robocop_cover));
            movieList.add(new Movie(Integer.parseInt(resources.getString(R.string.film_robocop_id)),
                    Drawable.createFromStream(inputStream, null), resources.getString(
                    R.string.film_robocop_name), resources.getString(R.string.film_robocop_description)
            ));
        } catch (IOException ex) {
            ex.printStackTrace();
            DialogUtils.showLong(this, ex.getMessage());
        }

        DataHolder.getDataHolder().setMoviesList(movieList);
        DataHolder.getDataHolder().setChosenMoviePosition(NONE_SCORE_CHANGE);
    }
}