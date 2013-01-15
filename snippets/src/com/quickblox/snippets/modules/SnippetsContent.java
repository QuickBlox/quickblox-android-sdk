package com.quickblox.snippets.modules;

import android.content.Context;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.QBRequestCanceler;
import com.quickblox.core.result.Result;
import com.quickblox.internal.core.helper.ContentType;
import com.quickblox.internal.core.helper.FileHelper;
import com.quickblox.internal.core.request.QBPagedRequestBuilder;
import com.quickblox.module.content.QBContent;
import com.quickblox.module.content.model.QBFile;
import com.quickblox.module.content.model.QBFileObjectAccess;
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
    QBFileObjectAccess fileObjectAccess;

    public SnippetsContent(Context context) {
        super(context);

        snippets.add(createFile);
        snippets.add(updateFile);
        snippets.add(getFileWithId);
        snippets.add(uploadFile);
        snippets.add(declareFileUpload);
        snippets.add(incrementRefCount);
        snippets.add(deleteFile);
        snippets.add(getFileObjectAccess);
        snippets.add(downloadFileWithUID);

        snippets.add(getFiles);
        snippets.add(getTaggedList);

        snippets.add(uploadFileTask);
        snippets.add(downloadFileTask);

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
            //
            QBContent.createFile(qbfile, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    QBFileResult fileResult = (QBFileResult) result;
                    if (result.isSuccess()) {
                        System.out.println(">>> File" + fileResult.getFile().toString());

                        fileObjectAccess = fileResult.getFile().getFileObjectAccess();
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
            qbfile.setId(20223);
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

    Snippet getFileWithId = new Snippet("get file with id") {
        @Override
        public void execute() {
            QBContent.getFile(20223, new QBCallbackImpl() {
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

    Snippet uploadFile = new Snippet("upload file") {
        @Override
        public void execute() {
            String params = fileObjectAccess.getParams();   // will return from the server when creating file
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
            QBContent.declareFileUploaded(20237, (int) file.length(), new QBCallbackImpl() {
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
            QBContent.incrementRefCount(20237, new QBCallbackImpl() {
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

    Snippet deleteFile = new Snippet("delete file") {
        @Override
        public void execute() {
            QBContent.deleteFile(20237, new QBCallbackImpl() {
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

    Snippet getFileObjectAccess = new Snippet("get file object access") {
        @Override
        public void execute() {
            QBContent.getFileObjectAccess(20237, new QBCallbackImpl() {
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

    Snippet downloadFileWithUID = new Snippet("download file with UID") {
        @Override
        public void execute() {
            QBContent.downloadFile("29e35b2faef54b15a8706c39857dc22f00", new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    QBFileDownloadResult downloadResult = (QBFileDownloadResult) result;
                    if (result.isSuccess()) {

                        byte[] content = downloadResult.getContent();       // that's downloaded file content
                        InputStream is = downloadResult.getContentStream(); // that's downloaded file content

                        System.out.println(">>> file downloaded successfully" + content.toString());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    //
    ///////////////////////////////////////////// Get files /////////////////////////////////////////////
    //
    Snippet getFiles = new Snippet("get files") {
        @Override
        public void execute() {
            QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder(5, 2);

            QBContent.getFiles(requestBuilder, new QBCallbackImpl() {
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

    //
    ///////////////////////////////////////////// Tasks /////////////////////////////////////////////
    //
    Snippet uploadFileTask = new Snippet("upload file task") {
        @Override
        public void execute() {

            Boolean fileIsPublic = true;
            QBRequestCanceler requestCanceler = QBContent.uploadFileTask(file, fileIsPublic, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {

                    if (result.isSuccess()) {
                        QBFileUploadTaskResult fileUploadTaskResultResult = (QBFileUploadTaskResult) result;

                        QBFile qbFile = fileUploadTaskResultResult.getFile();

                        System.out.println(">>> QBFile:" + qbFile.toString());
                    } else {
                        handleErrors(result);
                    }
                }
            });
//            requestCanceler.cancel();
        }
    };

    Snippet downloadFileTask = new Snippet("download file Task") {
        @Override
        public void execute() {
            QBContent.downloadFileTask(20248, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {

                    QBFileDownloadResult qbFileDownloadResult = (QBFileDownloadResult) result;
                    if (result.isSuccess()) {

                        byte[] content = qbFileDownloadResult.getContent();       // that's downloaded file content
                        InputStream is = qbFileDownloadResult.getContentStream(); // that's downloaded file content

                        System.out.println(">>> file downloaded successful" + qbFileDownloadResult.getContent().toString());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };
}