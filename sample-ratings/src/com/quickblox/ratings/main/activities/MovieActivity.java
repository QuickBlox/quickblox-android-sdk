package com.quickblox.ratings.main.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.quickblox.core.QBCallback;
import com.quickblox.core.result.Result;
import com.quickblox.module.ratings.QBRatings;
import com.quickblox.module.ratings.model.QBScore;
import com.quickblox.module.ratings.result.QBAverageResult;
import com.quickblox.ratings.main.R;
import com.quickblox.ratings.main.core.DataHolder;
import com.quickblox.ratings.main.definitions.QBQueries;
import com.quickblox.ratings.main.utils.DialogUtils;

public class MovieActivity extends BaseActivity implements QBCallback {

    private ImageView movieCoverImageView;
    private TextView movieDescriptionTextView;
    private RatingBar ratingBar;
    private RatingBar dialogRatingBar;
    private Dialog rateDialog;

    public void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);
        setContentView(R.layout.activity_movie);
        currentPosition = getIntent().getIntExtra(POSITION, 0);
        initUI();
        applyStars(DataHolder.getDataHolder().getMovieRating(currentPosition));
    }

    private void initUI() {
        actionBar.setDisplayHomeAsUpEnabled(true);
        movieCoverImageView = (ImageView) findViewById(R.id.movie_cover_imageview);
        movieDescriptionTextView = (TextView) findViewById(R.id.movie_description_imageview);
        ratingBar = (RatingBar) findViewById(R.id.rating_bar);
        movieCoverImageView.setImageDrawable(DataHolder.getDataHolder().getMovieCover(currentPosition));
        movieDescriptionTextView.setText(DataHolder.getDataHolder().getMovieDescription(currentPosition));
    }

    private void applyStars(double movieRating) {
        ratingBar.setRating((float) movieRating);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rate_dialog_button:
                createScore();
                rateDialog.dismiss();
                break;
            case R.id.rate_button:
                rateDialog = getRateDialog();
                rateDialog.show();
                break;
            case R.id.cancel_button:
                rateDialog.dismiss();
                break;
        }
    }

    public Dialog getRateDialog() {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        RelativeLayout dialog = (RelativeLayout) layoutInflater.inflate(R.layout.dialog_rate, null);
        dialogRatingBar = (RatingBar) dialog.findViewById(R.id.rating_bar);
        builder.setView(dialog);
        return builder.create();
    }

    private void createScore() {
        progressDialog.show();
        // ================= QuickBlox ===== Step 3 =================
        // Rate it!
        QBScore qbScore = new QBScore();
        qbScore.setGameModeId(DataHolder.getDataHolder().getMovieGameModeId(currentPosition));
        qbScore.setValue((int) dialogRatingBar.getRating());
        qbScore.setUserId(DataHolder.getDataHolder().getQbUserId());
        QBRatings.createScore(qbScore, this, QBQueries.CREATE_SCORE);
    }

    private void updateScore() {
        getAverageRatingForMovie(currentPosition, QBQueries.GET_AVERAGE_FOR_GAME_MODE, this);
    }

    @Override
    public void onComplete(Result result) {}

    @Override
    public void onComplete(Result result, Object context) {
        QBQueries qbQueries = (QBQueries) context;
        if (result.isSuccess()) {
            switch (qbQueries) {
                case CREATE_SCORE:
                    DialogUtils.showLong(this, resources.getString(R.string.score_success_send));
                    DataHolder.getDataHolder().setChosenMoviePosition(currentPosition);
                    updateScore();
                    break;
                case GET_AVERAGE_FOR_GAME_MODE:
                    QBAverageResult qbAverageResult1 = (QBAverageResult) result;
                    if (qbAverageResult1.getAverage().getValue() != null) {
                        DataHolder.getDataHolder().setMovieRating(currentPosition,
                                qbAverageResult1.getAverage().getValue());
                        DataHolder.getDataHolder().setChosenMoviePosition(NONE_SCORE_CHANGE);
                        applyStars(qbAverageResult1.getAverage().getValue());
                    }
                    break;
            }
        } else {
            DialogUtils.showLong(this, result.getErrors().get(0));
        }
        progressDialog.hide();
    }
}