package com.quickblox.ratings.main.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.quickblox.ratings.main.R;
import com.quickblox.ratings.main.core.DataHolder;

public class MoviesListAdapter extends BaseAdapter {

    private final int STARS_NUMBER = 5;
    private LayoutInflater inflater;
    private Context context;

    public MoviesListAdapter(Context context) {
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return DataHolder.getDataHolder().getMovieListSize();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_movie, null);
            viewHolder = new ViewHolder();
            viewHolder.movieCover = (ImageView) convertView.findViewById(R.id.movie_cover_imageview);
            viewHolder.movieName = (TextView) convertView.findViewById(R.id.movie_name);
//            viewHolder.startsLl = (LinearLayout) convertView.findViewById(R.id.starts_linearlayout);
            viewHolder.ratingBar = (RatingBar) convertView.findViewById(R.id.rating_bar);
            viewHolder.movieRating = (TextView) convertView.findViewById(R.id.movie_rating);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        applyMovieCover(viewHolder, position);
        applyMovieName(viewHolder, position);
        applyStartsLl(viewHolder, position);
        applyRating(viewHolder, position);

        return convertView;
    }

    private void applyStartsLl(ViewHolder viewHolder, int position) {
        double movieRating = DataHolder.getDataHolder().getMovieRating(position);
        viewHolder.ratingBar.setNumStars(STARS_NUMBER);
        viewHolder.ratingBar.setRating((float) movieRating);
    }

    private void applyMovieCover(ViewHolder viewHolder, int position) {
        viewHolder.movieCover.setImageDrawable(DataHolder.getDataHolder().getMovieCover(position));
    }

    private void applyMovieName(ViewHolder viewHolder, int position) {
        viewHolder.movieName.setText(DataHolder.getDataHolder().getMovieName(position));
    }

    private void applyRating(ViewHolder viewHolder, int position) {
        if (DataHolder.getDataHolder().getMovieRating(position) != 0) {
            viewHolder.movieRating.setVisibility(View.VISIBLE);
            viewHolder.movieRating.setText(String.valueOf(DataHolder.getDataHolder().getMovieRating(
                    position)));
        }
    }

    public static class ViewHolder {

        ImageView movieCover;
        TextView movieName;
        TextView movieRating;
        RatingBar ratingBar;
        LinearLayout startsLl;
    }
}