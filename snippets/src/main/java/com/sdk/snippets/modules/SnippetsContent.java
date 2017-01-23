package com.sdk.snippets.modules;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.content.model.QBFileObjectAccess;
import com.quickblox.core.Consts;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBProgressCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.ContentType;
import com.quickblox.core.helper.FileHelper;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.sdk.snippets.R;
import com.sdk.snippets.Utils;
import com.sdk.snippets.core.Snippet;
import com.sdk.snippets.core.SnippetAsync;
import com.sdk.snippets.core.Snippets;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by vfite on 04.02.14.
 */
public class SnippetsContent extends Snippets {
    private static final String TAG = SnippetsContent.class.getSimpleName();

    private static final String FILE_UID = "6221dd49a1bb46cfb61efe62c4526bd800";

    File file1 = null;
    QBFileObjectAccess fileObjectAccess;

    public SnippetsContent(Context context) {
        super(context);

        snippets.add(createFile);
        snippets.add(createFileSynchronous);
        //
        snippets.add(updateFile);
        snippets.add(updateFileSynchronous);
        //
        snippets.add(getFileWithId);
        snippets.add(getFileWithIdSynchronous);
        //
        snippets.add(deleteFile);
        snippets.add(deleteFileSynchronous);
        //
        snippets.add(uploadFile);
        snippets.add(uploadFileSynchronous);
        //
        snippets.add(declareFileUploaded);
        snippets.add(declareFileUploadedSynchronous);
        //
        snippets.add(getFileObjectAccess);
        snippets.add(getFileObjectAccessSynchronous);
        //
        snippets.add(downloadFileWithUID);
        snippets.add(downloadFileWithUIDSynchronous);
        //
        snippets.add(downloadFileWithID);
        snippets.add(downloadFileWithIDSynchronous);
        //
        snippets.add(getFiles);
        snippets.add(getFilesSynchronous);
        //
        //
        snippets.add(uploadFileTask);
        snippets.add(uploadFileTaskSynchronous);
        //
        snippets.add(updateFileTask);
        snippets.add(updateFileTaskSynchronous);

        // get file1
        int fileId = R.raw.kharkov;
        InputStream is = context.getResources().openRawResource(fileId);
        file1 = FileHelper.getFileInputStream(is, "kharkov.jpg", "qb_kharkiv");
    }


    //
    /////////////////////////////////////// Create file ////////////////////////////////////////////
    //


