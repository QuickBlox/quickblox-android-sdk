package com.quickblox.sample.content.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.sample.content.helper.DataHolder;
import com.quickblox.sample.content.utils.Consts;
import com.quickblox.sample.core.utils.Toaster;

import java.util.ArrayList;
import java.util.List;

public class GetFileListActivity extends BaseActivity {

    public static void start(Context context) {
        Intent intent = new Intent(context, GetFileListActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFileList();
    }

    private void getFileList() {
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();

        QBPagedRequestBuilder builder = new QBPagedRequestBuilder();
        builder.setPerPage(Consts.QB_PER_PAGE);
        builder.setPage(Consts.QB_PAGE);

        QBContent.getFiles(builder, new QBEntityCallbackImpl<ArrayList<QBFile>>() {
            @Override
            public void onSuccess(ArrayList<QBFile> qbFiles, Bundle bundle) {
                DataHolder.getInstance().setQbFileList(qbFiles);
                GalleryActivity.start(GetFileListActivity.this);
                finish();
            }

            @Override
            public void onError(List<String> errors) {
                progressDialog.dismiss();
                Toaster.shortToast(errors.get(0));
            }
        });
    }
}
