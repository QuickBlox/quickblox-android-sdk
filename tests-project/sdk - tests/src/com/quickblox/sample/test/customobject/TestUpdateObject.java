package com.quickblox.sample.test.customobject;

import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.internal.module.custom.request.QBCustomObjectUpdateBuilder;
import com.quickblox.module.custom.QBCustomObjects;
import com.quickblox.module.custom.model.QBCustomObject;
import com.quickblox.module.custom.result.QBCustomObjectLimitedResult;
import com.quickblox.module.custom.result.QBCustomObjectResult;
import org.apache.http.HttpStatus;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by vfite on 10.12.13.
 */
public class TestUpdateObject extends CustomObjectsTestCase {

    private static final String TAG = "update objects";
    QBCustomObject note = getFakeObject();

    public static String UPDATED_NOTE_ID= "52ab1322bf7b772b775b26a0";
    public static String UPDATED_NOTE_SINGLE_ID= "51d816e0535c12d75f006537";

    private List<QBCustomObject> qbCustomObjectList;

    public static String NOTE_ID1 = "51d816e0535c12d75f006537";
    public static String NOTE_ID2 = "51dbd0bd535c12b46d0036ce";
    private Integer isbn;
    private QBCustomObject noteforinc;

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }


    public void testUpdateObjectWithFilters() {


        QBCustomObjects.getObject(BOOK_CLASS_NAME, UPDATED_NOTE_ID, new QBCallbackImpl(){


            @Override
            public void onComplete(Result result) {
                noteforinc = ((QBCustomObjectResult) result).getCustomObject();
                isbn = Integer.parseInt((String) noteforinc.getFields().get(BK_FIELD_ISBN));
            }
        }) ;

        final int INC_VALUE =  100;
        QBCustomObjectUpdateBuilder qbCustomObjectUpdateBuilder = new QBCustomObjectUpdateBuilder();
        qbCustomObjectUpdateBuilder.inc(BK_FIELD_ISBN, INC_VALUE);

        noteforinc.setCustomObjectId(UPDATED_NOTE_ID);
        QBCustomObjects.updateObject(noteforinc, qbCustomObjectUpdateBuilder, new QBCallbackImpl() {

            @Override
            public void onComplete(Result result) {
                checkHttpStatus(HttpStatus.SC_OK, result);
                checkIfSuccess(result);
                QBCustomObject newnote = ((QBCustomObjectResult) result).getCustomObject();
                assertEqualsObject(newnote, noteforinc);
                int isbn = Integer.parseInt((String) newnote.getFields().get(BK_FIELD_ISBN));
                assertEquals((TestUpdateObject.this.isbn + INC_VALUE), isbn);
            }

        });

    }

    public void testUpdateObject() {
        note.put(FIELD_STATUS, types[1]);
        note.setCustomObjectId(UPDATED_NOTE_SINGLE_ID);
        QBCustomObjects.updateObject(note, new QBCallbackImpl() {

            @Override
            public void onComplete(Result result) {
                checkHttpStatus(HttpStatus.SC_OK, result);
                checkIfSuccess(result);
                QBCustomObject newnote = ((QBCustomObjectResult) result).getCustomObject();
                assertEqualsObject(newnote, note);
            }

        });

    }

    public void testUpdateNewObjects() {

        qbCustomObjectList = new LinkedList<QBCustomObject>();
        QBCustomObject fakeObject = getFakeObject();
        fakeObject.setCustomObjectId(NOTE_ID1);
        fakeObject.setParentId("a1098372910830921dsd");
        qbCustomObjectList.add(fakeObject);
        QBCustomObject fakeObject1 = getFakeObject();
        fakeObject1.setCustomObjectId(NOTE_ID2);
        qbCustomObjectList.add(fakeObject1);
        QBCustomObjects.updateObjects(qbCustomObjectList, new QBCallbackImpl() {


            @Override

            public void onComplete(Result result) {
                checkHttpStatus(HttpStatus.SC_OK, result);
                checkIfSuccess(result);
                ArrayList<QBCustomObject> responseObjects = ((QBCustomObjectLimitedResult) result).getCustomObjects();
                for (QBCustomObject qbCustomObject :responseObjects) {
                    QBCustomObject objectFromCollection = getObjectFromCollection(qbCustomObjectList, FIELD_COMMENTS, (String) qbCustomObject.getFields().get(FIELD_COMMENTS));
                    assertEqualsObject(objectFromCollection, qbCustomObject);
                }
            }

        });

    }


    @Override

    public void tearDown() throws Exception {

       // QBCustomObjects.deleteObject(note, null);

    }
}