    Snippet createFile = new Snippet("create file") {
        @Override
        public void execute() {

            QBFile qbfile = new QBFile();
            qbfile.setName(file1.getName());
            qbfile.setPublic(false);
            qbfile.setContentType(ContentType.getContentType(file1));
            //
            QBContent.createFile(qbfile).performAsync(new QBEntityCallback<QBFile>() {

                @Override
                public void onSuccess(QBFile file, Bundle params) {
                    Log.i(TAG, ">>> File" + file.toString());
                    fileObjectAccess = file.getFileObjectAccess();
                }

                @Override
                public void onError(QBResponseException errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet createFileSynchronous = new SnippetAsync("create file (synchronous)", context) {
        @Override
        public void executeAsync() {
            QBFile qbfile = new QBFile();
            qbfile.setName(file1.getName());
            qbfile.setPublic(false);
            qbfile.setContentType(ContentType.getContentType(file1));

            QBFile createdFile = null;
            try {
                createdFile = QBContent.createFile(qbfile).perform();
            } catch (QBResponseException e) {
                setException(e);
            }
            if (createdFile != null) {
                Log.i(TAG, ">>> File:" + createdFile);
                fileObjectAccess = createdFile.getFileObjectAccess();
            }
        }
    };


    //
    /////////////////////////////////////// Create file ////////////////////////////////////////////
    //


    Snippet updateFile = new Snippet("update file") {
        @Override
        public void execute() {
            QBFile qbfile = new QBFile();
            qbfile.setId(212949);
            qbfile.setName("my Car1");
            QBContent.updateFile(qbfile).performAsync(new QBEntityCallback<QBFile>(){

                @Override
                public void onSuccess(QBFile updatedFile, Bundle params) {
                    Log.i(TAG, ">>> File:" + updatedFile.toString());
                }

                @Override
                public void onError(QBResponseException errors) {
                         handleErrors(errors);
                }
            });
        }
    };

    Snippet updateFileSynchronous = new SnippetAsync("update file (synchronous)", context) {
        @Override
        public void executeAsync() {
            QBFile qbfile = new QBFile();
            qbfile.setId(212949);
            qbfile.setName("my Car2");

            QBFile updatedFile = null;
            try {
                updatedFile = QBContent.updateFile(qbfile).perform();
            } catch (QBResponseException e) {
                setException(e);
            }
            if (updatedFile != null) {
                Log.i(TAG, ">>> File:" + updatedFile);
            }
        }
    };


    //
    /////////////////////////////////////// Get file ///////////////////////////////////////////////
    //


    Snippet getFileWithId = new Snippet("get file", "with id") {
        @Override
        public void execute() {
            QBContent.getFile(212949).performAsync(new QBEntityCallback<QBFile>(){

                @Override
                public void onSuccess(QBFile file, Bundle params) {
                    Log.i(TAG, ">>> File:" + file.toString());
                }


                @Override
                public void onError(QBResponseException errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet getFileWithIdSynchronous = new SnippetAsync("get file (synchronous)", "with id", context) {
        @Override
        public void executeAsync() {
            QBFile getFile = null;
            try {
                getFile = QBContent.getFile(212949).perform();
            } catch (QBResponseException e) {
                setException(e);
            }
            if (getFile != null) {
                Log.i(TAG, ">>> File:" + getFile);
            }
        }
    };

    //
    ///////////////////////////////////// Delete file ///////////////////////////////////////////////
    //


    Snippet deleteFile = new Snippet("delete file") {
        @Override
        public void execute() {
            QBContent.deleteFile(212949).performAsync(new QBEntityCallback<Void>() {

                @Override
                public void onSuccess(Void result, Bundle bundle) {
                    Log.i(TAG, ">>> file deleted successfully");
                }

                @Override
                public void onError(QBResponseException errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet deleteFileSynchronous = new SnippetAsync("delete file (synchronous)", context) {
        @Override
        public void executeAsync() {
            try {
                QBContent.deleteFile(212949).perform();
                Log.i(TAG, ">>> file deleted successfully");
            } catch (QBResponseException e) {
                setException(e);
            }
        }
    };


    //
    /////////////////////////////////////// Upload file ////////////////////////////////////////////
    //

    Snippet uploadFile = new Snippet("upload file") {
        @Override
        public void execute() {
            String params = fileObjectAccess.getParams();   // will return from the server when creating file
            QBContent.uploadFile(file1, params, new QBProgressCallback() {
                @Override
                public void onProgressUpdate(int progress) {
                    Log.i(TAG, "progress: " + progress);
                }
            }).performAsync(new QBEntityCallback<Void>() {
                @Override
                public void onSuccess(Void amazonS3Response, Bundle params) {
                    Log.i(TAG, ">>> AmazonS3Response: " + amazonS3Response);
                }

                @Override
                public void onError(QBResponseException errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet uploadFileSynchronous = new SnippetAsync("upload file (synchronous)", context) {
        @Override
        public void executeAsync() {
            String params = fileObjectAccess.getParams();   // will return from the server when creating file

            try {
                QBContent.uploadFile(file1, params, new QBProgressCallback() {
                    @Override
                    public void onProgressUpdate(int progress) {
                        Log.i(TAG, "progress: " + progress);
                    }
                }).perform();
            } catch (QBResponseException e) {
                setException(e);
            }
        }
    };

    //
    //////////////////////////////////// Declare file uploaded /////////////////////////////////////
    //


    Snippet declareFileUploaded = new Snippet("declare file uploaded") {
        @Override
        public void execute() {
            QBContent.declareFileUploaded(212950, (int) file1.length()).performAsync(new QBEntityCallback<Void>() {

                @Override
                public void onSuccess(Void result, Bundle bundle) {
                    Log.i(TAG, ">>> declare file uploaded was successful");
                }

                @Override
                public void onError(QBResponseException errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet declareFileUploadedSynchronous = new SnippetAsync("declare file uploaded (synchronous)", context) {
        @Override
        public void executeAsync() {
            try {
                QBContent.declareFileUploaded(212951, (int) file1.length()).perform();
                Log.i(TAG, ">>> declare file uploaded was successful");
            } catch (QBResponseException e) {
                setException(e);
            }
        }
    };


    //
    ////////////////////////////////// Get file object access //////////////////////////////////////
    //


    Snippet getFileObjectAccess = new Snippet("get file object access") {
        @Override
        public void execute() {
            QBContent.getFileObjectAccess(212951).performAsync(new QBEntityCallback<QBFileObjectAccess>() {

                @Override
                public void onSuccess(QBFileObjectAccess fileObjectAccess, Bundle params) {
                    Log.i(TAG, ">>> FileObjectAccess: " + fileObjectAccess);
                }

                @Override
                public void onError(QBResponseException errors) {
                           handleErrors(errors);
                }
            });
        }
    };

    Snippet getFileObjectAccessSynchronous = new SnippetAsync("get file object access (synchronous)", context) {
        @Override
        public void executeAsync() {
            QBFileObjectAccess fileObjectAccess = null;
            try {
                fileObjectAccess = QBContent.getFileObjectAccess(212951).perform();
            } catch (QBResponseException e) {
                setException(e);
            }

            if(fileObjectAccess != null){
                Log.i(TAG, ">>> FileObjectAccess: " + fileObjectAccess);
            }
        }
    };


    //
    ////////////////////////////////////// Download file ///////////////////////////////////////////
    //


    Snippet downloadFileWithUID = new Snippet("download file with UID") {
        @Override
        public void execute() {
            Bundle params = new Bundle();
            QBContent.downloadFile(FILE_UID, new QBProgressCallback() {
                @Override
                public void onProgressUpdate(int progress) {
                    Log.i(TAG, "progress: " + progress);
                }
            }, params).performAsync(new QBEntityCallback<InputStream>() {

                @Override
                public void onSuccess(InputStream inputStream, Bundle params) {
                    long length = params.getLong(Consts.CONTENT_LENGTH_TAG);
                    Log.i(TAG, "content.length: " + length);
                }

                @Override
                public void onError(QBResponseException errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet downloadFileWithUIDSynchronous = new SnippetAsync("download file with UID (synchronous)", context) {
        @Override
        public void executeAsync() {
            InputStream inputStream = null;
            Bundle params = new Bundle();
            try {
                inputStream = QBContent.downloadFile(FILE_UID, new QBProgressCallback() {
                    @Override
                    public void onProgressUpdate(int progress) {
                        Log.i(TAG, "progress: " + progress);
                    }
                }, params).perform();
            } catch (QBResponseException e) {
                setException(e);
            }

            if(inputStream != null){
                long length = params.getLong(Consts.CONTENT_LENGTH_TAG);
                Log.i(TAG, "content.length: " + length);
            }
        }
    };

    Snippet downloadFileWithID = new Snippet("TASK: download file") {
        @Override
        public void execute() {
            final int fileId = 3607775;

            QBContent.downloadFileById(fileId, new QBProgressCallback() {
                @Override
                public void onProgressUpdate(int progress) {
                    Log.i(TAG, "progress: " + progress);
                }
            }).performAsync(new QBEntityCallback<InputStream>() {

                @Override
                public void onSuccess(final InputStream inputStream, Bundle params) {
                    long length = params.getLong(Consts.CONTENT_LENGTH_TAG);
                    Log.i(TAG, "content.length: " + length);

                    Utils.downloadFile(inputStream, context);
                }

                @Override
                public void onError(QBResponseException errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet downloadFileWithIDSynchronous = new SnippetAsync("TASK: download file (synchronous)", context) {
        @Override
        public void executeAsync() {
            final int fileId = 3607775;

            InputStream inputStream = null;
            Bundle params = new Bundle();

            try {
                inputStream = QBContent.downloadFileById(fileId, params, new QBProgressCallback() {
                    @Override
                    public void onProgressUpdate(int progress) {
                        Log.i(TAG, "progress: " + progress);
                    }
                }).perform();
            } catch (QBResponseException e) {
                setException(e);
            }

            if(inputStream != null){
                long length = params.getLong(Consts.CONTENT_LENGTH_TAG);
                Log.i(TAG, "content.length  : " + length);

                Utils.downloadFile(inputStream, context);
            }
        }
    };


    //
    ///////////////////////////////////////////// Get files /////////////////////////////////////////////
    //


    Snippet getFiles = new Snippet("get files") {
        @Override
        public void execute() {
            QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder(5, 2);

            QBContent.getFiles(requestBuilder).performAsync(new QBEntityCallback<ArrayList<QBFile>>() {

                @Override
                public void onSuccess(ArrayList<QBFile> files, Bundle params) {
                    Log.i(TAG, ">>> File list:" + files.toString());
                }

                @Override
                public void onError(QBResponseException errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet getFilesSynchronous = new SnippetAsync("get files (synchronous)", context) {
        @Override
        public void executeAsync() {
            Bundle params = new Bundle();
            QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder(10, 1);
            ArrayList<QBFile> files = null;
            try {
                files = QBContent.getFiles(requestBuilder, params).perform();
            } catch (QBResponseException e) {
                setException(e);
            }

            if(files != null){
                Log.i(TAG, "files: "+ files);
                Log.i(TAG, "currentPage: " + params.getInt(Consts.CURR_PAGE));
                Log.i(TAG, "perPage: " + params.getInt(Consts.PER_PAGE));
                Log.i(TAG, "totalPages: " + params.getInt(Consts.TOTAL_ENTRIES));
            }
        }
    };


    //
    ///////////////////////////////////////////// Tasks /////////////////////////////////////////////
    //


    Snippet uploadFileTask = new Snippet("TASK: upload file") {
        @Override
        public void execute() {

            Boolean fileIsPublic = false;
            QBContent.uploadFileTask(file1, fileIsPublic, null, new QBProgressCallback() {
                @Override
                public void onProgressUpdate(int progress) {
                    Log.i(TAG, "progress: " + progress);
                }
            }).performAsync(new QBEntityCallback<QBFile>() {

                @Override
                public void onSuccess(QBFile qbFile, Bundle params) {
                    Log.i(TAG, ">>> QBFile:" + qbFile.toString());

                    Log.i(TAG, "public url:" + qbFile.getPublicUrl());
                    Log.i(TAG, "private url:" + qbFile.getPrivateUrl());
                    //
                    Log.i(TAG, "public url static:" + QBFile.getPublicUrlForUID(qbFile.getUid()));
                    Log.i(TAG, "private url static:" + QBFile.getPrivateUrlForUID(qbFile.getUid()));
                }

                @Override
                public void onError(QBResponseException errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet uploadFileTaskSynchronous = new SnippetAsync("TASK: upload file (synchronous)", context) {
        @Override
        public void executeAsync() {
            Boolean fileIsPublic = false;
            QBFile qbFile = null;
            try {
                qbFile = QBContent.uploadFileTask(file1, fileIsPublic, null, new QBProgressCallback() {
                    @Override
                    public void onProgressUpdate(int progress) {
                        Log.i(TAG, "progress: " + progress);
                    }
                }).perform();
            } catch (QBResponseException e) {
                setException(e);
            }

            if(qbFile != null){
                Log.i(TAG, "files: "+ qbFile);
            }
        }
    };


    Snippet updateFileTask = new Snippet("TASK: update file") {
        final int fileId = 231089;

        @Override
        public void execute() {
            QBContent.updateFileTask(file1, fileId, null, new QBProgressCallback() {
                @Override
                public void onProgressUpdate(int progress) {
                    Log.i(TAG, "progress: " + progress);
                }
            }).performAsync(new QBEntityCallback<QBFile>() {

                @Override
                public void onSuccess(QBFile qbFile, Bundle params) {
                    Log.i(TAG, ">>> file updated successful" + qbFile);
                }

                @Override
                public void onError(QBResponseException errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet updateFileTaskSynchronous = new SnippetAsync("TASK: update file (synchronous)", context) {

        @Override
        public void executeAsync() {
            final int fileId = 231089;

            QBFile qbFile = null;
            try {
                qbFile = QBContent.updateFileTask(file1, fileId, null, new QBProgressCallback() {
                    @Override
                    public void onProgressUpdate(int progress) {
                        Log.i(TAG, "progress: " + progress);
                    }
                }).perform();
            } catch (QBResponseException e) {
                setException(e);
            }

            if(qbFile != null) {
                Log.i(TAG, "file: " + qbFile);
            }
        }
    };
}
