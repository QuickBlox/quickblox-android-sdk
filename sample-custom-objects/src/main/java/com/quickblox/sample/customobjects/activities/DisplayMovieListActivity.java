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

import java.util.List;

public class DisplayMovieListActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    private final String EXTRA_POSITION = "position";
    private ListView moviesListView;
    private MovieListAdapter movieListAdapter;
    private List<Movie> movieList;

    public static void start(Context context) {
        Intent intent = new Intent(context, DisplayMovieListActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        movieList = DataHolder.getDataHolder().getMovieList();
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
        movieListAdapter = new MovieListAdapter(this, movieList);
        moviesListView.setAdapter(movieListAdapter);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Intent intent = new Intent(this, ShowMovieActivity.class);
        intent.putExtra(EXTRA_POSITION, position);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_movie_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, AddNewMovieActivity.class);
                this.startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}