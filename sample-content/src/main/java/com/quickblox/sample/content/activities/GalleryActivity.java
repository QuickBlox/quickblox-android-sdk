package com.quickblox.sample.content.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBProgressCallback;
import com.quickblox.core.exception.QBResponseException;
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

public class GalleryActivity extends BaseActivity
        implements AdapterView.OnItemClickListener, OnImagePickedListener {

    public static final int GALLERY_REQUEST_CODE = 183;

    private GalleryAdapter galleryAdapter;
    private ImagePickHelper imagePickHelper;
    private LinearLayout emptyView;
    private TextView problemView;
    private TextView descriptionView;
    private FloatingActionButton FAB;

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
        galleryAdapter = new GalleryAdapter(this, DataHolder.getInstance().getQBFiles());

        GridView galleryGridView = _findViewById(R.id.gallery_gridview);
        emptyView = _findViewById(R.id.empty_view);
        problemView = _findViewById(R.id.problem);
        descriptionView = _findViewById(R.id.description);
        FAB = _findViewById(R.id.fab_upload_image);

        galleryGridView.setAdapter(galleryAdapter);
        galleryGridView.setOnItemClickListener(this);
    }

    private void getFileList() {
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();

        QBPagedRequestBuilder builder = new QBPagedRequestBuilder();
        builder.setPerPage(Consts.IMAGES_PER_PAGE);
        builder.setPage(Consts.START_PAGE);

        QBContent.getFiles(builder, new QBEntityCallback<ArrayList<QBFile>>() {
            @Override
            public void onSuccess(ArrayList<QBFile> qbFiles, Bundle bundle) {
                if (!DataHolder.getInstance().isEmpty()) {
                    DataHolder.getInstance().clear();
                }

                DataHolder.getInstance().addQbFiles(qbFiles);
                if (progressDialog.isIndeterminate()) {
                    progressDialog.dismiss();
                }
                updateData();
            }

            @Override
            public void onError(QBResponseException e) {
                progressDialog.dismiss();
                Toaster.shortToast(e.getErrors().toString());
            }
        });
    }

    private void updateData() {
        if (DataHolder.getInstance().isEmpty()) {
            noPhoto();
        } else {
            emptyView.setVisibility(View.GONE);
        }

        galleryAdapter.updateData(DataHolder.getInstance().getQBFiles());
    }

    private void noConnection() {
        problemView.setText(getResources().getString(R.string.problem));
        descriptionView.setText(getResources().getString(R.string.no_connection));
        descriptionView.setTextColor(ContextCompat.getColor(this, R.color.red));
        FAB.setClickable(false);
        FAB.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.green_light)));
        emptyView.setVisibility(View.VISIBLE);
//        Runnable myRunnable = new Runnable() {
//            public void run() {
//                getFileList();
//            }
//        };
//        Handler handler = new Handler();
//        handler.postDelayed(myRunnable, 30000);
//        TODO remove handler when close app and fix trouble with progressDialog when reconnection
//        handler.removeCallbacks(myRunnable);
    }

    private void existConnection() {
        FAB.setClickable(true);
        FAB.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.color_green_qb)));
        emptyView.setVisibility(View.GONE);
    }

    private void noPhoto() {
        problemView.setText(getResources().getString(R.string.no_photo));
        descriptionView.setText(getResources().getString(R.string.press_button));
        descriptionView.setTextColor(ContextCompat.getColor(this, R.color.text_color_light_grey));
        emptyView.setVisibility(View.VISIBLE);
    }

    private void uploadSelectedImage(File imageFile) {
        final int imageSize = (int) imageFile.length() / 1024;
        final float onePercent = (float) imageSize / 100;

        progressDialog.dismiss();
        progressDialog = DialogUtils.getProgressDialog(this);
        progressDialog.setMax(imageSize);
        progressDialog.setProgressNumberFormat("%1d/%2d kB");
        progressDialog.show();
        QBContent.uploadFileTask(imageFile, true, null, new QBEntityCallback<QBFile>() {
            @Override
            public void onSuccess(QBFile qbFile, Bundle bundle) {
                DataHolder.getInstance().addQbFile(qbFile);
                progressDialog.dismiss();
                updateData();
            }

            @Override
            public void onError(QBResponseException e) {
                progressDialog.dismiss();
                Toaster.shortToast(getString(R.string.gallery_upload_file_error) + e.getErrors());
            }
        }, new QBProgressCallback() {
            @Override
            public void onProgressUpdate(int progress) {
                progressDialog.setProgress((int) (onePercent * progress));
            }
        });
    }

    @Override
    public void onImagePicked(int requestCode, File file) {
        uploadSelectedImage(file);
    }

    @Override
    public void onImagePickError(int requestCode, Exception e) {
        ErrorUtils.showErrorDialog(this, R.string.gallery_pick_error, e.getMessage());
    }

    @Override
    public void onImagePickClosed(int requestCode) {
        // ignored
    }
}