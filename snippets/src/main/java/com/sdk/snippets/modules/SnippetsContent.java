package com.sdk.snippets.modules;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.QBProgressCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.ContentType;
import com.quickblox.core.helper.FileHelper;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.core.Consts;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.content.model.QBFileObjectAccess;
import com.quickblox.content.model.amazon.PostResponse;
import com.sdk.snippets.R;
import com.sdk.snippets.core.AsyncSnippet;
import com.sdk.snippets.core.Snippet;
import com.sdk.snippets.core.Snippets;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vfite on 04.02.14.
 */
public class SnippetsContent extends Snippets{
    private static final String TAG = SnippetsContent.class.getSimpleName();

    private static final String FILE_UID = "72bf17cf1c6b47118485b527435b5fd500";

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
        snippets.add(getFiles);
        snippets.add(getFilesSynchronous);
        //
        snippets.add(getTaggedList);
        snippets.add(getTaggedListSynchronous);
        //
        //
        snippets.add(uploadFileTask);
        snippets.add(uploadFileTaskSynchronous);
        //
        snippets.add(downloadFileTask);
        snippets.add(downloadFileTaskSynchronous);
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
            QBContent.createFile(qbfile, new QBEntityCallbackImpl<QBFile>() {

                @Override
                public void onSuccess(QBFile file, Bundle params) {
                    Log.i(TAG, ">>> File" + file.toString());
                    fileObjectAccess = file.getFileObjectAccess();
                }

                @Override
                public void onError(List<String> errors) {
                      handleErrors(errors);
                }
            });
        }
    };

    Snippet createFileSynchronous = new AsyncSnippet("create file (synchronous)", context) {
        @Override
        public void executeAsync() {
            QBFile qbfile = new QBFile();
            qbfile.setName(file1.getName());
            qbfile.setPublic(false);
            qbfile.setContentType(ContentType.getContentType(file1));

            QBFile createdFile = null;
            try {
                createdFile = QBContent.createFile(qbfile);
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
            QBContent.updateFile(qbfile, new QBEntityCallbackImpl<QBFile>(){

                @Override
                public void onSuccess(QBFile updatedFile, Bundle params) {
                    Log.i(TAG, ">>> File:" + updatedFile.toString());
                }

                @Override
                public void onError(List<String> errors) {
                         handleErrors(errors);
                }
            });
        }
    };

    Snippet updateFileSynchronous = new AsyncSnippet("update file (synchronous)", context) {
        @Override
        public void executeAsync() {
            QBFile qbfile = new QBFile();
            qbfile.setId(212949);
            qbfile.setName("my Car2");

            QBFile updatedFile = null;
            try {
                updatedFile = QBContent.updateFile(qbfile);
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
            QBContent.getFile(212949, new QBEntityCallbackImpl<QBFile>(){

                @Override
                public void onSuccess(QBFile file, Bundle params) {
                    Log.i(TAG, ">>> File:" + file.toString());
                }


                @Override
                public void onError(List<String> errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet getFileWithIdSynchronous = new AsyncSnippet("get file (synchronous)", "with id", context) {
        @Override
        public void executeAsync() {
            QBFile getFile = null;
            try {
                getFile = QBContent.getFile(212949);
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
            QBContent.deleteFile(212949, new QBEntityCallbackImpl() {

                @Override
                public void onSuccess() {
                    Log.i(TAG, ">>> file deleted successfully");
                }

                @Override
                public void onError(List errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet deleteFileSynchronous = new AsyncSnippet("delete file (synchronous)", context) {
        @Override
        public void executeAsync() {
            try {
                QBContent.deleteFile(212949);
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
            QBContent.uploadFile(file1, params, new QBEntityCallbackImpl<PostResponse>(){
                @Override
                public void onSuccess(PostResponse amazonS3Response, Bundle params) {
                    Log.i(TAG, ">>> AmazonS3Response: " + amazonS3Response);
                }

                @Override
                public void onError(List<String> errors) {
                    handleErrors(errors);
                }
            }, new QBProgressCallback() {
                @Override
                public void onProgressUpdate(int progress) {
                    Log.i(TAG, "progress: " + progress);
                }
            });
        }
    };

    Snippet uploadFileSynchronous = new AsyncSnippet("upload file (synchronous)", context) {
        @Override
        public void executeAsync() {
            String params = fileObjectAccess.getParams();   // will return from the server when creating file

            PostResponse amazonS3Response = null;
            try {
                amazonS3Response = QBContent.uploadFile(file1, params, new QBProgressCallback() {
                    @Override
                    public void onProgressUpdate(int progress) {
                        Log.i(TAG, "progress: " + progress);
                    }
                });
            } catch (QBResponseException e) {
                setException(e);
            }
            if(amazonS3Response != null){
                Log.i(TAG, ">>> AmazonS3Response: " + amazonS3Response);
            }
        }
    };

    //
    //////////////////////////////////// Declare file uploaded /////////////////////////////////////
    //


    Snippet declareFileUploaded = new Snippet("declare file uploaded") {
        @Override
        public void execute() {
            QBContent.declareFileUploaded(212950, (int) file1.length(), new QBEntityCallbackImpl() {

                @Override
                public void onSuccess() {
                    Log.i(TAG, ">>> declare file uploaded was successful");
                }

                @Override
                public void onError(List errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet declareFileUploadedSynchronous = new AsyncSnippet("declare file uploaded (synchronous)", context) {
        @Override
        public void executeAsync() {
            try {
                QBContent.declareFileUploaded(212951, (int) file1.length());
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
            QBContent.getFileObjectAccess(212951, new QBEntityCallbackImpl<QBFileObjectAccess>() {

                @Override
                public void onSuccess(QBFileObjectAccess fileObjectAccess, Bundle params) {
                    Log.i(TAG, ">>> FileObjectAccess: " + fileObjectAccess);
                }

                @Override
                public void onError(List<String> errors) {
                           handleErrors(errors);
                }
            });
        }
    };

    Snippet getFileObjectAccessSynchronous = new AsyncSnippet("get file object access (synchronous)", context) {
        @Override
        public void executeAsync() {
            QBFileObjectAccess fileObjectAccess = null;
            try {
                fileObjectAccess = QBContent.getFileObjectAccess(212951);
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
            QBContent.downloadFile(FILE_UID, new QBEntityCallbackImpl<InputStream>() {

                @Override
                public void onSuccess(InputStream inputStream, Bundle params) {
                    long length = params.getLong(Consts.CONTENT_LENGTH_TAG);
                    Log.i(TAG, "content.length: " + length);;
                }

                @Override
                public void onError(List<String> errors) {
                       handleErrors(errors);
                }
            }, new QBProgressCallback() {
                @Override
                public void onProgressUpdate(int progress) {
                    Log.i(TAG, "progress: " + progress);
                }
            });
        }
    };

    Snippet downloadFileWithUIDSynchronous = new AsyncSnippet("download file with UID (synchronous)", context) {
        @Override
        public void executeAsync() {
            InputStream inputStream = null;
            Bundle params = new Bundle();
            try {
                inputStream = QBContent.downloadFile(FILE_UID, params, new QBProgressCallback() {
                    @Override
                    public void onProgressUpdate(int progress) {
                        Log.i(TAG, "progress: " + progress);
                    }
                });
            } catch (QBResponseException e) {
                setException(e);
            }

            if(inputStream != null){
                long length = params.getLong(Consts.CONTENT_LENGTH_TAG);
                Log.i(TAG, "content.length: " + length);
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

            QBContent.getFiles(requestBuilder, new QBEntityCallbackImpl<ArrayList<QBFile>>() {

                @Override
                public void onSuccess(ArrayList<QBFile> files, Bundle params) {
                    Log.i(TAG, ">>> File list:" + files.toString());
                }

                @Override
                public void onError(List<String> errors) {
                      handleErrors(errors);
                }
            });
        }
    };

    Snippet getFilesSynchronous = new AsyncSnippet("get files (synchronous)", context) {
        @Override
        public void executeAsync() {
            Bundle params = new Bundle();
            QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder(10, 1);
            ArrayList<QBFile> files = null;
            try {
                files = QBContent.getFiles(requestBuilder, params);
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
    /////////////////////////////////// Get tagged files ///////////////////////////////////////////
    //


    Snippet getTaggedList = new Snippet("get tagged files") {
        @Override
        public void execute() {
            QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder(20, 1);

            QBContent.getTaggedList(requestBuilder, new QBEntityCallbackImpl<ArrayList<QBFile>>() {
                @Override
                public void onSuccess(ArrayList<QBFile> files, Bundle params) {
                    Log.i(TAG, ">>> File list:" + files.toString());
                }

                @Override
                public void onError(List<String> errors) {
                           handleErrors(errors);
                }
            });
        }
    };

    Snippet getTaggedListSynchronous = new AsyncSnippet("get tagged files (synchronous)", context) {
        @Override
        public void executeAsync() {
            Bundle params = new Bundle();
            QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder(10, 1);
            ArrayList<QBFile> files = null;
            try {
                files = QBContent.getTaggedList(requestBuilder, params);
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
            QBContent.uploadFileTask(file1, fileIsPublic, null, new QBEntityCallbackImpl<QBFile>() {

                @Override
                public void onSuccess(QBFile qbFile, Bundle params) {
                    Log.i(TAG, ">>> QBFile:" + qbFile.toString());
                }

                @Override
                public void onError(List<String> errors) {
                      handleErrors(errors);
                }
            }, new QBProgressCallback() {
                @Override
                public void onProgressUpdate(int progress) {
                    Log.i(TAG, "progress: " + progress);
                }
            });
        }
    };

    Snippet uploadFileTaskSynchronous = new AsyncSnippet("TASK: upload file (synchronous)", context) {
        @Override
        public void executeAsync() {
            Boolean fileIsPublic = false;
            QBFile qbFile = null;
            try {
                qbFile = QBContent.uploadFileTask(file1, fileIsPublic, (String) null, new QBProgressCallback() {
                    @Override
                    public void onProgressUpdate(int progress) {
                        Log.i(TAG, "progress: " + progress);
                    }
                });
            } catch (QBResponseException e) {
                setException(e);
            }

            if(qbFile != null){
                Log.i(TAG, "files: "+ qbFile);
            }
        }
    };



    Snippet downloadFileTask = new Snippet("TASK: download file") {
        @Override
        public void execute() {
            final int fileId = 2641910;

            QBContent.downloadFileTask(fileId, new QBEntityCallbackImpl<InputStream>(){

                @Override
                public void onSuccess(final InputStream inputStream, Bundle params) {
                    long length = params.getLong(Consts.CONTENT_LENGTH_TAG);
                    Log.i(TAG, "content.length: " + length);

//                        Thread thread = new Thread() {
//                            @Override
//                            public void run() {
//                                try {
//                                    while(true) {
//                                        String filePath = context.getFilesDir().getPath().toString() + "/bigFile.pkg";
//                                        File file = new File(filePath);
//                                        OutputStream stream = new BufferedOutputStream(new FileOutputStream(file));
//                                        int bufferSize = 1024;
//                                        byte[] buffer = new byte[bufferSize];
//                                        int len;
//                                        while ((len = inputStream.read(buffer)) != -1) {
//                                            stream.write(buffer, 0, len);
//                                        }
//                                        if(stream != null) {
//                                            stream.close();
//                                        }
//                                    }
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        };
//                        thread.start();
                }

                @Override
                public void onError(List<String> errors) {
                               handleErrors(errors);
                }
            }, new QBProgressCallback() {
                @Override
                public void onProgressUpdate(int progress) {
                    Log.i(TAG, "progress: " + progress);
                }
            });
        }
    };

    Snippet downloadFileTaskSynchronous = new AsyncSnippet("TASK: download file (synchronous)", context) {
        @Override
        public void executeAsync() {
            final int fileId = 2641910;

            InputStream inputStream = null;
            Bundle params = new Bundle();

            try {
                inputStream = QBContent.downloadFileTask(fileId, params, new QBProgressCallback() {
                    @Override
                    public void onProgressUpdate(int progress) {
                        Log.i(TAG, "progress: " + progress);
                    }
                });
            } catch (QBResponseException e) {
                setException(e);
            }

            if(inputStream != null){
                long length = params.getLong(Consts.CONTENT_LENGTH_TAG);
                Log.i(TAG, "content.length  : " + length);
            }
        }
    };



    Snippet updateFileTask = new Snippet("TASK: update file") {
        final int fileId = 231089;
        @Override
        public void execute() {
            QBContent.updateFileTask(file1, fileId, null, new QBEntityCallbackImpl<QBFile>(){

                @Override
                public void onSuccess(QBFile qbFile, Bundle params) {
                    Log.i(TAG, ">>> file updated successful"+qbFile);
                }


                @Override
                public void onError(List<String> errors) {
                      handleErrors(errors);
                }
            }, new QBProgressCallback() {
                @Override
                public void onProgressUpdate(int progress) {
                    Log.i(TAG, "progress: " + progress);
                }
            });
        }
    };

    Snippet updateFileTaskSynchronous = new AsyncSnippet("TASK: update file (synchronous)", context) {

        @Override
        public void executeAsync() {
            final int fileId = 231089;

            QBFile qbFile = null;
            try {
                qbFile = QBContent.updateFileTask(file1, fileId, (String)null, new QBProgressCallback() {
                    @Override
                    public void onProgressUpdate(int progress) {
                        Log.i(TAG, "progress: " + progress);
                    }
                });
            } catch (QBResponseException e) {
                setException(e);
            }

            if(qbFile != null) {
                Log.i(TAG, "file: " + qbFile);
            }
        }
    };
}
