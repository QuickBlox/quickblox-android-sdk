package com.quickblox.sample.customobjects.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.quickblox.sample.customobjects.R;
import com.quickblox.sample.customobjects.model.Movie;

import java.util.ArrayList;
import java.util.List;

public class MovieListAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private List<Movie> movieList;

    public MovieListAdapter(Context context, List<Movie> movieList) {
        this.movieList = movieList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return getMovieListSize();
    }

    @Override
    public Object getItem(int position) {
        return movieList.get(position);
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
        applyName(holder.titleTextView, position);
        applyComment(holder.descriptionTextView, position);

        return convertView;
    }

    // TODO If there is only one line for method it's not necessarily to create separate method
    // Also it will be better to pass as argument Movie object itself
    private void applyName(TextView name, int position) {
        // TODO Replace movieList.get(position) with getItem(position), adapter should encapsulate its data set
        name.setText(movieList.get(position).getName());
    }

    // TODO The same :)
    private void applyComment(TextView comment, int position) {
        comment.setText(movieList.get(position).getDescription());
    }

    public int getMovieListSize() {
        // TODO After creating DataHolder constructor movieList never will be null
        // So we can skip this check
        if (movieList == null) {
            movieList = new ArrayList<>();
        }
        return movieList.size();
    }


    private static class ViewHolder {
        public TextView titleTextView;
        public TextView descriptionTextView;
    }
}
