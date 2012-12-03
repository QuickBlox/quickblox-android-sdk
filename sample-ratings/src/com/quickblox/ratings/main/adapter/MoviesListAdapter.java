package com.quickblox.ratings.main.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.quickblox.ratings.main.R;
import com.quickblox.ratings.main.core.DataHolder;

/**
 * Created with IntelliJ IDEA.
 * User: android
 * Date: 27.11.12
 * Time: 16:34
 * To change this template use File | Settings | File Templates.
 */
public class MoviesListAdapter extends BaseAdapter {

    LayoutInflater inflater;
    final double STARS_NUMBER = 10;
    Context ctx;

    public MoviesListAdapter(Context ctx) {
        this.ctx = ctx;
        inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
            convertView = inflater.inflate(R.layout.movies_list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.movieCover = (ImageView) convertView.findViewById(R.id.movie_cover);
            viewHolder.movieName = (TextView) convertView.findViewById(R.id.movie_name);
            viewHolder.startsLl = (LinearLayout) convertView.findViewById(R.id.starts_ll);
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
        viewHolder.startsLl.removeAllViews();
        if (movieRating != 0) {
            for (int i = 0; i < STARS_NUMBER; ++i) {
                ImageView star = (ImageView) inflater.inflate(R.layout.image_holder, null);
                if (i < movieRating) {
                    star.setImageDrawable(ctx.getResources().getDrawable(R.drawable.star_full));
                } else {
                    star.setImageDrawable(ctx.getResources().getDrawable(R.drawable.star_empty));
                }
                viewHolder.startsLl.addView(star);
            }
        }
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
            viewHolder.movieRating.setText(String.valueOf(DataHolder.getDataHolder().getMovieRating(position)));
        }
    }

    public static class ViewHolder {
        ImageView movieCover;
        TextView movieName;
        TextView movieRating;
        LinearLayout startsLl;
    }


}
