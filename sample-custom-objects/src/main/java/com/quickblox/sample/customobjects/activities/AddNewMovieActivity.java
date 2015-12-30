package com.quickblox.sample.customobjects.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.RatingBar;

import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.customobjects.QBCustomObjects;
import com.quickblox.customobjects.model.QBCustomObject;
import com.quickblox.sample.core.utils.Toaster;
import com.quickblox.sample.customobjects.R;
import com.quickblox.sample.customobjects.definition.Consts;
import com.quickblox.sample.customobjects.helper.DataHolder;

import java.util.HashMap;
import java.util.List;

public class AddNewMovieActivity extends BaseActivity {

    private EditText titleEditText;
    private EditText descriptionEditText;
    private EditText yearEditText;
    private RatingBar ratingBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_movie);
        initUI();
    }

    private void initUI() {
        actionBar.setDisplayHomeAsUpEnabled(true);

        titleEditText = _findViewById(R.id.add_movie_name_textview);
        descriptionEditText = _findViewById(R.id.add_movie_description_textview);
        yearEditText = _findViewById(R.id.add_movie_year_textview);
        ratingBar = _findViewById(R.id.add_movie_ratingBar);
    }

    // TODO Method code should start right after method signature, without empty line
    private void createNewMovie() {

        String title = titleEditText.getText().toString();
        String description = descriptionEditText.getText().toString();
        String year = yearEditText.getText().toString();
        float rating = ratingBar.getRating();

        // TODO Comments should be in English, and above the code
        if (!isValidData(title, description, year)) {                      // нужна ли эта проверка?
            Toaster.longToast(R.string.error_fields_is_empty);
            return;
        }

        progressDialog.show();

        // TODO Move this to QbCustomObjectUtils as well
        HashMap<String, Object> fields = new HashMap<String, Object>();
        fields.put(Consts.NAME, title);
        fields.put(Consts.DESCRIPTION, description);
        fields.put(Consts.YEAR, year);
        fields.put(Consts.RATING, rating);

        QBCustomObject qbCustomObject = new QBCustomObject();
        qbCustomObject.setClassName(Consts.CLASS_NAME);
        qbCustomObject.setFields(fields);
        // TODO

        QBCustomObjects.createObject(qbCustomObject, new QBEntityCallbackImpl<QBCustomObject>() {
            @Override
            public void onSuccess(QBCustomObject qbCustomObject, Bundle bundle) {
                progressDialog.dismiss();

                DataHolder.getInstance().addMovieToList(qbCustomObject);
                finish();
            }

            @Override
            public void onError(List<String> errors) {
                progressDialog.dismiss();
                Toaster.longToast(errors.get(0));
            }
        });
    }

    private boolean isValidData(String title, String description, String year) {
        // TODO TextUtils.isEmpty is better to use, as it checks for null as well
        return (!title.isEmpty() && !description.isEmpty() && !year.isEmpty());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.save_movie_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // TODO All string to be shown to user should be in strings.xml
                Toaster.shortToast("Save");
                createNewMovie();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}