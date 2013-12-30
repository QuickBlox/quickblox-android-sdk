package com.quickblox.sample.test.customobject;

import android.util.Log;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.internal.core.helper.Lo;
import com.quickblox.internal.core.helper.StringifyArrayList;
import com.quickblox.module.custom.QBCustomObjects;
import com.quickblox.module.custom.model.QBCustomObject;
import com.quickblox.module.custom.model.QBPermissions;
import com.quickblox.module.custom.model.QBPermissionsLevel;
import com.quickblox.module.custom.result.QBCustomObjectLimitedResult;
import com.quickblox.module.custom.result.QBCustomObjectResult;
import org.apache.http.HttpStatus;
import org.junit.AfterClass;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: vfite
 * Date: 28.11.13
 * Time: 15:35
 * To change this template use File | Settings | File Templates.
 */
public class TestCreateCustomObject extends CustomObjectsTestCase {
    QBCustomObject note = getFakeObject();

    public static final String TAG = "createObjects";
    static public List<QBCustomObject> qbCustomObjectList;
    public static final String PARENT_ID = "51d816e0535c12d75f006537";
    Lo lo = new Lo(this);

    private static StringifyArrayList<String> coIds = new StringifyArrayList();

    @Override
    public void setUp() throws Exception {
        super.setUp();

        if (qbCustomObjectList == null || qbCustomObjectList.isEmpty()) {
            qbCustomObjectList = new LinkedList<QBCustomObject>();
            QBCustomObject fakeObject = getFakeObject();
            qbCustomObjectList.add(fakeObject);
            qbCustomObjectList.add(getFakeObject());
        }
    }


    public void testCreateNewObjects() {

        QBCustomObjects.createObjects(qbCustomObjectList, new QBCallbackImpl() {


            @Override
            public void onComplete(Result result) {
                checkHttpStatus(HttpStatus.SC_CREATED, result);
                checkIfSuccess(result);
                ArrayList<QBCustomObject> responseObjects = ((QBCustomObjectLimitedResult) result).getCustomObjects();
                for (QBCustomObject qbCustomObject:responseObjects) {
                    Log.d(TAG, "posted item=" + qbCustomObjectList.get(0).getFields().toString());
                    coIds.add(qbCustomObject.getCustomObjectId());
                    QBCustomObject objectFromCollection = getObjectFromCollection(qbCustomObjectList, FIELD_COMMENTS, (String) qbCustomObject.getFields().get(FIELD_COMMENTS));
                    assertEqualsObject(objectFromCollection, qbCustomObject);
                }
            }

        });

    }

    public void testCreateObject() {

        note.setParentId(PARENT_ID);
        QBCustomObjects.createObject(note, new QBCallbackImpl() {

            @Override
            public void onComplete(Result result) {
                checkHttpStatus(HttpStatus.SC_CREATED, result);
                checkIfSuccess(result);
                QBCustomObject newHero = ((QBCustomObjectResult) result).getCustomObject();
                coIds.add(newHero.getCustomObjectId());
                assertEquals(note.getClassName(), newHero.getClassName());
                assertEquals(note.getFields().get(FIELD_COMMENTS), newHero.getFields().get(FIELD_COMMENTS));
                assertEquals(note.getFields().get(FIELD_TITLE), newHero.getFields().get(FIELD_TITLE));
                assertEquals(note.getFields().get(FIELD_STATUS), newHero.getFields().get(FIELD_STATUS));
                assertNotNull(newHero.getParentId());
            }

        });

    }

    public void testCreateObjectWithPermissions() {
        QBPermissions qbPermissions = new QBPermissions();
        qbPermissions.setReadPermission(QBPermissionsLevel.OPEN);
        ArrayList<String> userIds = new ArrayList<String>();
        userIds.add("13163");
        qbPermissions.setDeletePermission(QBPermissionsLevel.OPEN_FOR_USER_IDS, userIds);
        qbPermissions.setUpdatePermission(QBPermissionsLevel.OWNER);
        note.setPermission(qbPermissions);
        QBCustomObjects.createObject(note, new QBCallbackImpl() {

            @Override

            public void onComplete(Result result) {
                checkHttpStatus(HttpStatus.SC_CREATED, result);
                checkIfSuccess(result);
                QBCustomObject newHero = ((QBCustomObjectResult) result).getCustomObject();
                lo.g(newHero.toString());
                coIds.add(newHero.getCustomObjectId());
                QBPermissions qbPermissions =    newHero.getPermission();
                assertNotNull(qbPermissions);
                assertNotNull(qbPermissions.getReadLevel());
                assertNotNull(qbPermissions.getUpdateLevel());
                assertNotNull(qbPermissions.getDeleteLevel() );
                assertEquals(qbPermissions.getReadLevel().getAccess(), QBPermissionsLevel.OPEN);
                assertEquals(qbPermissions.getDeleteLevel().getAccess(), QBPermissionsLevel.OPEN_FOR_USER_IDS);
                assertEquals(qbPermissions.getUpdateLevel().getAccess(), QBPermissionsLevel.OWNER);
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

