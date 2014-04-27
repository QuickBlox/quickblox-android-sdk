package com.quickblox.ratings.main.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.quickblox.core.QBCallback;
import com.quickblox.core.result.Result;
import com.quickblox.module.ratings.QBRatings;
import com.quickblox.module.ratings.model.QBScore;
import com.quickblox.ratings.main.R;
import com.quickblox.ratings.main.core.DataHolder;
import com.quickblox.ratings.main.definitions.QBQueries;

public class MovieActivity extends Activity implements QBCallback {

    private final String POSITION = "position";
    private ImageView movieCover;
    private TextView movieDescription;
    private RatingBar ratingBar;
    private final int STARS_NUMBER = 10;
    private ProgressDialog progressDialog;
    private int position;
    private RatingBar dialogRatingBar;
    private LayoutInflater layoutInflater;
    private AlertDialog alert;

    public void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);
        setContentView(R.layout.activity_movie);
        layoutInflater = getLayoutInflater();
        initialize();
    }

    private void initialize() {
        position = getIntent().getIntExtra(POSITION, 0);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.please_wait));
        progressDialog.setCancelable(false);

        movieCover = (ImageView) findViewById(R.id.movie_cover_imageview);
        movieDescription = (TextView) findViewById(R.id.movie_description_imageview);
        ratingBar = (RatingBar) findViewById(R.id.rating_bar);
        movieCover.setImageDrawable(DataHolder.getDataHolder().getMovieCover(position));
        movieDescription.setText(DataHolder.getDataHolder().getMovieDescription(position));
        applyStars(DataHolder.getDataHolder().getMovieRating(position));
    }

    private void applyStars(double movieRating) {
       ratingBar.setNumStars(STARS_NUMBER);
        ratingBar.setRating((float) movieRating);
    }

    private void showCustomAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        RelativeLayout customDialog = (RelativeLayout) layoutInflater.inflate(R.layout.dialog_rate, null);
        dialogRatingBar = (RatingBar) customDialog.findViewById(R.id.rating_bar);
        builder.setView(customDialog);
        alert = builder.create();
        alert.show();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rate_dialog_button:
                // Create new Score
                createScore();
                this.alert.dismiss();
                break;
            case R.id.rate_button:
                showCustomAlert();
                break;
            case R.id.cancel_button:
                this.alert.dismiss();
                break;
        }
    }

    private void createScore() {
        progressDialog.show();
        // ================= QuickBlox ===== Step 3 =================
        // Rate it!
        QBScore qbScore = new QBScore();
        qbScore.setGameModeId(DataHolder.getDataHolder().getMovieGameModeId(position));
        qbScore.setValue((int) dialogRatingBar.getRating());
        qbScore.setUserId(DataHolder.getDataHolder().getQbUserId());
        QBRatings.createScore(qbScore, this, QBQueries.CREATE_SCORE);
    }

    @Override
    public void onComplete(Result result) {}

    @Override
    public void onComplete(Result result, Object context) {
        QBQueries qbQueries = (QBQueries) context;
        if (result.isSuccess()) {
            switch (qbQueries) {
                case CREATE_SCORE:
                    Toast.makeText(this, "Score successfully send!", Toast.LENGTH_SHORT).show();
                    DataHolder.getDataHolder().setChosenMoviePosition(position);
                    break;
            }
        } else {
            Toast.makeText(this, result.getErrors().get(0), Toast.LENGTH_SHORT).show();
        }
        progressDialog.hide();
    }
}