package com.quickblox.android.framework.snippets;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.quickblox.android.framework.snippets.Snippet;
import com.quickblox.android.framework.snippets.Snippets;

/**
 * User: Oleg Soroka
 * Date: 02.10.12
 * Time: 11:02
 */
public class SnippetsAdapter extends BaseAdapter {

    Context context;
    Snippets snippets;

    public SnippetsAdapter(Context context, Snippets snippets) {
        this.context = context;
        this.snippets = snippets;
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

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        final Snippet snippet = (Snippet) getItem(i);
        View v;
        if (view == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            v = inflater.inflate(android.R.layout.simple_list_item_2, null);
            TextView text1 = (TextView) v.findViewById(android.R.id.text1);
            TextView text2 = (TextView) v.findViewById(android.R.id.text2);

            text1.setText(snippet.getTitle());
            if (snippet.getSubtitle() != null) {
                text2.setText(snippet.getSubtitle());
            } else {
                text2.setText("");
            }
        } else {
            v = view;
        }

        v.setTag(snippet);

        return v;
    }
}