package com.quickblox.sample.customobjects.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.customobjects.QBCustomObjects;
import com.quickblox.customobjects.model.QBCustomObject;
import com.quickblox.sample.core.utils.Toaster;
import com.quickblox.sample.customobjects.definition.Consts;
import com.quickblox.sample.customobjects.helper.DataHolder;

import java.util.ArrayList;
import java.util.List;

public class GetMovieListActivity extends BaseActivity {

    public static void start(Context context) {
        Intent intent = new Intent(context, GetMovieListActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getMovieList();
    }

    private void getMovieList() {
        // TODO There is no need to comment obvious code
        // Get all movies
        progressDialog.show();
        QBCustomObjects.getObjects(Consts.CLASS_NAME, new QBEntityCallbackImpl<ArrayList<QBCustomObject>>() {
            @Override
            public void onSuccess(ArrayList<QBCustomObject> qbCustomObjects, Bundle bundle) {
                // TODO Create method isEmpty() to avoid long statements with numbers
                if (DataHolder.getDataHolder().size() > 0) {
                    DataHolder.getDataHolder().clear();
                }

                // TODO Use isEmpty() as well
                if (qbCustomObjects != null && qbCustomObjects.size() != 0) {
                    // TODO Create method in DataHolder to put collection of custom objects,
                    // without need to create for loop in activity code
                    for (QBCustomObject customObject : qbCustomObjects) {
                        DataHolder.getDataHolder().addMovieToList(customObject);
                    }
                }
                DisplayMovieListActivity.start(GetMovieListActivity.this);
                finish();
            }

            @Override
            public void onError(List<String> errors) {
                Toaster.shortToast(errors.get(0));
                progressDialog.dismiss();
            }
        });
    }
}
