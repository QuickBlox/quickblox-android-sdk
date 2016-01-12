package com.quickblox.sample.customobjects.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.quickblox.sample.customobjects.R;
import com.quickblox.sample.customobjects.adapter.MovieListAdapter;
import com.quickblox.sample.customobjects.helper.DataHolder;
import com.quickblox.sample.customobjects.model.Movie;
import com.quickblox.sample.customobjects.utils.QBCustomObjectsUtils;

public class DisplayMovieListActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    private ListView moviesListView;
    private MovieListAdapter movieListAdapter;

    public static void start(Context context) {
        Intent intent = new Intent(context, DisplayMovieListActivity.class);
        context.startActivity(intent);
    }

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
        moviesListView = _findViewById(R.id.list_movies);
        moviesListView.setOnItemClickListener(this);
        movieListAdapter = new MovieListAdapter(this, DataHolder.getInstance().getMovieList());
        moviesListView.setAdapter(movieListAdapter);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Movie movie = QBCustomObjectsUtils.getMovieItem(DataHolder.getInstance().getMovieList(), position);
        ShowMovieActivity.start(this, movie.getId());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_movies_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_movie:
                AddNewMovieActivity.start(this);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}