package com.quickblox.sample.customobjects.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.server.Performer;
import com.quickblox.customobjects.QBCustomObjects;
import com.quickblox.customobjects.model.QBCustomObject;
import com.quickblox.extensions.RxJavaPerformProcessor;
import com.quickblox.sample.core.utils.Toaster;
import com.quickblox.sample.customobjects.R;
import com.quickblox.sample.customobjects.helper.DataHolder;
import com.quickblox.sample.customobjects.model.Movie;
import com.quickblox.sample.customobjects.utils.QBCustomObjectsUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class AddNewMovieActivity extends BaseActivity implements TextWatcher {
    private static final String TAG = AddNewMovieActivity.class.getSimpleName();

    private static final String OBJ = "\uFFFC";
    private EditText titleEditText;
    private EditText descriptionEditText;
    private Spinner yearSpinner;
    private RatingBar ratingBar;
    private Toast toast;
    private String title;
    private String description;
    private float rating;

    public static void start(Context context) {
        Intent intent = new Intent(context, AddNewMovieActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_movie);
        initUI();
        initSpinner();
    }

    @SuppressLint("ShowToast")
    private void initUI() {
        actionBar.setDisplayHomeAsUpEnabled(true);

        toast = Toast.makeText(this, R.string.error, Toast.LENGTH_LONG);

        titleEditText = _findViewById(R.id.add_movie_title_textview);
        titleEditText.addTextChangedListener(this);

        descriptionEditText = _findViewById(R.id.add_movie_description_textview);
        descriptionEditText.addTextChangedListener(this);

        yearSpinner = _findViewById(R.id.year_spinner);
        ratingBar = _findViewById(R.id.add_movie_ratingBar);
    }

    private void initSpinner() {
        List<String> years = new ArrayList<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int firstFilmYear = 1895;
        for (int i = currentYear + 10; i >= firstFilmYear; i--) {
            years.add(Integer.toString(i));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, years);
        yearSpinner.setAdapter(adapter);
        yearSpinner.setSelection(10);
    }

    private void createNewMovie() {
        title = titleEditText.getText().toString().trim();
        description = descriptionEditText.getText().toString().trim();
        String year = yearSpinner.getSelectedItem().toString();
        rating = ratingBar.getRating();

        if (!isValidData()) {
            return;
        }
        progressDialog.show();

        QBCustomObject qbCustomObject = QBCustomObjectsUtils.createCustomObject(title, description, year, rating);


        Performer<QBCustomObject> performer = QBCustomObjects.createObject(qbCustomObject);
        Observable<QBCustomObject> observable =
                performer.convertTo(RxJavaPerformProcessor.INSTANCE);

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<QBCustomObject>() {
            @Override
            public void onCompleted() {
                progressDialog.dismiss();
                Toaster.shortToast(R.string.done);
                finish();
            }

            @Override
            public void onError(Throwable e) {
                progressDialog.dismiss();
                if (QBCustomObjectsUtils.checkQBException(e)) {
                    View rootLayout = findViewById(R.id.activity_add_movie);
                    showSnackbarError(rootLayout, R.string.splash_create_session_error, (QBResponseException) e, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            createNewMovie();
                        }
                    });
                } else {
                    Log.d(TAG, "onError" + e.getMessage());
                }
            }

            @Override
            public void onNext(QBCustomObject qbCustomObject) {
                DataHolder.getInstance().addMovieToMap(new Movie(qbCustomObject));
            }
        });
    }

    private boolean isValidData() {
        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(description)) {
            toast.setText(R.string.error_fields_is_empty);
            toast.show();
            return false;
        }
        if (rating == 0) {
            toast.setText(R.string.error_rating_is_empty);
            toast.show();
            return false;
        }
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        toast.cancel();
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

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        //ignore
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        //ignore
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s.length() >= getResources().getInteger(R.integer.field_max_length)) {
            toast.setText(R.string.error_too_long_text);
            toast.show();
        }
        if (s.toString().contains(OBJ)) {
            int index = s.toString().indexOf(OBJ);
            s.replace(index, index + 1, "");
        }
    }
}