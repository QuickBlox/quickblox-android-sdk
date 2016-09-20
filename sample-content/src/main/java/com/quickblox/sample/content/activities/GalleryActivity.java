package com.quickblox.sample.content.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import com.quickblox.sample.content.helper.DownloadMoreListener;
import com.quickblox.sample.core.utils.DialogUtils;
import com.quickblox.sample.core.utils.ErrorUtils;
import com.quickblox.sample.core.utils.Toaster;
import com.quickblox.sample.core.utils.imagepick.ImagePickHelper;
import com.quickblox.sample.core.utils.imagepick.OnImagePickedListener;

import java.io.File;
import java.util.ArrayList;

public class GalleryActivity extends BaseActivity
        implements AdapterView.OnItemClickListener, OnImagePickedListener, DownloadMoreListener {

    public static final int GALLERY_REQUEST_CODE = 183;

    private static final int IMAGE_SIZE_LIMIT_KB = 1024 * 100;
    private static final int IMAGES_PER_PAGE = 50;
    private int current_page = 1;

    private GalleryAdapter galleryAdapter;
    private ImagePickHelper imagePickHelper;
    private LinearLayout emptyView;
    private TextView problemView;
    private TextView descriptionView;

    public static void start(Context context) {
        Intent intent = new Intent(context, GalleryActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        DataHolder.getInstance().clear();
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
        galleryAdapter.setDownloadMoreListener(this);

        GridView galleryGridView = _findViewById(R.id.gallery_gridview);
        emptyView = _findViewById(R.id.empty_view);
        problemView = _findViewById(R.id.problem);
        descriptionView = _findViewById(R.id.description);

        galleryGridView.setAdapter(galleryAdapter);
        galleryGridView.setOnItemClickListener(this);
    }

    private void getFileList() {
        progressDialog = DialogUtils.getProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();

        QBPagedRequestBuilder builder = new QBPagedRequestBuilder();
        builder.setPerPage(IMAGES_PER_PAGE);
        builder.setPage(current_page++);

        QBContent.getFiles(builder).performAsync(new QBEntityCallback<ArrayList<QBFile>>() {
            @Override
            public void onSuccess(ArrayList<QBFile> qbFiles, Bundle bundle) {
                if (qbFiles.isEmpty()) {
                    current_page--;
                } else {
                    DataHolder.getInstance().addQbFiles(qbFiles);
                }
                if (progressDialog.isIndeterminate()) {
                    progressDialog.dismiss();
                }
                updateData();
            }

            @Override
            public void onError(QBResponseException e) {
                progressDialog.dismiss();
                current_page--;
                View view = findViewById(R.id.activity_gallery);
                showSnackbarError(view, R.string.splash_create_session_error, e, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getFileList();
                    }
                });
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

    private void noPhoto() {
        problemView.setText(getResources().getString(R.string.no_photo));
        descriptionView.setText(getResources().getString(R.string.press_button));
        descriptionView.setTextColor(ContextCompat.getColor(this, R.color.text_color_light_grey));
        emptyView.setVisibility(View.VISIBLE);
    }

    private void uploadSelectedImage(final File imageFile) {
        final int imageSizeKb = (int) imageFile.length() / 1024;
        final float onePercent = (float) imageSizeKb / 100;
        if (imageSizeKb >= IMAGE_SIZE_LIMIT_KB) {
            Toaster.longToast(R.string.image_size_error);
            return;
        }

        progressDialog.dismiss();
        progressDialog = DialogUtils.getProgressDialog(this);
        progressDialog.setMax(imageSizeKb);
        progressDialog.setProgressNumberFormat("%1d/%2d kB");
        progressDialog.show();

        QBContent.uploadFileTask(imageFile, true, null, new QBProgressCallback() {
            @Override
            public void onProgressUpdate(int progress) {
                progressDialog.setProgress((int) (onePercent * progress));
            }
        }).performAsync(new QBEntityCallback<QBFile>() {
            @Override
            public void onSuccess(QBFile qbFile, Bundle bundle) {
                DataHolder.getInstance().addQbFile(qbFile);
                progressDialog.dismiss();
                updateData();
            }

            @Override
            public void onError(QBResponseException e) {
                progressDialog.dismiss();
                View view = findViewById(R.id.activity_gallery);
                showSnackbarError(view, R.string.splash_create_session_error, e, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        uploadSelectedImage(imageFile);
                    }
                });
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

    @Override
    public void downloadMore() {
        getFileList();
    }
}