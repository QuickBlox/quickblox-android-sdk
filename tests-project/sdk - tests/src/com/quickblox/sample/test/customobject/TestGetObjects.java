package com.quickblox.sample.test.customobject;

import android.util.Log;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.internal.core.helper.StringifyArrayList;
import com.quickblox.internal.module.custom.Consts;
import com.quickblox.internal.module.custom.request.QBCustomObjectRequestBuilder;
import com.quickblox.module.custom.QBCustomObjects;
import com.quickblox.module.custom.model.QBCustomObject;
import com.quickblox.module.custom.result.QBCustomObjectCountResult;
import com.quickblox.module.custom.result.QBCustomObjectLimitedResult;
import com.quickblox.module.users.QBUsers;
import org.apache.http.HttpStatus;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vfite on 10.12.13.
 */
public class TestGetObjects extends CustomObjectsTestCase {

    private static final String TAG = "TestGetObjects";
    static int objectsCount = 3;

    public static final String BOOK_CLASS_NAME = "Book";


    static ArrayList<QBCustomObject> objects = new ArrayList<QBCustomObject>();
    static StringifyArrayList<String> coIds = new StringifyArrayList<String>();


    @BeforeClass
    public static void testBefore() {
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        if (objects == null || objects.isEmpty()) {
            for (int i = 0; i < objectsCount; i++) {
                QBCustomObject hero = getBookFakeObject();
                objects.add(hero);
            }

            QBCustomObjects.createObjects(objects, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    checkIfSuccess(result);
                    QBCustomObjectLimitedResult customObjectResult = (QBCustomObjectLimitedResult) result;
                    for (QBCustomObject qbCustomObject : customObjectResult.getCustomObjects()) {
                        coIds.add(qbCustomObject.getCustomObjectId());
                    }
                }
            });
        }
    }


   public void testGetObject() {
        QBCustomObjects.getObjects(BOOK_CLASS_NAME, new QBCallbackImpl() {

            @Override
            public void onComplete(Result result) {
                checkHttpStatus(HttpStatus.SC_OK, result);
                checkIfSuccess(result);
                QBCustomObjectLimitedResult customObjectResult = (QBCustomObjectLimitedResult) result;
                ArrayList<QBCustomObject> customObjectsList = customObjectResult.getCustomObjects();
                assertEquals(objects.size(), customObjectsList.size());
                for (QBCustomObject co : customObjectsList) {
                    assertTrue(containsById(coIds, co));

                }
            }

        });

    }

    public void testGetObjectSpecifyOutput() {

        List<Object> outputParams = new ArrayList<Object>();
        outputParams.add(BK_FIELD_TITLE);
        outputParams.add(BK_FIELD_AUTH);
        outputParams.add(BK_FIELD_ISBN);
        QBCustomObjectRequestBuilder qbCustomObjectRequestBuilder = new QBCustomObjectRequestBuilder();
        qbCustomObjectRequestBuilder.in(BK_FIELD_AUTH, "v0nmjmk9up", "s3sadv3jgu" );
        qbCustomObjectRequestBuilder.output(outputParams);
        QBCustomObjects.getObjects(BOOK_CLASS_NAME, qbCustomObjectRequestBuilder, new QBCallbackImpl() {

            @Override
            public void onComplete(Result result) {
                checkHttpStatus(HttpStatus.SC_OK, result);
                checkIfSuccess(result);
                QBCustomObjectLimitedResult customObjectResult = (QBCustomObjectLimitedResult) result;
                ArrayList<QBCustomObject> customObjectsList = customObjectResult.getCustomObjects();
//                assertEquals(objects.size(), customObjectsList.size());
                for (QBCustomObject co : customObjectsList) {
                    assertNotNull(co.getFields().get(BK_FIELD_TITLE));
                    assertNotNull(co.getFields().get(BK_FIELD_AUTH));
                    assertNotNull(co.getFields().get(BK_FIELD_ISBN));
                    assertNull(co.getFields().get(BK_FIELD_IMG));
                    assertNull(co.getFields().get(Consts.ENTITY_FIELD_CREATED_AT));
                }
            }

        });

    }

    public void testGetObjectsWithAdditionalParams() {

        List<Object> outputParams = new ArrayList<Object>();
        outputParams.add(BK_FIELD_TITLE);
        outputParams.add(BK_FIELD_AUTH);
        outputParams.add(BK_FIELD_ISBN);
        QBCustomObjectRequestBuilder qbCustomObjectRequestBuilder = new QBCustomObjectRequestBuilder();
        //qbCustomObjectRequestBuilder.or(BK_FIELD_AUTH, "v0nmjmk9up");
        //qbCustomObjectRequestBuilder.or(BK_FIELD_ISBN, 295);
        qbCustomObjectRequestBuilder.ctn(BK_FIELD_TITLE, "2");
        qbCustomObjectRequestBuilder.output(outputParams);
        QBCustomObjects.getObjects(BOOK_CLASS_NAME, qbCustomObjectRequestBuilder, new QBCallbackImpl() {

            @Override
            public void onComplete(Result result) {
                checkHttpStatus(HttpStatus.SC_OK, result);
                checkIfSuccess(result);
                QBCustomObjectLimitedResult customObjectResult = (QBCustomObjectLimitedResult) result;
                assertNotNull(customObjectResult);
                ArrayList<QBCustomObject> customObjectsList = customObjectResult.getCustomObjects();
                assertNotNull(customObjectsList);
                for (QBCustomObject co : customObjectsList) {
                    assertNotNull(co.getFields().get(BK_FIELD_TITLE));
                    assertNotNull(co.getFields().get(BK_FIELD_AUTH));
                    assertNotNull(co.getFields().get(BK_FIELD_ISBN));
                    assertNull(co.getFields().get(BK_FIELD_IMG));
                    assertNull(co.getFields().get(Consts.ENTITY_FIELD_CREATED_AT));
                }
            }

        });

    }


    public void testGetObjects() {

        QBCustomObjects.getObjects(BOOK_CLASS_NAME, new QBCallbackImpl() {

            @Override
            public void onComplete(Result result) {
                checkHttpStatus(HttpStatus.SC_OK, result);
                checkIfSuccess(result);
                QBCustomObjectLimitedResult customObjectResult = (QBCustomObjectLimitedResult) result;
                ArrayList<QBCustomObject> customObjectsList = customObjectResult.getCustomObjects();
                assertEquals(objects.size(), customObjectsList.size());
                for (QBCustomObject co : customObjectsList) {
                    assertTrue(containsById(coIds, co));

                }
            }

        });

    }


    public void testCountObjects() {

        QBCustomObjects.countObjects(BOOK_CLASS_NAME, new QBCallbackImpl() {

            @Override
            public void onComplete(Result result) {
                checkHttpStatus(HttpStatus.SC_OK, result);
                checkIfSuccess(result);
                QBCustomObjectCountResult countResult = (QBCustomObjectCountResult) result;
                int count = countResult.getCount();
                assertEquals(objects.size(), count);

            }

        });

    }


    public void testLimitedQuery() {
        final Integer limit = 1;
        final Integer skip = 1;

        QBCustomObjectRequestBuilder requestBuilder = new QBCustomObjectRequestBuilder();
        requestBuilder.setPagesLimit(limit);
        requestBuilder.setPagesSkip(skip);
        QBCustomObjects.getObjects(BOOK_CLASS_NAME, requestBuilder, new QBCallbackImpl() {

            @Override
            public void onComplete(Result result) {
                checkHttpStatus(HttpStatus.SC_OK, result);
                checkIfSuccess(result);
                QBCustomObjectLimitedResult customObjectLimitedResult = (QBCustomObjectLimitedResult) result;
                ArrayList<QBCustomObject> customObjectsList = customObjectLimitedResult.getCustomObjects();
                assertEquals(limit, new Integer(customObjectsList.size()));
                assertEquals(limit, customObjectLimitedResult.getPagesLimit());
                assertEquals(skip, customObjectLimitedResult.getPagesSkip());

            }

        });

    }


    private boolean containsById(StringifyArrayList<String> list, QBCustomObject qbCustomObject) {

        boolean contains = false;
        for (String coId : list) {
            if (qbCustomObject.getCustomObjectId().equals(coId)) {
                contains = true;
                break;
            }

        }
        return contains;

    }

    @AfterClass
    public static void testCleanUp() {
        if (coIds != null && !coIds.isEmpty()) {
            QBCustomObjects.deleteObjects(BOOK_CLASS_NAME, coIds, null);
        }
    }


    /*@Override

    public void tearDown() throws Exception {
        QBCustomObjects.deleteObjects(BOOK_CLASS_NAME, coIds, null);
    }*/
}
