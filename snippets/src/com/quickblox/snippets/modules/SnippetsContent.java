package com.quickblox.snippets.modules;

import android.content.Context;
import android.util.Log;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.QBStringResult;
import com.quickblox.core.result.Result;
import com.quickblox.internal.core.helper.ContentType;
import com.quickblox.internal.core.helper.FileHelper;
import com.quickblox.internal.core.request.QBPagedRequestBuilder;
import com.quickblox.module.content.QBContent;
import com.quickblox.module.content.model.QBFile;
import com.quickblox.module.content.result.*;
import com.quickblox.snippets.R;
import com.quickblox.snippets.Snippet;
import com.quickblox.snippets.Snippets;

import java.io.File;
import java.io.InputStream;

/**
 * User: Oleg Soroka
 * Date: 11.10.12
 * Time: 13:03
 */
public class SnippetsContent extends Snippets {

    File file = null;

    public SnippetsContent(Context context) {
        super(context);

        snippets.add(uploadFileTask);
        snippets.add(downloadFileTask);
        snippets.add(getFiles);
        snippets.add(getTaggedList);
        snippets.add(getFileWithId);
        snippets.add(createFile);
        snippets.add(uploadFile);
        snippets.add(declareFileUpload);
        snippets.add(updateFile);
        snippets.add(getFileObjectAccess);
        snippets.add(downloadFileWithUID);
        snippets.add(deleteFile);
        snippets.add(incrementRefCount);
        snippets.add(getFileDownloadLink);

        // get file
        int fileId = R.raw.sample_file;
        InputStream is = context.getResources().openRawResource(fileId);
        file = FileHelper.getFileInputStream(is, "sample_file.txt", "qb_snippets12");
    }

    Snippet createFile = new Snippet("create file") {
        @Override
        public void execute() {

            QBFile qbfile = new QBFile();
            qbfile.setName(file.getName());
            qbfile.setPublic(true);
            qbfile.setContentType(ContentType.getContentType(file));
            QBContent.createFile(qbfile, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    QBFileResult fileResult = (QBFileResult) result;
                    if (result.isSuccess()) {
                        System.out.println(">>> File" + fileResult.getFile().toString());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet getFiles = new Snippet("get files with pagination") {
        @Override
        public void execute() {
            QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder(20, 1);
            QBContent.getFiles(requestBuilder, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBFilePagedResult qbFilePagedResult = (QBFilePagedResult) result;
                        Log.d("PUBLICURL", qbFilePagedResult.getFiles().get(0).getPublicUrl());
                        System.out.println(">>> File list:" + qbFilePagedResult.getFiles().toString());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet getTaggedList = new Snippet("get tagged list") {
        @Override
        public void execute() {
            QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder(20, 1);

            QBContent.getTaggedList(requestBuilder, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBFilePagedResult qbFilePagedResult = (QBFilePagedResult) result;
                        System.out.println(">>> File list:" + qbFilePagedResult.getFiles().toString());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet getFileWithId = new Snippet("get file with id") {
        @Override
        public void execute() {
            QBContent.getFile(14, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBFileResult fileResult = (QBFileResult) result;

                        System.out.println(">>> file " + fileResult.getFile());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet updateFile = new Snippet("update file") {
        @Override
        public void execute() {
            QBFile qbfile = new QBFile();
            qbfile.setId(19979);
            qbfile.setName("my Car1");
            QBContent.updateFile(qbfile, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBFileResult fileResult = (QBFileResult) result;
                        System.out.println(">>> File:" + fileResult.getFile().toString());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet deleteFile = new Snippet("delete file") {
        @Override
        public void execute() {
            QBContent.deleteFile(123, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        System.out.println(">>> file deleted successfully");
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet uploadFile = new Snippet("upload file") {
        @Override
        public void execute() {
            String params = "";   // will return from the server when creating file
            QBContent.uploadFile(file, params, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBFileUploadResult uploadResult = (QBFileUploadResult) result;
                        System.out.println(">>> AmazonPostResponse" + uploadResult.getAmazonPostResponse());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet declareFileUpload = new Snippet("declare file upload") {
        @Override
        public void execute() {
            QBContent.declareFileUploaded(123, 53252, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        System.out.println(">>> declare file uploaded was successful" + result.toString());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet incrementRefCount = new Snippet("increment ref count") {
        @Override
        public void execute() {
            QBContent.incrementRefCount(125, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        System.out.println(">>> count of ref increment successfully" + result.toString());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };


    Snippet downloadFileWithUID = new Snippet("download file with UID") {
        @Override
        public void execute() {
            QBContent.downloadFile("abcdfh6465dfw9hsdfsfs4727r3y29", new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    QBFileDownloadResult downloadResult = (QBFileDownloadResult) result;
                    if (result.isSuccess()) {

                        byte[] content = downloadResult.getContent();       // that's downloaded file content
                        InputStream is = downloadResult.getContentStream(); // that's downloaded file content

                        System.out.println(">>> file downloaded successfully" + downloadResult.getContent().toString());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet getFileObjectAccess = new Snippet("get file object access") {
        @Override
        public void execute() {
            QBContent.getFileObjectAccess(41, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    QBFileObjectAccessResult objectAccessResult = (QBFileObjectAccessResult) result;
                    if (result.isSuccess()) {
                        System.out.println(">>> FileObjectAccess" + objectAccessResult.getFileObjectAccess().toString());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet getFileDownloadLink = new Snippet("get file download link TASK") {
        @Override
        public void execute() {
            QBContent.getFileDownloadLink(12, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    QBStringResult qbStringResult = ((QBStringResult) result);
                    if (result.isSuccess()) {
                        System.out.println(">>> download link" + qbStringResult.toString());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };


    //
    ///////////////////////////////////////////// Tasks /////////////////////////////////////////////
    //
    Snippet uploadFileTask = new Snippet("upload file task") {
        @Override
        public void execute() {

            Boolean fileIsPublic = true;
            QBContent.uploadFileTask(file, fileIsPublic, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {

                    if (result.isSuccess()) {
                        QBFileUploadTaskResult fileUploadTaskResultResult = (QBFileUploadTaskResult) result;
                        QBFile qbFile = fileUploadTaskResultResult.getFile();
                        String downloadUrl = qbFile.getPublicUrl();

                        System.out.println(">>> QBFile:" + qbFile.toString());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet downloadFileTask = new Snippet("download file Task") {
        @Override
        public void execute() {
            QBContent.downloadFileTask(123, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {

                    QBFileDownloadResult qbFileDownloadResult = (QBFileDownloadResult) result;
                    if (result.isSuccess()) {
                        System.out.println(">>> file downloaded successful" + qbFileDownloadResult.getContent().toString());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };
}