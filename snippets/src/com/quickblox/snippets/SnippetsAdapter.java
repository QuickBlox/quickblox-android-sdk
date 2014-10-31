package com.quickblox.snippets;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * User: Oleg Soroka
 * Date: 02.10.12
 * Time: 11:02
 */
public class SnippetsAdapter extends BaseAdapter {

    Context context;
    Snippets snippets;
    LayoutInflater inflater;

    public SnippetsAdapter(Context context, Snippets snippets) {
        this.context = context;
        this.snippets = snippets;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return snippets.getSnippets().size();
    }

    @Override
    public Object getItem(int i) {
        return snippets.getSnippets().get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public Snippet getSnippet(int index) {
        return snippets.getSnippets().get(index);
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        final Snippet snippet = (Snippet) getItem(i);
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(android.R.layout.simple_list_item_2, null);
            viewHolder = new ViewHolder();
            viewHolder.snippetTitle = (TextView) convertView.findViewById(android.R.id.text1);
            viewHolder.snippetSubTitle = (TextView) convertView.findViewById(android.R.id.text2);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.snippetTitle.setText(snippet.getTitle());
        if (snippet.getSubtitle() != null) {
            viewHolder.snippetSubTitle.setText(snippet.getSubtitle());
        } else {
            viewHolder.snippetSubTitle.setText("");
        }
        return convertView;
    }

    static class ViewHolder {
        TextView snippetTitle;
        TextView snippetSubTitle;
    }

}