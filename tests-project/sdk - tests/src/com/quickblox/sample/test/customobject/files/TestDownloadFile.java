package com.quickblox.sample.test.customobject.files;

import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.internal.core.helper.FileHelper;
import com.quickblox.module.content.result.QBFileDownloadResult;
import com.quickblox.module.content.result.QBFileUploadTaskResult;
import com.quickblox.module.custom.QBCustomObjectsFiles;
import org.apache.http.HttpStatus;

import java.io.File;

/**
 * Created by vfite on 12.12.13.
 */
public class TestDownloadFile extends TestFileTestCase {

    private static final String FILE_NAME = "new_licence.doc";
    private File fileFromAsset;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        fileObject = getRandomFile();
        QBCustomObjectsFiles.uploadFile(fileObject, CLASS_NAME, NOTE_ID, FIELD_LICENSE, FILE_NAME, new QBCallbackImpl() {

            @Override
            public void onComplete(Result result) {
                file = ((QBFileUploadTaskResult) result).getFile();

            }

        });

    }


    public void testDownloadFile() {

        QBCustomObjectsFiles.downloadFile(CLASS_NAME, NOTE_ID, FIELD_LICENSE,  new QBCallbackImpl() {

            @Override
            public void onComplete(Result result) {
                checkHttpStatus(HttpStatus.SC_OK, result);
                checkIfSuccess(result);
                QBFileDownloadResult downloadResult = (QBFileDownloadResult) result;
                fileFromAsset = FileHelper.getFileFromAsset(downloadResult.getContentStream(), "filename.txt");
                assertEquals(fileFromAsset.length(), fileObject.length());
            }

        });

    }


    @Override

    public void tearDown() throws Exception {

        if (fileFromAsset != null) {
            QBCustomObjectsFiles.deleteFile(CLASS_NAME, NOTE_ID, FIELD_LICENSE, null);
        }

    }
}
