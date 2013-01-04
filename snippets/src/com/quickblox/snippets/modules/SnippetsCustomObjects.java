package com.quickblox.snippets.modules;

import android.content.Context;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.module.custom.QBCustomObjects;
import com.quickblox.module.custom.model.QBCustomObject;
import com.quickblox.module.custom.result.QBCustomObjectLimitedResult;
import com.quickblox.module.custom.result.QBCustomObjectResult;
import com.quickblox.module.custom.result.QBCustomObjectTaskResult;
import com.quickblox.snippets.Snippet;
import com.quickblox.snippets.Snippets;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * User: Oleg Soroka
 * Date: 11.10.12
 * Time: 12:46
 */
public class SnippetsCustomObjects extends Snippets {

    // Define custom object model in QB Admin Panel
    // http://image.quickblox.com/3f71573f1fd8b23a1e375b904a80.injoit.png
    String className = "hero";
    String fieldHealth = "health";
    String fieldPower = "power";

    public SnippetsCustomObjects(Context context) {
        super(context);

        snippets.add(createCustomObject);
        snippets.add(getCustomObjectById);
        snippets.add(deleteCustomObject);
        snippets.add(getCustomObjects);
        snippets.add(updateCustomObject);
        snippets.add(getGetCustomObjectsByIds);
    }

    Snippet getCustomObjects = new Snippet("get objects") {
        @Override
        public void execute() {
            QBCustomObjects.getObjects(className, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBCustomObjectLimitedResult coresult = (QBCustomObjectLimitedResult) result;

                        ArrayList<QBCustomObject> co = coresult.getCustomObjects();
                        System.out.println(">>> custom object list: " + co.toString());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet createCustomObject = new Snippet("create object") {
        @Override
        public void execute() {
            QBCustomObject customObject = new QBCustomObject(className);
            customObject.put(fieldHealth, 99);
            customObject.put(fieldPower, 123.45);
            customObject.setParentId("50d9bf2d535c12344701c43a");

            QBCustomObjects.createObject(customObject, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {

                    if (result.isSuccess()) {
                        QBCustomObjectResult customObjectResult = (QBCustomObjectResult) result;
                        QBCustomObject newCustomObject = customObjectResult.getCustomObject();

                        System.out.println(">>> custom object: " + newCustomObject);
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet getGetCustomObjectsByIds = new Snippet("get custom objects by ids") {
        @Override
        public void execute() {

            ArrayList<String> coIDs = new ArrayList<String>();
            coIDs.add("50e67e6e535c121c66004c74");
            coIDs.add("50e67e6d535c127f66004f47");
            coIDs.add("50e67e6b535c121c66004c72");
            coIDs.add("50e59f81535c121c660015fd");

            QBCustomObjects.getObjectsByIdsTask(className, coIDs, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBCustomObjectTaskResult taskResult = (QBCustomObjectTaskResult) result;

                        System.out.format(">>> custom objects: " + taskResult.getCustomObjects().toString());
                    }
                }
            });
        }
    };

    Snippet getCustomObjectById = new Snippet("get object") {
        @Override
        public void execute() {
            QBCustomObject customObject = new QBCustomObject(className, "50e3f8c7535c126073000d52");

            QBCustomObjects.getObject(customObject, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBCustomObjectResult customObjectResult = (QBCustomObjectResult) result;
                        QBCustomObject newCustomObject = customObjectResult.getCustomObject();

                        System.out.println(">>> custom object: " + newCustomObject);
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet deleteCustomObject = new Snippet("delete object") {
        @Override
        public void execute() {
            QBCustomObject customObject = new QBCustomObject(className, "af3514342afbbb3555");

            QBCustomObjects.deleteObject(customObject, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        System.out.println(">>> custom object deleted OK");
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet updateCustomObject = new Snippet("update object") {
        @Override
        public void execute() {
            QBCustomObject co = new QBCustomObject();
            co.setClassName(className);
            HashMap<String, Object> fields = new HashMap<String, Object>();
            fields.put(fieldPower, 1);
            fields.put(fieldHealth, 10);
            co.setFields(fields);
            co.setCustomObjectId("50e3f85f535c123376000d31");

            QBCustomObjects.updateObject(co, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBCustomObjectResult updateResult = (QBCustomObjectResult) result;

                        System.out.println(">>> updatedAt: " + updateResult.getCustomObject().getUpdatedAt());
                        System.out.println(">>> createdAt: " + updateResult.getCustomObject().getCreatedAt());

                        System.out.println(">>> co : " + updateResult.getCustomObject().toString());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };
}