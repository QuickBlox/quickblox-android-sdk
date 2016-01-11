package com.quickblox.sample.content.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.QBProgressCallback;
import com.quickblox.sample.content.R;
import com.quickblox.sample.content.adapter.GalleryAdapter;
import com.quickblox.sample.content.helper.DataHolder;
import com.quickblox.sample.content.utils.GetImageFileTask;
import com.quickblox.sample.content.utils.ImageHelper;
import com.quickblox.sample.content.utils.OnGetImageFileListener;
import com.quickblox.sample.core.utils.ErrorUtils;

import java.io.File;
import java.util.List;

public class GalleryActivity extends BaseActivity implements AdapterView.OnItemClickListener, OnGetImageFileListener {

    private GridView galleryGridView;
    private GalleryAdapter galleryAdapter;
    private ImageHelper imageHelper;
    private ImageView selectedImageView;

    public static void start(Context context) {
        Intent intent = new Intent(context, GalleryActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        initUI();
        initGalleryView();

        imageHelper = new ImageHelper(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        startShowImageActivity(position);
    }

    public void onStartUploadImageClick(View view) {
        imageHelper.getImage();
    }

    @Override
    public void onGotImageFile(File imageFile) {
        uploadSelectedImage(imageFile);
    }

    private void startShowImageActivity(int position) {
        Intent intent = new Intent(this, ShowImageActivity.class);
        intent.putExtra(EXTRA_POSITION, position);
        startActivity(intent);
    }

    private void initUI() {
        galleryGridView = _findViewById(R.id.gallery_gridview);
        selectedImageView = _findViewById(R.id.image_upload_view);
    }

    private void initGalleryView() {
        galleryAdapter = new GalleryAdapter(this, DataHolder.getInstance().getQBFileList());
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

    //ToDo Try to upload file more than 40 mb size
    private void getImageFile(Intent data) {
        Uri originalUri = data.getData();
        selectedImageView.setImageURI(originalUri);
        selectedImageView.setVisibility(View.VISIBLE);

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

                ErrorUtils.showErrorDialog(GalleryActivity.this, R.string.gallery_upload_file_error, errors);
            }
        }, new QBProgressCallback() {
            @Override
            public void onProgressUpdate(int progress) {
                progressDialog.setProgress(progress);
            }
        });
    }
}