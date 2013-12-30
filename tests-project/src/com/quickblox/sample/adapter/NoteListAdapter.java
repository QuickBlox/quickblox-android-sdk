package com.quickblox.sample.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.quickblox.sample.helper.DataHolder;


/**
 * Created with IntelliJ IDEA.
 * User: android
 * Date: 30.11.12
 * Time: 16:22
 */
public class NoteListAdapter extends BaseAdapter {

    LayoutInflater inflater;

    public NoteListAdapter(Context ctx) {
        this.inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
       /* if (convertView == null) {
            convertView = inflater.inflate(R.layout.note_list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.title = (TextView) convertView.findViewById(R.id.note);
            viewHolder.date = (TextView) convertView.findViewById(R.id.date);
            viewHolder.status = (TextView) convertView.findViewById(R.id.status);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        applyTitle(viewHolder.title, position);
        applyStatus(viewHolder.status, position);
        applyDate(viewHolder.date, position);*/

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

    static class ViewHolder {
        TextView title;
        TextView status;
        TextView date;
    }
}
