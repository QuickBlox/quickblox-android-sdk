package com.quickblox.android.framework.snippets.modules;

import android.content.Context;
import com.quickblox.android.framework.base.definitions.QBCallback;
import com.quickblox.android.framework.base.helpers.FileHelper;
import com.quickblox.android.framework.base.net.results.Result;
import com.quickblox.android.framework.modules.content.models.QBFile;
import com.quickblox.android.framework.modules.content.net.results.QBFileDownloadResult;
import com.quickblox.android.framework.modules.content.net.results.QBFileUploadTaskResult;
import com.quickblox.android.framework.modules.content.net.server.QBContent;
import com.quickblox.android.framework.snippets.R;
import com.quickblox.android.framework.snippets.Snippet;
import com.quickblox.android.framework.snippets.Snippets;

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

            QBContent.uploadFileTask(file, fileIsPublic, new QBCallback() {
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
                QBContent.downloadFile(uid, new QBCallback() {
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