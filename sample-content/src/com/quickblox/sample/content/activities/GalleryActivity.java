package com.quickblox.sample.content.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBProgressCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.sample.content.R;
import com.quickblox.sample.content.adapter.GalleryAdapter;
import com.quickblox.sample.content.helper.DataHolder;
import com.quickblox.sample.content.utils.DialogUtils;
import com.quickblox.sample.content.utils.GetImageFileTask;
import com.quickblox.sample.content.utils.ImageHelper;
import com.quickblox.sample.content.utils.OnGetImageFileListener;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;

import java.io.File;

public class GalleryActivity extends BaseActivity implements AdapterView.OnItemClickListener, OnGetImageFileListener{

    private GridView galleryGridView;
    private GalleryAdapter galleryAdapter;
    private ImageHelper imageHelper;
    private ImageView selectedImageImageView;

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add_new_image_button:
                imageHelper.getImage();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        startShowImgActivity(position);
    }

    @Override
    public void onGotImageFile(File imageFile) {
        uploadSelectedImage(imageFile);
    }

    private void startShowImgActivity(int position) {
        Intent intent = new Intent(this, ShowImageActivity.class);
        intent.putExtra(POSITION, position);
        startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        initUI();
        initGalleryView();

        imageHelper = new ImageHelper(this);
    }

    private void initUI() {
        galleryGridView = (GridView) findViewById(R.id.gallery_gridview);
        selectedImageImageView = (ImageView) findViewById(R.id.image_imageview);
    }

    private void initGalleryView() {
        galleryAdapter = new GalleryAdapter(this);
        galleryGridView.setAdapter(galleryAdapter);
        galleryGridView.setOnItemClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Uri originalUri = data.getData();
            selectedImageImageView.setImageURI(originalUri);
            selectedImageImageView.setVisibility(View.VISIBLE);
            progressDialog.setProgress(0);
            progressDialog.show();

            new GetImageFileTask(this).execute(imageHelper, selectedImageImageView);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadSelectedImage(File imageFile) {

        // Upload new file
        //
        QBContent.uploadFileTask(imageFile, false, null, new QBEntityCallback<QBFile>() {
            @Override
            public void onSuccess(QBFile qbFile, Bundle bundle) {
                DataHolder.getDataHolder().addQbFile(qbFile);
                selectedImageImageView.setVisibility(View.GONE);
                galleryAdapter.notifyDataSetChanged();

                progressDialog.hide();

                Log.d("GalleryActivity", "url: " + qbFile.getPublicUrl());
            }

            @Override
            public void onError(QBResponseException error) {
                progressDialog.hide();

                Log.d("GalleryActivity", "onError: " + error.getErrors());

                DialogUtils.show(GalleryActivity.this, error.getLocalizedMessage());
            }
        }, new QBProgressCallback() {
            @Override
            public void onProgressUpdate(int progress) {
                boolean isMain = Looper.myLooper() == Looper.getMainLooper();
                Log.d("GalleryActivity", "progress: " + progress);
                if(progress == 0){
                    progressDialog.setProgress(0);
                }else {
                    progressDialog.setProgress(progress);
                }
            }
        });
    }
}