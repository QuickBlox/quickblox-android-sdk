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
import com.quickblox.sample.customobjects.definition.Consts;
import com.quickblox.sample.customobjects.helper.DataHolder;
import com.quickblox.sample.customobjects.model.Movie;

import java.util.List;

public class DisplayMovieListActivity extends BaseActivity implements AdapterView.OnItemClickListener {

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
        movieList = DataHolder.getInstance().getMovieList();
        setContentView(R.layout.activity_movies_list);
        initUI();
    }

    @Override
    public void onResume() {
        super.onResume();
        movieListAdapter.notifyDataSetChanged();
    }

    private void initUI() {
        // TODO Use _findViewById method from CoreBaseActivity to avoid class cast, do this in all activities
        // TODO View ids should look like "view_view_purpose"
        // In this case it would be just "list_movies"
        moviesListView = _findViewById(R.id.movies_listview);
        moviesListView.setOnItemClickListener(this);
        // TODO We can pass DataHolder#getMovieList right to constructor, without using class field
        movieListAdapter = new MovieListAdapter(this, movieList);
        moviesListView.setAdapter(movieListAdapter);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        // TODO ShowMovieActivity should have start() method with Movie id as an argument
        // And intent should be created in the start() method
        Intent intent = new Intent(this, ShowMovieActivity.class);
        intent.putExtra(Consts.EXTRA_POSITION, position);
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
        }
        return super.onOptionsItemSelected(item);
    }
}