package com.quickblox.sample.customobjects.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RatingBar;
import android.widget.TextView;

import com.quickblox.sample.customobjects.R;
import com.quickblox.sample.customobjects.utils.Consts;
import com.quickblox.sample.customobjects.helper.DataHolder;
import com.quickblox.sample.customobjects.model.Movie;

public class ShowMovieActivity extends BaseActivity {

    private TextView nameTextView;
    private TextView yearTextView;
    private TextView descriptionTextView;
    private RatingBar ratingBar;

    public static void start(Context context, String id) {
        Intent intent = new Intent(context, ShowMovieActivity.class);
        intent.putExtra(Consts.EXTRA_MOVIE_ID, id);
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
        nameTextView = _findViewById(R.id.movie_title_textview);
        yearTextView = _findViewById(R.id.text_movie_year);
        descriptionTextView = _findViewById(R.id.movie_description_textview);
        ratingBar = _findViewById(R.id.rating_movie);
    }

    private void fillFields() {
        String id = getIntent().getStringExtra(Consts.EXTRA_MOVIE_ID);
        Movie movie = DataHolder.getInstance().getMovieObject(id);
        fillField(nameTextView, movie.getName());
        fillField(yearTextView, movie.getYear());
        fillField(descriptionTextView, movie.getDescription());
        ratingBar.setRating(movie.getRating());
    }
}