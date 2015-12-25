package com.quickblox.sample.customobjects.activities;

import android.os.Bundle;
import android.widget.RatingBar;
import android.widget.TextView;

import com.quickblox.sample.customobjects.R;
import com.quickblox.sample.customobjects.helper.DataHolder;

public class ShowMovieActivity extends BaseActivity {

    private static final String EXTRA_POSITION = "position";

    private TextView nameTextView;
    private TextView yearTextView;
    private TextView descriptionTextView;
    private RatingBar ratingBar;

    private int position;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);
        initUI();
        fillFields();
    }

    private void initUI() {
        actionBar.setDisplayHomeAsUpEnabled(true);
        position = getIntent().getIntExtra(EXTRA_POSITION, 0);
        nameTextView = (TextView) findViewById(R.id.movie_name_textview);
        yearTextView = (TextView) findViewById(R.id.movie_year_textview);
        descriptionTextView = (TextView) findViewById(R.id.movie_description_textview);
        ratingBar = (RatingBar) findViewById(R.id.movie_ratingBar);
    }

    private void fillFields() {
        nameTextView.setText(DataHolder.getDataHolder().getMovieName(position));
        yearTextView.setText(DataHolder.getDataHolder().getMovieYear(position));
        descriptionTextView.setText(DataHolder.getDataHolder().getMovieDescription(position));
        ratingBar.setRating(Float.parseFloat(DataHolder.getDataHolder().getMovieRating(position)));
    }
}