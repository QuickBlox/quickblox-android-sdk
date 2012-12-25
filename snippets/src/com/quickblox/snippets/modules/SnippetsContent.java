package com.quickblox.snippets.modules;

import android.content.Context;
import android.util.Log;
import com.quickblox.core.QBCallback;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.QBStringResult;
import com.quickblox.core.result.Result;
import com.quickblox.internal.core.helper.ContentType;
import com.quickblox.internal.core.helper.FileHelper;
import com.quickblox.module.content.QBContent;
import com.quickblox.module.content.model.QBFile;
import com.quickblox.module.content.result.QBFileDownloadResult;
import com.quickblox.module.content.result.QBFileResult;
import com.quickblox.module.content.result.QBFileUploadResult;
import com.quickblox.module.content.result.QBFileUploadTaskResult;
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

    public SnippetsContent(Context context) {
        super(context);
//        - get file download link TASK (??? уточнить со мной о дореализации)

        snippets.add(uploadFileTask);
        snippets.add(downloadFileTask);
        snippets.add(getFiles);
        snippets.add(getTaggedList);
        snippets.add(getFileWithId);
        snippets.add(createFile);
        snippets.add(declareFileUpload);
        snippets.add(uploadFile);
        snippets.add(updateFile);
        snippets.add(getFileObjectAccess);
        snippets.add(downloadFileWithUID);
        snippets.add(deleteFile);
        snippets.add(incrementRefCount);
        snippets.add(getFileDownloadLink);

    }

    String uid = null; // file id
    int fileID = 709;
    File file = null;
    String params;
    QBFile qbfile;
    int fileSize = 0;

    Snippet getFileDownloadLink = new Snippet("get file download link TASK") {
        @Override
        public void execute() {
            if (fileID != 0) {
                QBContent.getFileDownloadLink(fileID, new QBCallback() {
                    @Override
                    public void onComplete(Result result) {
                        printResultToConsole(result);
                        if (result.isSuccess()) {
                            Log.d("QBStringResult", ((QBStringResult) result).getString());
                        }
                    }

                    @Override
                    public void onComplete(Result result, Object context) {
                    }
                });
            }
        }
    };


    Snippet incrementRefCount = new Snippet("increment ref count") {
        @Override
        public void execute() {
            if (fileID != 0) {
                QBContent.incrementRefCount(fileID, new QBCallback() {
                    @Override
                    public void onComplete(Result result) {
                        printResultToConsole(result);
                    }

                    @Override
                    public void onComplete(Result result, Object context) {
                    }
                });
            }
        }
    };

    Snippet deleteFile = new Snippet("delete file") {
        @Override
        public void execute() {
            if (fileID != 0) {
                QBContent.deleteFile(fileID, new QBCallback() {
                    @Override
                    public void onComplete(Result result) {
                        printResultToConsole(result);
                        if (result.isSuccess()) {
                            fileID = 0;
                        }
                    }

                    @Override
                    public void onComplete(Result result, Object context) {
                    }
                });
            }
        }
    };

    Snippet downloadFileWithUID = new Snippet("download file with UID") {
        @Override
        public void execute() {
            if (uid != null) {
                QBContent.downloadFile(uid, new QBCallback() {
                    @Override
                    public void onComplete(Result result) {
                        printResultToConsole(result);
                    }

                    @Override
                    public void onComplete(Result result, Object context) {
                    }
                });
            }
        }
    };

    Snippet getFileObjectAccess = new Snippet("get file object access") {
        @Override
        public void execute() {
            if (fileID != 0) {
                QBContent.getFileObjectAccess(fileID, new QBCallback() {
                    @Override
                    public void onComplete(Result result) {
                        printResultToConsole(result);
                    }

                    @Override
                    public void onComplete(Result result, Object context) {
                    }
                });
            }
        }
    };

    Snippet updateFile = new Snippet("update file") {
        @Override
        public void execute() {
            if (fileID != 0) {
                QBFile qbfile = new QBFile();
                qbfile.setId(fileID);
                qbfile.setName("newName");
                QBContent.updateFile(qbfile, new QBCallback() {
                    @Override
                    public void onComplete(Result result) {
                        printResultToConsole(result);
                    }

                    @Override
                    public void onComplete(Result result, Object context) {
                    }
                });
            }
        }
    };

    Snippet createFile = new Snippet("create file") {
        @Override
        public void execute() {
            int fileId = R.raw.sample_file;
            InputStream is = context.getResources().openRawResource(fileId);
            file = FileHelper.getFileInputStream(is, "sample_file.txt", "qb_snippets");
            qbfile = new QBFile();
            boolean publicAccess = true;
            String contentType = ContentType.getContentType(file);
            qbfile.setName(file.getName());
            qbfile.setPublic(publicAccess);
            qbfile.setContentType(contentType);
            QBContent.createFile(qbfile, new QBCallback() {
                @Override
                public void onComplete(Result result) {
                    printResultToConsole(result);
                    if (result.isSuccess()) {
                        params = ((QBFileResult) result).getFile().getFileObjectAccess().getParams();
                    } else {
                        file = null;
                    }
                }

                @Override
                public void onComplete(Result result, Object context) {
                }
            });

        }
    };

    Snippet uploadFile = new Snippet("upload file") {
        @Override
        public void execute() {
            if (file != null) {
                QBContent.uploadFile(file, params, new QBCallback() {
                    @Override
                    public void onComplete(Result result) {
                        printResultToConsole(result);
                        if (result.isSuccess()) {
                            QBFileUploadResult uploadResult = (QBFileUploadResult) result;
                            String downloadUrl = uploadResult.getAmazonPostResponse().getLocation();
                            qbfile.setDownloadUrl(downloadUrl);

                            int fileId = qbfile.getId();
                            fileSize = (int) file.length();
                        }
                    }

                    @Override
                    public void onComplete(Result result, Object context) {

                    }
                });
            }
        }
    };

    Snippet declareFileUpload = new Snippet("declare file upload") {
        @Override
        public void execute() {
            if (fileID != 0) {
                QBContent.declareFileUploaded(fileID, fileSize, new QBCallback() {
                    @Override
                    public void onComplete(Result result) {
                        printResultToConsole(result);
                    }

                    @Override
                    public void onComplete(Result result, Object context) {
                    }
                });
            }
        }
    };

    Snippet getFileWithId = new Snippet("get file with id") {
        @Override
        public void execute() {
            if (fileID != 0) {
                QBContent.getFile(fileID, new QBCallback() {
                    @Override
                    public void onComplete(Result result) {
                        printResultToConsole(result);
                    }

                    @Override
                    public void onComplete(Result result, Object context) {
                    }
                });
            }
        }
    };

    Snippet uploadFileTask = new Snippet("upload file task") {
        @Override
        public void execute() {

            int fileId = R.raw.sample_file;
            InputStream is = context.getResources().openRawResource(fileId);

            // You should add permission to your AndroidManifest.xml file
            // <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
            // to allow read data from InputStream to File
            File file = FileHelper.getFileInputStream(is, "sample_file.txt", "qb_snippets");

            Boolean fileIsPublic = true;

            QBContent.uploadFileTask(file, fileIsPublic, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    printResultToConsole(result);
                    if (result.isSuccess()) {
                        QBFileUploadTaskResult fileUploadTaskResultResult = (QBFileUploadTaskResult) result;
                        QBFile qbFile = fileUploadTaskResultResult.getFile();
                        String downloadUrl = qbFile.getDownloadUrl();

                        System.out.println(">>> file has been successfully uploaded, " +
                                "there is link to download below:");
                        System.out.println(">>> " + downloadUrl);
                        fileID = qbFile.getId();
                        uid = qbFile.getUid();
                    }
                }
            });
        }
    };

    Snippet downloadFileTask = new Snippet("download file") {
        @Override
        public void execute() {
            if (uid == null) {
                System.out.println("Upload file to storage before downloading.");
            } else {
                QBContent.downloadFile(uid, new QBCallbackImpl() {
                    @Override
                    public void onComplete(Result result) {
                        printResultToConsole(result);
                        if (result.isSuccess()) {
                            QBFileDownloadResult fileDownloadResult = (QBFileDownloadResult) result;
                            byte[] content = fileDownloadResult.getContent();       // that's downloaded file content
                            InputStream is = fileDownloadResult.getContentStream(); // that's downloaded file content
                        }
                    }
                });
            }
        }
    };

    Snippet getFiles = new Snippet("get files with pagination") {
        @Override
        public void execute() {
            QBContent.getFiles(1, 20, new QBCallback() {
                @Override
                public void onComplete(Result result) {
                    printResultToConsole(result);
                }

                @Override
                public void onComplete(Result result, Object context) {
                }
            });
        }
    };

    Snippet getTaggedList = new Snippet("get tagged list") {
        @Override
        public void execute() {
            QBContent.getTaggedList(1, 20, new QBCallback() {
                @Override
                public void onComplete(Result result) {
                    printResultToConsole(result);
                }

                @Override
                public void onComplete(Result result, Object context) {
                }
            });
        }
    };
}