package com.quickblox.sample.customobjects.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.view.View;
import android.view.ViewGroup;

import com.quickblox.sample.core.ui.adapter.BaseListAdapter;
import com.quickblox.sample.customobjects.R;
import com.quickblox.sample.customobjects.databinding.ListItemMovieBinding;
import com.quickblox.sample.customobjects.model.Movie;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class MovieListAdapter extends BaseListAdapter<Movie> {

    public MovieListAdapter(Context context, Map<String, Movie> movieMap) {
        super(context, new ArrayList<>(movieMap.values()));
        updateData(movieMap);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_movie, parent, false);
            holder = new ViewHolder(convertView);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Movie movie = (Movie) getItem(position);
        holder.movieBinding.setMovie(movie);

        return convertView;
    }

    public void updateData(Map<String, Movie> movieMap) {
        objectsList = new ArrayList<>(movieMap.values());
        Collections.sort(objectsList, new Movie.DateComparator());
        notifyDataSetChanged();
    }

    private static class ViewHolder {
        ListItemMovieBinding movieBinding;

        public ViewHolder(View v) {
            movieBinding = DataBindingUtil.bind(v);
        }
    }
}