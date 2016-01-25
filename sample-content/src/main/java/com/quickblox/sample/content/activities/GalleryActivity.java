package com.quickblox.sample.content.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.QBProgressCallback;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.sample.content.R;
import com.quickblox.sample.content.adapter.GalleryAdapter;
import com.quickblox.sample.content.helper.DataHolder;
import com.quickblox.sample.content.utils.Consts;
import com.quickblox.sample.core.utils.DialogUtils;
import com.quickblox.sample.core.utils.ErrorUtils;
import com.quickblox.sample.core.utils.Toaster;
import com.quickblox.sample.core.utils.imagepick.ImagePickHelper;
import com.quickblox.sample.core.utils.imagepick.OnImagePickedListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends BaseActivity implements AdapterView.OnItemClickListener, OnImagePickedListener {

    public static final int GALLERY_REQUEST_CODE = 183;

    private GridView galleryGridView;
    private GalleryAdapter galleryAdapter;
    private ImageView selectedImageView;
    private ImagePickHelper imagePickHelper;

    public static void start(Context context) {
        Intent intent = new Intent(context, GalleryActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        initUI();
        getFileList();
        imagePickHelper = new ImagePickHelper();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        QBFile qbFile = (QBFile) adapterView.getItemAtPosition(position);
        ShowImageActivity.start(this, qbFile.getId());
    }

    public void onStartUploadImageClick(View view) {
        imagePickHelper.pickAnImage(this, GALLERY_REQUEST_CODE);
    }

    private void initUI() {
        galleryGridView = _findViewById(R.id.gallery_gridview);
        selectedImageView = _findViewById(R.id.image_uploading_view);

        galleryAdapter = new GalleryAdapter(this, DataHolder.getInstance().getQBFileSparseArray());
        galleryGridView.setAdapter(galleryAdapter);
        galleryGridView.setOnItemClickListener(this);
    }

    private void getFileList() {
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();

        QBPagedRequestBuilder builder = new QBPagedRequestBuilder();
        builder.setPerPage(Consts.IMAGES_PER_PAGE);
        builder.setPage(Consts.START_PAGE);

        QBContent.getFiles(builder, new QBEntityCallbackImpl<ArrayList<QBFile>>() {
            @Override
            public void onSuccess(ArrayList<QBFile> qbFiles, Bundle bundle) {
                SparseArray<QBFile> qbFileSparseArr = DataHolder.getInstance().getQBFileSparseArray();
                if (qbFileSparseArr.size() > 0) {
                    DataHolder.getInstance().clear();
                }
                DataHolder.getInstance().addQbFiles(qbFiles);
                progressDialog.dismiss();
                galleryAdapter.updateData(qbFileSparseArr);
            }

            @Override
            public void onError(List<String> errors) {
                progressDialog.dismiss();
                Toaster.shortToast(errors.get(0));
            }
        });
    }

    private void uploadSelectedImage(File imageFile) {
        progressDialog = DialogUtils.getProgressDialog(this);
        progressDialog.show();
        QBContent.uploadFileTask(imageFile, true, null, new QBEntityCallbackImpl<QBFile>() {
            @Override
            public void onSuccess(QBFile qbFile, Bundle bundle) {
                DataHolder.getInstance().addQbFile(qbFile);
                selectedImageView.setVisibility(View.GONE);
                progressDialog.dismiss();
            }

            @Override
            public void onError(List<String> errors) {
                selectedImageView.setVisibility(View.GONE);
                progressDialog.dismiss();
                Toaster.shortToast(R.string.gallery_upload_file_error + errors.get(0));
            }
        }, new QBProgressCallback() {
            @Override
            public void onProgressUpdate(int progress) {
                progressDialog.setProgress(progress);
            }
        });
    }

    @Override
    public void onImagePicked(int requestCode, File file) {
        selectedImageView.setVisibility(View.VISIBLE);
        Glide.with(this)
                .load(file)
                .into(selectedImageView);

        uploadSelectedImage(file);
    }

    @Override
    public void onImagePickError(int requestCode, Exception e) {
        ErrorUtils.showErrorDialog(this, R.string.gallery_pick_error, e.getMessage());
    }

    @Override
    public void onImagePickClosed(int requestCode) {
//ignored
    }
}