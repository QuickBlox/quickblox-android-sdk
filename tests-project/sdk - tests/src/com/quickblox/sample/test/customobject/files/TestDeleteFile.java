package com.quickblox.sample.test.customobject.files;

import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.module.custom.QBCustomObjectsFiles;
import com.quickblox.module.custom.model.QBCustomObjectFileField;
import com.quickblox.module.custom.result.QBCOFileUploadResult;
import org.apache.http.HttpStatus;

/**
 * Created by vfite on 12.12.13.
 */
public class TestDeleteFile extends TestFileTestCase {

    private QBCustomObjectFileField customObjectFileField;

    @Override

    public void setUp() throws Exception {
        super.setUp();
        fileObject = getRandomFile();
        QBCustomObjectsFiles.uploadFile(fileObject, CLASS_NAME, NOTE_ID, FIELD_LICENSE, "license.url", new QBCallbackImpl() {

            @Override
            public void onComplete(Result result) {
                customObjectFileField = ((QBCOFileUploadResult) result).getCustomObjectFileField();
            }

        });

    }


    public void testDeleteFile() {

        QBCustomObjectsFiles.deleteFile(CLASS_NAME, customObjectFileField.getCustomObjectId(), FIELD_LICENSE, new QBCallbackImpl() {

            @Override
            public void onComplete(Result result) {
                checkHttpStatus(HttpStatus.SC_OK, result);
                checkIfSuccess(result);
                checkEmptyResponseBody(result);
            }

        });

    }
}
