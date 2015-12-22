package com.quickblox.sample.customobjects.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.quickblox.sample.customobjects.R;
import com.quickblox.sample.customobjects.adapter.MovieListAdapter;
import com.quickblox.sample.customobjects.adapter.NoteListAdapter;

public class DisplayNoteListActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    private final String POSITION = "position";
    private ListView moviesListView;
    private MovieListAdapter movieListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies_list);
        initUI();
    }

    @Override
    public void onResume() {
        super.onResume();
        movieListAdapter.notifyDataSetChanged();
    }

    private void initUI() {
        moviesListView = (ListView) findViewById(R.id.movies_listview);
        moviesListView.setOnItemClickListener(this);
        movieListAdapter = new MovieListAdapter(this);
        moviesListView.setAdapter(movieListAdapter);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add_new_note_button:
                Intent intent = new Intent(this, AddNewNoteActivity.class);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Intent intent = new Intent(this, ShowNoteActivity.class);
        intent.putExtra(POSITION, position);
        startActivity(intent);
    }
}