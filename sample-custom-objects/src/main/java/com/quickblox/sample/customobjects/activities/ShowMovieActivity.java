package com.quickblox.sample.customobjects.activities;

import android.os.Bundle;
import android.widget.RatingBar;
import android.widget.TextView;

import com.quickblox.sample.customobjects.R;
import com.quickblox.sample.customobjects.definition.Consts;
import com.quickblox.sample.customobjects.helper.DataHolder;

import java.text.NumberFormat;
import java.text.ParsePosition;

public class ShowMovieActivity extends BaseActivity {

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

        // TODO This has nothing to do with UI
        position = getIntent().getIntExtra(Consts.EXTRA_POSITION, 0);
        // TODO "text_movie_name" etc
        nameTextView = (TextView) findViewById(R.id.movie_name_textview);
        yearTextView = (TextView) findViewById(R.id.movie_year_textview);
        descriptionTextView = (TextView) findViewById(R.id.movie_description_textview);
        // TODO No camel case in view ids — "rating_movie" will be ok
        ratingBar = (RatingBar) findViewById(R.id.movie_ratingBar);
    }

    private void fillFields() {
        // TODO It will be better to get movie by id and then work with it and not with DataHolder itself
        nameTextView.setText(DataHolder.getInstance().getMovieObject(position).getName());
        yearTextView.setText(DataHolder.getInstance().getMovieObject(position).getYear());
        descriptionTextView.setText(DataHolder.getInstance().getMovieObject(position).getDescription());
        float rating = isRatingNull() ? 0 : Float.parseFloat(DataHolder.getInstance().getMovieObject(position).getRating());
        ratingBar.setRating(rating);
    }

    private boolean isRatingNull() {
        return DataHolder.getInstance().getMovieObject(position).getRating().equals("null"); //достаточно такой проверки?!
    }

}