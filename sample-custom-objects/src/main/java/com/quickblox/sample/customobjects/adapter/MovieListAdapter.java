package com.quickblox.sample.customobjects.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.quickblox.sample.customobjects.R;
import com.quickblox.sample.customobjects.helper.DataHolder;

public class MovieListAdapter extends BaseAdapter {

    private LayoutInflater inflater;

    public MovieListAdapter(Context context) {
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return DataHolder.getDataHolder().getMovieListSize();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_movie, null);
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

    private void applyName(TextView name, int position) {
        name.setText(DataHolder.getDataHolder().getMovieName(position));
    }

    private void applyComment(TextView comment, int position) {
        comment.setText(DataHolder.getDataHolder().getMovieDescription(position));
    }


    private static class ViewHolder {
        public TextView titleTextView;
        public TextView descriptionTextView;
    }
}
