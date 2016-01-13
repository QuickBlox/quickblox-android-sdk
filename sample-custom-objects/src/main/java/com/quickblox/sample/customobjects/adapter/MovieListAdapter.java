package com.quickblox.sample.customobjects.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.quickblox.sample.customobjects.R;
import com.quickblox.sample.customobjects.model.Movie;
import com.quickblox.sample.customobjects.utils.QBCustomObjectsUtils;

import java.util.Map;

public class MovieListAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private Map<String, Movie> movieList;

    public MovieListAdapter(Context context, Map<String, Movie> movieList) {
        // TODO Adapter should have List<Movie> field, which have to be set and filled with values from map in constructor
        this.movieList = movieList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return movieList.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO return movieList.get(position)
        return QBCustomObjectsUtils.getMovieItem(movieList, position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_movie, parent, false);
            holder = new ViewHolder();

            holder.titleTextView = (TextView) convertView.findViewById(R.id.text_title);
            holder.descriptionTextView = (TextView) convertView.findViewById(R.id.text_description);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Movie movie = (Movie) getItem(position);
        holder.titleTextView.setText(movie.getName());
        holder.descriptionTextView.setText(movie.getDescription());

        return convertView;
    }

    private static class ViewHolder {
        public TextView titleTextView;
        public TextView descriptionTextView;
    }
}
