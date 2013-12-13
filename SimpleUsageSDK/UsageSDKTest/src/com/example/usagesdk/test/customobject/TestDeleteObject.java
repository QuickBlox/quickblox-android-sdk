package com.example.usagesdk.test.customobject;

import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.internal.core.helper.StringifyArrayList;
import com.quickblox.module.custom.QBCustomObjects;
import com.quickblox.module.custom.model.QBCustomObject;
import com.quickblox.module.custom.result.QBCustomObjectResult;
import org.apache.http.HttpStatus;
import org.junit.AfterClass;

import java.util.Arrays;

/**
 * Created by vfite on 10.12.13.
 */
public class TestDeleteObject extends CustomObjectsTestCase {

    private static final String CLASS_NAME = "note";
    QBCustomObject note = getFakeObject();
    String[] IDS = new String[]{"50e3f85f535c123376000d31", "50e3f85f535c123376000d32"};

    private static StringifyArrayList<String> coIds = new StringifyArrayList();
    @Override

    public void setUp() throws Exception {
        super.setUp();
        QBCustomObjects.createObject(note, new QBCallbackImpl(){
            @Override
            public void onComplete(Result result) {
                checkHttpStatus(HttpStatus.SC_CREATED, result);
                checkIfSuccess(result);
                QBCustomObject newNote = ((QBCustomObjectResult) result).getCustomObject();
                coIds.add(newNote.getCustomObjectId());
            }
        });

    }


    public void testDeleteObjects() {

        StringifyArrayList<String> deleteIds = new StringifyArrayList<String>( Arrays.asList(IDS));
        QBCustomObjects.deleteObjects(CLASS_NAME, deleteIds, new QBCallbackImpl() {

            @Override
            public void onComplete(Result result) {
                checkHttpStatus(HttpStatus.SC_OK, result);
                checkIfSuccess(result);
                checkEmptyResponseBody(result);
            }

        });

    }

   public void testDeleteObject() {

        QBCustomObjects.deleteObject(note, new QBCallbackImpl() {

            @Override
            public void onComplete(Result result) {
                checkHttpStatus(HttpStatus.SC_OK, result);
                checkIfSuccess(result);
                checkEmptyResponseBody(result);

            }

        });

    }


    public void testDeleteObjectById() {

        String id = note.getCustomObjectId();

        QBCustomObjects.deleteObject(CLASS_NAME, id, new QBCallbackImpl() {

            @Override
            public void onComplete(Result result) {
                checkHttpStatus(HttpStatus.SC_OK, result);
                checkIfSuccess(result);
                checkEmptyResponseBody(result);
            }

        });
    }

    @AfterClass
    public static void testCleanUp(){
        if (coIds != null && !coIds.isEmpty()) {
            QBCustomObjects.deleteObjects(CLASS_NAME, coIds, null);
        }
    }
}
