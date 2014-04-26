package com.quickblox.ratings.main.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.Toast;

import com.quickblox.core.QBCallback;
import com.quickblox.core.QBSettings;
import com.quickblox.core.result.Result;
import com.quickblox.module.auth.QBAuth;
import com.quickblox.module.auth.result.QBSessionResult;
import com.quickblox.module.ratings.QBRatings;
import com.quickblox.module.ratings.model.QBGameMode;
import com.quickblox.module.ratings.result.QBAverageResult;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.ratings.main.R;
import com.quickblox.ratings.main.core.DataHolder;
import com.quickblox.ratings.main.definitions.QBQueries;
import com.quickblox.ratings.main.object.Movie;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class SplashActivity extends Activity implements QBCallback {

    private final int APP_ID = 99;
    private final String AUTH_KEY = "63ebrp5VZt7qTOv";
    private final String AUTH_SECRET = "YavMAxm5T59-BRw";
    private final String USER_LOGIN = "Gerrit";
    private final String USER_PASSWORD = "qwerty123";
    private final int NONE_SCORE_CHANGE = -1;
    int i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        i = 0;
        applyMovies();

        // ================= QuickBlox ===== Step 1 =================
        // Initialize QuickBlox application with credentials.
        // Getting app credentials -- http://quickblox.com/developers/Getting_application_credentials
        QBSettings.getInstance().fastConfigInit(String.valueOf(APP_ID), AUTH_KEY, AUTH_SECRET);

        // Sign in by default user
        QBUser qbUser = new QBUser(USER_LOGIN, USER_PASSWORD);
        QBAuth.createSession(qbUser, this, QBQueries.SIGN_IN);
    }


    // Get average by all score for game mode
    private void getAverageRatingForMovie(int index, QBQueries queryName) {

        // ================= QuickBlox ===== Step 2 =================
        // Get averages
        QBGameMode qbGameMode = new QBGameMode();
        //        qbGameMode.setAppId(APP_ID);
        qbGameMode.setId(DataHolder.getDataHolder().getMovieGameModeId(index));
        QBRatings.getAverageByGameMode(qbGameMode, this, queryName);
    }


    private void applyMovies() {
        List<Movie> movieList = new ArrayList<Movie>();

        AssetManager assetManager = getAssets();
        InputStream ims;

        try {
            ims = assetManager.open("image_ted.jpg");
            movieList.add(new Movie(278, Drawable.createFromStream(ims, null), "Ted",
                    "As the result of a childhood wish, John Bennett's teddy bear, Ted, came to life and has been by John's side ever since - a friendship that's tested when Lori, John's girlfriend of four years, wants more from their relationship"));

            ims = getAssets().open("image_hachiko.jpg");
            movieList.add(new Movie(279, Drawable.createFromStream(ims, null), "Hachiko: A Dog's Tale",
                    "A drama based on the true story of a college professor's bond with the abandoned dog he takes into his home."
            ));

            ims = assetManager.open("image_godfather.jpg");
            movieList.add(new Movie(280, Drawable.createFromStream(ims, null), "The Godfather",
                    "The aging patriarch of an organized crime dynasty transfers control of his clandestine empire to his reluctant son."
            ));

            ims = assetManager.open("image_shawshank_redemption.jpg");
            movieList.add(new Movie(281, Drawable.createFromStream(ims, null), "The Shawshank Redemption",
                    "Two imprisoned men bond over a number of years, finding solace and eventual redemption through acts of common decency."
            ));

            ims = assetManager.open("image_the_lord_of_the_rings.jpg");
            movieList.add(new Movie(282, Drawable.createFromStream(ims, null),
                    "The Lord of the Rings: The Fellowship of the Ring",
                    "An innocent hobbit of The Shire journeys with eight companions to the fires of Mount Doom to destroy the One Ring and the dark lord Sauron forever."
            ));

            ims = assetManager.open("image_fight_club.jpg");
            movieList.add(new Movie(283, Drawable.createFromStream(ims, null), "Fight Club",
                    "An insomniac office worker and a devil-may-care soap maker form an underground fight club that transforms into a violent revolution."
            ));

            ims = assetManager.open("image_harry_potter.jpg");
            movieList.add(new Movie(284, Drawable.createFromStream(ims, null),
                    "Harry Potter and the Deathly Hallows",
                    "Harry, Ron and Hermione search for Voldemort's remaining Horcruxes in their effort to destroy the Dark Lord."
            ));
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        DataHolder.getDataHolder().setMovieList(movieList);
        DataHolder.getDataHolder().setChosenMoviePosition(NONE_SCORE_CHANGE);
    }

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
                    getAverageRatingForMovie(i, QBQueries.GET_AVERAGE_FOR_GAME_MODE);
                    break;
                case GET_AVERAGE_FOR_GAME_MODE:
                    // Result  for ---> getAverageByGameMode query
                    QBAverageResult qbAverageResult = (QBAverageResult) result;
                    if (qbAverageResult.getAverage().getValue() != null) {
                        DataHolder.getDataHolder().setMovieRating(i, qbAverageResult.getAverage().getValue());
                    }
                    if (i + 1 < DataHolder.getDataHolder().getMovieListSize()) {
                        getAverageRatingForMovie(++i, QBQueries.GET_AVERAGE_FOR_GAME_MODE);
                    } else {
                        startMoviesListActivity();
                    }
                    break;
            }
        } else {
            Toast.makeText(this, result.getErrors().get(0), Toast.LENGTH_SHORT).show();
        }
    }

    private void startMoviesListActivity() {
        Intent intent = new Intent(this, MoviesListActivity.class);
        startActivity(intent);
        finish();
    }
}