package com.quickblox.sample.customobjects.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.RatingBar;

import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.customobjects.QBCustomObjects;
import com.quickblox.customobjects.model.QBCustomObject;
import com.quickblox.sample.core.utils.Toaster;
import com.quickblox.sample.customobjects.R;
import com.quickblox.sample.customobjects.helper.DataHolder;
import com.quickblox.sample.customobjects.utils.QBCustomObjectsUtils;

public class AddNewMovieActivity extends BaseActivity {

    private EditText titleEditText;
    private EditText descriptionEditText;
    private EditText yearEditText;
    private RatingBar ratingBar;

    public static void start(Context context) {
        Intent intent = new Intent(context, AddNewMovieActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_movie);
        initUI();
    }

    private void initUI() {
        actionBar.setDisplayHomeAsUpEnabled(true);

        titleEditText = _findViewById(R.id.add_movie_title_textview);
        descriptionEditText = _findViewById(R.id.add_movie_description_textview);
        yearEditText = _findViewById(R.id.add_movie_year_textview);
        ratingBar = _findViewById(R.id.add_movie_ratingBar);
    }

    private void createNewMovie() {
        String title = titleEditText.getText().toString();
        String description = descriptionEditText.getText().toString();
        String year = yearEditText.getText().toString();
        float rating = ratingBar.getRating();

        if (!isValidData(title, description, year, rating)) {
            return;
        }
        progressDialog.show();

        QBCustomObject qbCustomObject = QBCustomObjectsUtils.createCustomObject(title, description, year, rating);

        QBCustomObjects.createObject(qbCustomObject, new QBEntityCallback<QBCustomObject>() {
            @Override
            public void onSuccess(QBCustomObject qbCustomObject, Bundle bundle) {
                progressDialog.dismiss();
                Toaster.shortToast(R.string.done);
                DataHolder.getInstance().addMovieToList(qbCustomObject);
                finish();
            }

            @Override
            public void onError(QBResponseException e) {
                progressDialog.dismiss();
                Toaster.longToast(e.getErrors().toString());
            }
        });
    }

    private boolean isValidData(String title, String description, String year, float rating) {
        String space = Character.toString((char) 0x20);

        if (title.startsWith(space) || description.startsWith(space)
                || TextUtils.isEmpty(title) || TextUtils.isEmpty(description) || TextUtils.isEmpty(year)) {
            Toaster.longToast(R.string.error_fields_is_empty);
            return false;
        }
        if (rating == 0) {
            Toaster.longToast(R.string.error_rating_is_empty);
            return false;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_add_movie, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_save_movie:
                createNewMovie();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}