package com.quickblox.sample.customobjects.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.quickblox.sample.customobjects.R;
import com.quickblox.sample.customobjects.model.Movie;

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
        return movieList.size();
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
            // TODO Add singleLine attribute to description, to avoid
            holder.descriptionTextView = (TextView) convertView.findViewById(R.id.text_description);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // TODO Get movie as variable, instead of casting it everytime
        holder.titleTextView.setText(((Movie) getItem(position)).getName());
        holder.descriptionTextView.setText(((Movie) getItem(position)).getDescription());

        return convertView;
    }

    // TODO One blank line between method and class declaration will be enough
    private static class ViewHolder {
        public TextView titleTextView;
        public TextView descriptionTextView;
    }
}
