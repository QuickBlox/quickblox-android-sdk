package com.quickblox.ratings.main.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.quickblox.ratings.main.R;
import com.quickblox.ratings.main.core.DataHolder;

public class MoviesListAdapter extends BaseAdapter {

    private LayoutInflater layoutInflater;

    public MoviesListAdapter(Context context) {
        layoutInflater = LayoutInflater.from(context);
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
            convertView = layoutInflater.inflate(R.layout.list_item_movie, null);
            viewHolder = new ViewHolder();
            viewHolder.movieCover = (ImageView) convertView.findViewById(R.id.movie_cover_imageview);
            viewHolder.movieName = (TextView) convertView.findViewById(R.id.movie_name);
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

    private void applyMovieCover(ViewHolder viewHolder, int position) {
        viewHolder.movieCover.setImageDrawable(DataHolder.getDataHolder().getMovieCover(position));
    }

    private void applyMovieName(ViewHolder viewHolder, int position) {
        viewHolder.movieName.setText(DataHolder.getDataHolder().getMovieName(position));
    }

    private void applyStartsLl(ViewHolder viewHolder, int position) {
        double movieRating = DataHolder.getDataHolder().getMovieRating(position);
        viewHolder.ratingBar.setRating((float) movieRating);
    }

    private void applyRating(ViewHolder viewHolder, int position) {
        if (DataHolder.getDataHolder().getMovieRating(position) != 0) {
            viewHolder.movieRating.setVisibility(View.VISIBLE);
            viewHolder.movieRating.setText(String.valueOf(DataHolder.getDataHolder().getMovieRating(
                    position)));
        }
    }

    public static class ViewHolder {

        private ImageView movieCover;
        private TextView movieName;
        private TextView movieRating;
        private RatingBar ratingBar;
    }
}