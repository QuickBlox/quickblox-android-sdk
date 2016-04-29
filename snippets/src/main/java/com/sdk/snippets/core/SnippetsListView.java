package com.sdk.snippets.core;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class SnippetsListView extends ListView {

    public void initialize(Snippets snippets) {
        final SnippetsAdapter snippetsAdapter = new SnippetsAdapter(getContext(), snippets);

        setAdapter(snippetsAdapter);

        setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Snippet snippet = snippetsAdapter.getSnippet(i);
                snippet.performExecution();
            }
        });
    }

    public SnippetsListView(Context context) {
        super(context);
    }

    public SnippetsListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SnippetsListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    class SnippetsAdapter extends BaseAdapter {

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

        class ViewHolder {
            TextView snippetTitle;
            TextView snippetSubTitle;
        }

    }
}