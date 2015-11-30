package com.quickblox.sample.customobjects.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.quickblox.sample.customobjects.R;
import com.quickblox.sample.customobjects.helper.DataHolder;

public class NoteListAdapter extends BaseAdapter {

    private LayoutInflater layoutInflater;

    public NoteListAdapter(Context context) {
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return DataHolder.getDataHolder().getNoteListSize();
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
            convertView = layoutInflater.inflate(R.layout.list_item_note, null);
            viewHolder = new ViewHolder();
            viewHolder.titleTextView = (TextView) convertView.findViewById(R.id.note_textview);
            viewHolder.dateTextView = (TextView) convertView.findViewById(R.id.date_textview);
            viewHolder.statusTextView = (TextView) convertView.findViewById(R.id.status_textview);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        applyTitle(viewHolder.titleTextView, position);
        applyStatus(viewHolder.statusTextView, position);
        applyDate(viewHolder.dateTextView, position);

        return convertView;
    }

    private void applyTitle(TextView title, int position) {
        title.setText(DataHolder.getDataHolder().getNoteTitle(position));
    }

    private void applyStatus(TextView status, int position) {
        status.setText(DataHolder.getDataHolder().getNoteStatus(position));
    }

    private void applyDate(TextView date, int position) {
        date.setText(DataHolder.getDataHolder().getNoteDate(position));
    }

    private static class ViewHolder {

        TextView titleTextView;
        TextView statusTextView;
        TextView dateTextView;
    }
}