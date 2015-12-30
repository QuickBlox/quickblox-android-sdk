package com.quickblox.sample.customobjects.activities;

import android.os.Bundle;
import android.widget.RatingBar;
import android.widget.TextView;

import com.quickblox.sample.customobjects.R;
import com.quickblox.sample.customobjects.helper.DataHolder;

import java.text.NumberFormat;
import java.text.ParsePosition;

public class ShowMovieActivity extends BaseActivity {

    // TODO This constant should be in one place, if used in multiple activities
    // If we'll change its value in one place we will get wrong app behavior
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

        // TODO This has nothing to do with UI
        position = getIntent().getIntExtra(EXTRA_POSITION, 0);
        // TODO "text_movie_name" etc
        nameTextView = (TextView) findViewById(R.id.movie_name_textview);
        yearTextView = (TextView) findViewById(R.id.movie_year_textview);
        descriptionTextView = (TextView) findViewById(R.id.movie_description_textview);
        // TODO No camel case in view ids — "rating_movie" will be ok
        ratingBar = (RatingBar) findViewById(R.id.movie_ratingBar);
    }

    private void fillFields() {
        // TODO It will be better to get movie by id and then work with it and not with DataHolder itself
        nameTextView.setText(DataHolder.getInstance().getMovieName(position));
        yearTextView.setText(DataHolder.getInstance().getMovieYear(position));
        descriptionTextView.setText(DataHolder.getInstance().getMovieDescription(position));
        float rating = isRatingNull() ? 0 : Float.parseFloat(DataHolder.getInstance().getMovieRating(position));
        ratingBar.setRating(rating);
    }

    private boolean isRatingNull() {
        return DataHolder.getInstance().getMovieRating(position).equals("null"); //достаточно такой проверки?!
    }

    private boolean isNumeric() {
        String rating = DataHolder.getInstance().getMovieRating(position);// или надо такую
        NumberFormat formatter = NumberFormat.getInstance();
        ParsePosition pos = new ParsePosition(0);
        formatter.parse(rating, pos);
        return rating.length() == pos.getIndex();
    }
}