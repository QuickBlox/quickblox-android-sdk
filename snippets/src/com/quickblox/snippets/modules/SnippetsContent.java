package com.quickblox.snippets.modules;

import android.content.Context;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.internal.core.helper.FileHelper;
import com.quickblox.module.content.QBContent;
import com.quickblox.module.content.model.QBFile;
import com.quickblox.module.content.result.QBFileDownloadResult;
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

        snippets.add(uploadFile);
        snippets.add(downloadFile);
    }

    String uid = null; // file id

    Snippet uploadFile = new Snippet("upload file") {
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

                        uid = qbFile.getUid();
                    }
                }
            });
        }
    };

    Snippet downloadFile = new Snippet("download file") {
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
}