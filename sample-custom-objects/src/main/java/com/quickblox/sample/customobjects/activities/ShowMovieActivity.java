package com.quickblox.sample.customobjects.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RatingBar;
import android.widget.TextView;

import com.quickblox.sample.customobjects.R;
import com.quickblox.sample.customobjects.definition.Consts;
import com.quickblox.sample.customobjects.helper.DataHolder;
import com.quickblox.sample.customobjects.model.Movie;

public class ShowMovieActivity extends BaseActivity {

    private TextView nameTextView;
    private TextView yearTextView;
    private TextView descriptionTextView;
    private RatingBar ratingBar;

    public static void start(Context context, int id) {
        Intent intent = new Intent(context, ShowMovieActivity.class);
        intent.putExtra(Consts.EXTRA_POSITION, id);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);
        initUI();
        fillFields();
    }

    private void initUI() {
        actionBar.setDisplayHomeAsUpEnabled(true);
        nameTextView = _findViewById(R.id.text_movie_name);
        yearTextView = _findViewById(R.id.text_movie_year);
        descriptionTextView = _findViewById(R.id.text_movie_description);
        ratingBar = _findViewById(R.id.rating_movie);
    }

    private void fillFields() {
        // TODO Rename variable position to id, as well as constant EXTRA_POSITION to EXTRA_MOVIE_ID
        int position = getIntent().getIntExtra(Consts.EXTRA_POSITION, 0);
        Movie movie = DataHolder.getInstance().getMovieObject(position);
        nameTextView.setText(movie.getName());
        yearTextView.setText(movie.getYear());
        descriptionTextView.setText(movie.getDescription());
        ratingBar.setRating(movie.getRating());
    }
}