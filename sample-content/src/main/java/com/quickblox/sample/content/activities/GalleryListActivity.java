package com.quickblox.sample.content.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.QBProgressCallback;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.sample.content.R;
import com.quickblox.sample.content.adapter.GalleryAdapter;
import com.quickblox.sample.content.helper.DataHolder;
import com.quickblox.sample.content.utils.Consts;
import com.quickblox.sample.content.utils.GetImageFileTask;
import com.quickblox.sample.content.utils.ImageHelper;
import com.quickblox.sample.content.utils.OnGetImageFileListener;
import com.quickblox.sample.core.utils.DialogUtils;
import com.quickblox.sample.core.utils.ErrorUtils;
import com.quickblox.sample.core.utils.Toaster;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GalleryListActivity extends BaseActivity implements AdapterView.OnItemClickListener, OnGetImageFileListener {

    private GridView galleryGridView;
    private GalleryAdapter galleryAdapter;
    private ImageHelper imageHelper;
    private ImageView selectedImageView;

    public static void start(Context context) {
        Intent intent = new Intent(context, GalleryListActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        initUI();
        initGalleryView();
        getFileList();

        imageHelper = new ImageHelper(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        QBFile qbFile = (QBFile) adapterView.getItemAtPosition(position);
        ShowImageActivity.start(this, qbFile.getId());
    }

    public void onStartUploadImageClick(View view) {
        imageHelper.getImage();
    }

    @Override
    public void onGotImageFile(File imageFile) {
        uploadSelectedImage(imageFile);
    }

    private void initUI() {
        galleryGridView = _findViewById(R.id.gallery_gridview);


        selectedImageView = _findViewById(R.id.image_add_view);
    }

    private void initGalleryView() {
        galleryAdapter = new GalleryAdapter(this, DataHolder.getInstance().getQBFileSparseArray());
        galleryGridView.setAdapter(galleryAdapter);
        galleryGridView.setOnItemClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            getImageFile(data);
        }
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
                SparseArray<QBFile> qbFileSparseArr = DataHolder.getInstance().getQBFileSparseArray();
                if (qbFileSparseArr.size() > 0) {
                    Log.d("GalleryListActivity", "qbFileSparseArr.size() > 0");
                     DataHolder.getInstance().clear();
                }

                DataHolder.getInstance().setQbFileSparseArray(qbFiles);
                progressDialog.dismiss();
                galleryAdapter.updateAdapter(qbFileSparseArr);
            }

            @Override
            public void onError(List<String> errors) {
                progressDialog.dismiss();
                Toaster.shortToast(errors.get(0));
            }
        });
    }

    //ToDo Try to upload file more than 40 mb size
    private void getImageFile(Intent data) {
        Uri originalUri = data.getData();
        selectedImageView.setImageURI(originalUri);
        selectedImageView.setVisibility(View.VISIBLE);

        progressDialog = DialogUtils.getProgressDialog(this);
        progressDialog.setProgress(0);
        progressDialog.show();

        new GetImageFileTask(this).execute(imageHelper, selectedImageView);
    }

    private void uploadSelectedImage(File imageFile) {
        QBContent.uploadFileTask(imageFile, false, null, new QBEntityCallbackImpl<QBFile>() {
            @Override
            public void onSuccess(QBFile qbFile, Bundle bundle) {
                DataHolder.getInstance().addQbFile(qbFile);
                selectedImageView.setVisibility(View.GONE);

                progressDialog.dismiss();
            }

            @Override
            public void onError(List<String> errors) {
                progressDialog.dismiss();

                ErrorUtils.showErrorDialog(GalleryListActivity.this, R.string.gallery_upload_file_error, errors);
            }
        }, new QBProgressCallback() {
            @Override
            public void onProgressUpdate(int progress) {
                progressDialog.setProgress(progress);
            }
        });
    }
}