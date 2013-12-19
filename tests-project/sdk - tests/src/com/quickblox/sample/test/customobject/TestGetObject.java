package com.quickblox.sample.test.customobject;

import android.util.Log;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.QBErrors;
import com.quickblox.core.result.Result;
import com.quickblox.internal.core.helper.Lo;
import com.quickblox.module.custom.QBCustomObjects;
import com.quickblox.module.custom.model.QBCustomObject;
import com.quickblox.module.custom.model.QBPermissions;
import com.quickblox.module.custom.result.QBCustomObjectPermissionResult;
import com.quickblox.module.custom.result.QBCustomObjectResult;
import com.quickblox.module.users.QBUsers;
import org.apache.http.HttpStatus;
import org.junit.AfterClass;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vfite on 10.12.13.
 */
public class TestGetObject extends CustomObjectsTestCase {
    private static final String TAG = "DELETE NOTE";
    static QBCustomObject note;// = getFakeObject();

    Lo lo = new Lo(this);

    public static final int[] ERROR_STATUSES = new int[]{HttpStatus.SC_NOT_FOUND, HttpStatus.SC_FORBIDDEN, HttpStatus.SC_UNPROCESSABLE_ENTITY};

    @Override
    public void setUp() throws Exception {
        super.setUp();
        if(note == null || note.getCustomObjectId()==null){
            note = getFakeObject();
            QBCustomObjects.createObject(note, null);
        }
    }


    public void testGetObject() {
        //note = new QBCustomObject(CLASS_NAME, NOTE_ID);
        QBCustomObjects.getObject(note, new QBCallbackImpl() {

            @Override
            public void onComplete(Result result) {
                checkHttpStatus(HttpStatus.SC_OK, result);
                checkIfSuccess(result);
                QBCustomObject newHero = ((QBCustomObjectResult) result).getCustomObject();
                assertEquals(note.getClassName(), newHero.getClassName());
                assertEquals(note.getFields().get(FIELD_TITLE), newHero.getFields().get(FIELD_TITLE));
                assertEquals(note.getFields().get(FIELD_COMMENTS), newHero.getFields().get(FIELD_COMMENTS));
                assertEquals(note.getFields().get(FIELD_STATUS), newHero.getFields().get(FIELD_STATUS));

            }

        });

    }


    public void testGetObjectWithPermissions() {
        //note = new QBCustomObject(CLASS_NAME, NOTE_ID);
        QBCustomObjects.getObjectPermissions(CLASS_NAME, note.getCustomObjectId(), new QBCallbackImpl() {

            @Override
            public void onComplete(Result result) {
                checkHttpStatus(HttpStatus.SC_OK, result);
                checkIfSuccess(result);
                QBPermissions qbPermissions = ((QBCustomObjectPermissionResult) result).getPermissions();
                lo.g(qbPermissions.toString());
                assertNotNull(qbPermissions.getReadLevel());
                assertNotNull(qbPermissions.getUpdateLevel());
                assertNotNull(qbPermissions.getDeleteLevel() );
            }

        });

    }

    public void testGetObjectSpecifyOutputParams() {

        List<Object> outputParams = new ArrayList<Object>();
        outputParams.add(FIELD_TITLE);
        outputParams.add(FIELD_STATUS);
        QBCustomObjects.getObject(note, outputParams, new QBCallbackImpl() {

            @Override
            public void onComplete(Result result) {
                checkHttpStatus(HttpStatus.SC_OK, result);
                checkIfSuccess(result);
                QBCustomObject newHero = ((QBCustomObjectResult) result).getCustomObject();
                assertEquals(note.getClassName(), newHero.getClassName());
                assertEquals(note.getFields().get(FIELD_TITLE), newHero.getFields().get(FIELD_TITLE));
                assertEquals(note.getFields().get(FIELD_STATUS), newHero.getFields().get(FIELD_STATUS));
                assertNull( newHero.getFields().get(FIELD_COMMENTS));
                assertNull(newHero.getFields().get(FIELD_LICENSE));

            }

        });

    }


    public void testGetObjectByIdIncorrectClassNameAndId() {

        QBCustomObjects.getObject("nonexistentClass", "nonexistentId", new QBCallbackImpl() {

            @Override
            public void onComplete(Result result) {
                assertStatus(ERROR_STATUSES, result);
                checkIfNotSuccess(result);
                assertError(new String[]{QBErrors.UNDEFINED_CLASS, QBErrors.RESOURCE_NOT_FOUND}, result);
            }

        });

    }


    public void testGetObjectByIdIncorrectId() {

        QBCustomObjects.getObject(CLASS_NAME, "nonexistentId", new QBCallbackImpl() {

            @Override
            public void onComplete(Result result) {
                assertStatus(ERROR_STATUSES, result);
                checkIfNotSuccess(result);
                assertError(new String[]{QBErrors.UNDEFINED_CLASS, QBErrors.RESOURCE_NOT_FOUND}, result);
            }

        });

    }



    public void testGetObjectById() {

        String id = note.getCustomObjectId();
        QBCustomObjects.getObject(CLASS_NAME, id, new QBCallbackImpl() {

            @Override

            public void onComplete(Result result) {
                checkHttpStatus(HttpStatus.SC_OK, result);
                checkIfSuccess(result);
                QBCustomObject newHero = ((QBCustomObjectResult) result).getCustomObject();
                assertEquals(note.getClassName(), newHero.getClassName());
                assertEqualsObject(note, newHero);
            }

        });

    }

    @AfterClass
    public static void testCleanUp(){
                     if(note!=null && note.getCustomObjectId()!=null){
                         QBCustomObjects.deleteObject(note, null);
                     }
    }

}
