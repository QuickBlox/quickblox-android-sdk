package com.quickblox.sample.test.customobject;

import android.util.Log;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.internal.core.helper.StringifyArrayList;
import com.quickblox.module.custom.QBCustomObjects;
import com.quickblox.module.custom.model.QBCustomObject;
import com.quickblox.module.custom.result.QBCustomObjectDeletedResult;
import com.quickblox.module.custom.result.QBCustomObjectLimitedResult;
import com.quickblox.module.custom.result.QBCustomObjectResult;
import org.apache.http.HttpStatus;
import org.junit.AfterClass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * Created by vfite on 10.12.13.
 */
public class TestDeleteObject extends CustomObjectsTestCase {

    QBCustomObject note = getFakeObject();

    private StringifyArrayList<String> coIds = new StringifyArrayList();
    private LinkedList<QBCustomObject> qbCustomObjectList;

    @Override

    public void setUp() throws Exception {
        super.setUp();
    }

    public void createObjects(int size) {

        qbCustomObjectList = new LinkedList<QBCustomObject>();
        for(int i = 0; i < size; i++) {
            qbCustomObjectList.add(getFakeObject());
        }
        coIds.clear();
        QBCustomObjects.createObjects(qbCustomObjectList, new QBCallbackImpl() {


        @Override
        public void onComplete(Result result) {
            checkHttpStatus(HttpStatus.SC_CREATED, result);
            checkIfSuccess(result);
            ArrayList<QBCustomObject> responseObjects = ((QBCustomObjectLimitedResult) result).getCustomObjects();
            for (QBCustomObject qbCustomObject : responseObjects) {
                coIds.add(qbCustomObject.getCustomObjectId());
            }
        }

    });
    }


    public void testDeleteObjects() {

        final int COUNT_OBJ =3;

        createObjects(COUNT_OBJ);
        QBCustomObjects.deleteObjects(CLASS_NAME, coIds, new QBCallbackImpl() {

            @Override
            public void onComplete(Result result) {
                checkHttpStatus(HttpStatus.SC_OK, result);
                checkIfSuccess(result);
                QBCustomObjectDeletedResult qbCustomObjectDeletedResult = (QBCustomObjectDeletedResult) result;
                assertNotNull(qbCustomObjectDeletedResult);
                assertNotNull(qbCustomObjectDeletedResult.getDeleted());
                assertTrue(qbCustomObjectDeletedResult.getDeleted().size() ==  COUNT_OBJ);
                assertTrue(qbCustomObjectDeletedResult.getWrongPermissions().isEmpty());
                assertTrue(qbCustomObjectDeletedResult.getNotFound().isEmpty());
            }

        });

    }

    public void testDeleteObject() {

        QBCustomObjects.createObject(note, new QBCallbackImpl() {
            @Override
            public void onComplete(Result result) {
                checkHttpStatus(HttpStatus.SC_CREATED, result);
                checkIfSuccess(result);
            }
        });

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

        createObjects(1);
        String id = coIds.get(0);
        QBCustomObjects.deleteObject(CLASS_NAME, id, new QBCallbackImpl() {

            @Override
            public void onComplete(Result result) {
                checkHttpStatus(HttpStatus.SC_OK, result);
                checkIfSuccess(result);
                checkEmptyResponseBody(result);
            }

        });
    }

   /* @AfterClass
    public static void testCleanUp() {
        if (coIds != null && !coIds.isEmpty()) {
            QBCustomObjects.deleteObjects(CLASS_NAME, coIds, null);
        }
    }*/
}
