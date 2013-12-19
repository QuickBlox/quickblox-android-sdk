package com.quickblox.sample.test.customobject;

import com.quickblox.module.custom.model.QBCustomObject;
import com.quickblox.sample.test.BaseTestCase;
import com.quickblox.sample.test.faker.NumberFaker;
import com.quickblox.sample.test.faker.StringFaker;

/**
 * Created with IntelliJ IDEA.
 * User: vfite
 * Date: 28.11.13
 * Time: 14:28
 * To change this template use File | Settings | File Templates.
 */
public class CustomObjectsTestCase extends BaseTestCase {


    public static String BK_FIELD_TITLE= "name";
    public static String BK_FIELD_AUTH = "Author";
    public static String BK_FIELD_ISBN = "ISBN";
    public static String BK_FIELD_IMG = "Image";

    public static final String BOOK_CLASS_NAME = "Book";

    String[] types = {"New", "In Process"};
    public static String CLASS_NAME = "note";
    public static String FIELD_TITLE= "title";
    public static String FIELD_STATUS = "status";
    public static String FIELD_LICENSE = "license";
    public static String FIELD_COMMENTS = "comments";
    public static String NOTE_ID = "50e3f85f535c123376000d31";


    public QBCustomObject getFakeObject() {

        QBCustomObject note = new QBCustomObject(CLASS_NAME);
        String title = StringFaker.getRandomString(10);
        String comments = StringFaker.getRandomString(10);
        String type = StringFaker.getRandomFromArray(types);
        note.put(FIELD_TITLE, title);
        note.put(FIELD_COMMENTS, comments);
        note.put(FIELD_STATUS, type);

        return note;

    }

    public QBCustomObject getBookFakeObject() {

        QBCustomObject book = new QBCustomObject(BOOK_CLASS_NAME);
        String title = StringFaker.getRandomString(10);
        String author = StringFaker.getRandomString(10);
        Integer isbn = NumberFaker.getInt(1000);
        book.put(BK_FIELD_TITLE, title);
        book.put(BK_FIELD_AUTH, author);
        book.put(BK_FIELD_ISBN, isbn);

        return book;

    }


    protected void assertEqualsObject(QBCustomObject qbCustomObjectRequest, QBCustomObject qbCustomObjectResponse) {
        assertEquals(qbCustomObjectRequest.getClassName(), qbCustomObjectResponse.getClassName());

        assertEquals(qbCustomObjectRequest.getFields().get(FIELD_COMMENTS), qbCustomObjectResponse.getFields().get(FIELD_COMMENTS));

        assertEquals(qbCustomObjectRequest.getFields().get(FIELD_TITLE), qbCustomObjectResponse.getFields().get(FIELD_TITLE));

        assertEquals(qbCustomObjectRequest.getFields().get(FIELD_STATUS), qbCustomObjectResponse.getFields().get(FIELD_STATUS));

    }

}
