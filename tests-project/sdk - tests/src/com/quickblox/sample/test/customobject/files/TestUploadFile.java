package com.quickblox.sample.test.customobject.files;

import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.module.custom.QBCustomObjectsFiles;
import com.quickblox.module.custom.model.QBCustomObject;
import com.quickblox.module.custom.model.QBCustomObjectFileField;
import com.quickblox.module.custom.result.QBCOFileUploadResult;
import org.apache.http.HttpStatus;

/**
 * Created by vfite on 11.12.13.
 */
public class TestUploadFile extends TestFileTestCase {

    private QBCustomObjectFileField customObjectFileField;


    @Override
    public void setUp() throws Exception {
        super.setUp();
        fileObject = getRandomFile();
    }


    public void testUploadCOFile() {
        QBCustomObject qbCustomObject = new QBCustomObject(CLASS_NAME, FILE_UPLOAD_NOTE_ID);
        QBCustomObjectsFiles.uploadFile(fileObject, qbCustomObject, FIELD_LICENSE, new QBCallbackImpl() {
            @Override
            public void onComplete(Result result) {
                    checkHttpStatus(HttpStatus.SC_OK, result);
                    checkIfSuccess(result);
                    customObjectFileField = ((QBCOFileUploadResult) result).getCustomObjectFileField();
                    assertEquals(fileObject.getName(), customObjectFileField.getFileName());
                    assertTrue(customObjectFileField.getSize() > 0);
                    assertEquals(((int)fileObject.length()), customObjectFileField.getSize());
                    assertNotNull( customObjectFileField.getId());
                    assertNotNull(customObjectFileField.getContentType());
                    assertNotNull( customObjectFileField.getFileId());
            }
        });
    }

    @Override
    public void tearDown() throws Exception {
        if (customObjectFileField != null) {
            QBCustomObjectsFiles.deleteFile(CLASS_NAME, FILE_UPLOAD_NOTE_ID, FIELD_LICENSE, null);
        }
    }
}
