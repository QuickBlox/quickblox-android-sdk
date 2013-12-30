package com.quickblox.sample.test.customobject.files;

import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.internal.core.helper.FileHelper;
import com.quickblox.module.content.result.QBFileDownloadResult;
import com.quickblox.module.content.result.QBFileUploadTaskResult;
import com.quickblox.module.custom.QBCustomObjects;
import com.quickblox.module.custom.QBCustomObjectsFiles;
import com.quickblox.module.custom.model.QBCustomObjectFileField;
import com.quickblox.module.custom.result.QBCOFileUploadResult;
import org.apache.http.HttpStatus;
import org.junit.AfterClass;

import java.io.File;

/**
 * Created by vfite on 12.12.13.
 */
public class TestDownloadFile extends TestFileTestCase {

    private static final String FILE_NAME = "new_licence.doc";
    private static File fileFromAsset;
    //private static File file;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        fileObject = getRandomFile();
        QBCustomObjectsFiles.uploadFile(fileObject, CLASS_NAME, FILE_UPLOAD_NOTE_ID, FIELD_LICENSE, new QBCallbackImpl() {

            @Override
            public void onComplete(Result result) {
                QBCustomObjectFileField customObjectFileField = ((QBCOFileUploadResult) result).getCustomObjectFileField();

            }

        });

    }


    public void testDownloadFile() {

        QBCustomObjectsFiles.downloadFile(CLASS_NAME, FILE_UPLOAD_NOTE_ID, FIELD_LICENSE,  new QBCallbackImpl() {

            @Override
            public void onComplete(Result result) {
                checkHttpStatus(HttpStatus.SC_OK, result);
                checkIfSuccess(result);
                QBFileDownloadResult downloadResult = (QBFileDownloadResult) result;
                fileFromAsset = FileHelper.getFileFromAsset(downloadResult.getContentStream(), "filename.txt", SD_CARD_TEST_ROOT);
                assertEquals(fileFromAsset.length(), fileObject.length());
            }

        });

    }

    @Override

    public void tearDown() throws Exception {

        if (fileFromAsset != null) {
            QBCustomObjectsFiles.deleteFile(CLASS_NAME, FILE_UPLOAD_NOTE_ID, FIELD_LICENSE, null);
        }

    }
}
