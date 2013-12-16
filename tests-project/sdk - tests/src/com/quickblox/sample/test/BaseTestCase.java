package com.quickblox.sample.test;

import android.test.InstrumentationTestCase;
import android.util.Log;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.QBSettings;
import com.quickblox.core.result.Result;
import com.quickblox.internal.core.helper.Lo;
import com.quickblox.internal.core.helper.StringUtils;
import com.quickblox.module.auth.QBAuth;
import com.quickblox.module.auth.result.QBSessionResult;
import com.quickblox.module.custom.model.QBCustomObject;
import com.quickblox.module.users.model.QBUser;
import org.apache.http.HttpStatus;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: vfite
 * Date: 28.11.13
 * Time: 14:08
 * To change this template use File | Settings | File Templates.
 */
public class BaseTestCase extends InstrumentationTestCase {
    private static final String TAG = "RESULT_RAW_BODY";
    Lo lo = new Lo(this.getClass().getSimpleName());

    private final int APP_ID = 99;
    private final String AUTH_KEY = "63ebrp5VZt7qTOv";
    private final String AUTH_SECRET = "YavMAxm5T59-BRw";

    private static String token;

    @Override
    protected void setUp() throws Exception {
        if (token == null) {
            QBSettings.getInstance().fastConfigInit(String.valueOf(APP_ID), AUTH_KEY, AUTH_SECRET);
            QBSettings.getInstance().setSynchronous(true);
            QBUser qbUser = new QBUser(TestConfig.USER_LOGIN, TestConfig.USER_PASSWORD);
            // authorize app with default user
            QBAuth.createSession(qbUser, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    checkHttpStatus(HttpStatus.SC_CREATED, result);
                    checkIfSuccess(result);
                    assertNotNull(((QBSessionResult) result).getSession().getToken());
                    token =            ((QBSessionResult) result).getSession().getToken();
                }
            });
        }
    }

    protected QBCustomObject getObjectFromCollection(List<QBCustomObject> qbCustomObjectsList, String key, String value){
        QBCustomObject returnObject = null;
        for (QBCustomObject qbCustomObject : qbCustomObjectsList) {
            if (value.equals(qbCustomObject.getFields().get(key))){
                returnObject =     qbCustomObject;
                break;
            }
        }
        return returnObject;
    }

    protected void checkEmptyResponseBody(Result result) {
        Log.i(TAG, result.getRawBody());
        if (StringUtils.isEmpty(result.getRawBody())) {
            assertTrue(true);
        } else {

            fail("response body should be empty");

        }

    }


    protected void checkIfSuccess(Result result) {

        if (result.isSuccess()) {

            assertTrue(true);

        } else {

            fail("this response should not contains error");

        }

    }


    protected void checkIfNotSuccess(Result result) {

        if (!result.isSuccess()) {

            assertTrue(true);

        } else {

            fail("this response should contains error");

        }

    }

    protected void assertError(String[] errors,Result result ){
        String error = result.getErrors().get(0);
        if(error == null){
            fail("error empty");
        }
        boolean condition = false;
        for (String msgErrror : errors) {
            condition =  msgErrror.equals(error);
            if(condition){
                break;
            }
        }
        assertTrue(condition);
    }

    protected void assertStatus(int[] statuses,Result result ){
        int statusCode = result.getStatusCode();
        boolean condition = false;
        for (int status : statuses) {
            condition = statusCode == status;
            if(condition){
                break;
            }
        }
        assertTrue(condition);
    }


    protected void checkHttpStatus(int successCode, Result result) {

        if (result.getStatusCode() == successCode) {

            assertTrue(true);

        } else {

            fail("result: " + result.getStatusCode()

                    + " but must be: " + successCode);

        }

    }

}
