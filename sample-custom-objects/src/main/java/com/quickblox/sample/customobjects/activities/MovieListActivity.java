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

import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.customobjects.QBCustomObjects;
import com.quickblox.customobjects.model.QBCustomObject;
import com.quickblox.sample.core.utils.Toaster;
import com.quickblox.sample.customobjects.R;
import com.quickblox.sample.customobjects.adapter.MovieListAdapter;
import com.quickblox.sample.customobjects.definition.Consts;
import com.quickblox.sample.customobjects.helper.DataHolder;
import com.quickblox.sample.customobjects.model.Movie;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MovieListActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    private ListView moviesListView;
    private MovieListAdapter movieListAdapter;

    public static void start(Context context) {
        Intent intent = new Intent(context, MovieListActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies_list);
        initUI();
        getMovieList();
    }

    @Override
    public void onResume() {
        super.onResume();
        movieListAdapter.updateAdapter(DataHolder.getInstance().getMovieMap());
    }

    private void initUI() {
        moviesListView = _findViewById(R.id.list_movies);
        moviesListView.setOnItemClickListener(this);
        movieListAdapter = new MovieListAdapter(this, DataHolder.getInstance().getMovieMap());
        moviesListView.setAdapter(movieListAdapter);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Movie movie = (Movie) adapterView.getItemAtPosition(position);
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

    private void getMovieList() {
        progressDialog.show();
        QBCustomObjects.getObjects(Consts.CLASS_NAME, new QBEntityCallbackImpl<ArrayList<QBCustomObject>>() {
            @Override
            public void onSuccess(ArrayList<QBCustomObject> qbCustomObjects, Bundle bundle) {
                Map<String, Movie> movieMap = DataHolder.getInstance().getMovieMap();
                if (!movieMap.isEmpty()) {
                    DataHolder.getInstance().clear();
                }
                DataHolder.getInstance().addQBCustomObject(qbCustomObjects);
                progressDialog.dismiss();
                movieListAdapter.updateAdapter(movieMap);
            }

            @Override
            public void onError(List<String> errors) {
                Toaster.shortToast(errors.get(0));
                progressDialog.dismiss();
            }
        });
    }
}