package com.quickblox.snippets.modules;

import android.content.Context;
import com.quickblox.core.QBCallback;
import com.quickblox.module.custom.result.QBCustomObjectLimitedResult;
import com.quickblox.snippets.Snippet;
import com.quickblox.snippets.Snippets;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.module.custom.QBCustomObjects;
import com.quickblox.module.custom.model.QBCustomObject;
import com.quickblox.module.custom.result.QBCustomObjectResult;

import java.util.ArrayList;

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
    String fieldGodMode = "god_mode";
    String fieldName = "name";

    String customObjectId = null;

    public SnippetsCustomObjects(Context context) {
        super(context);

        snippets.add(createCustomObject);
        snippets.add(getCustomObjectById);
        snippets.add(deleteCustomObject);
        snippets.add(getCustomObjects);
        snippets.add(updateCustomObject);
    }

    Snippet getCustomObjects = new Snippet("get objects") {
        @Override
        public void execute() {
            QBCustomObjects.getObjects(className, new QBCallback() {
                @Override
                public void onComplete(Result result) {
                    printResultToConsole(result);
                    ArrayList<QBCustomObject> co = ((QBCustomObjectLimitedResult) result).getCustomObjects();
                }

                @Override
                public void onComplete(Result result, Object context) {

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

            QBCustomObjects.createObject(customObject, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    printResultToConsole(result);

                    if (result.isSuccess()) {
                        QBCustomObjectResult customObjectResult = (QBCustomObjectResult) result;
                        QBCustomObject newCustomObject = customObjectResult.getCustomObject();

                        System.out.println(">>> custom object: " + newCustomObject);

                        customObjectId = newCustomObject.getCustomObjectId();
                    }
                }
            });
        }
    };

    Snippet getCustomObjectById = new Snippet("get object") {
        @Override
        public void execute() {
            if (customObjectId != null) {
                QBCustomObject customObject = new QBCustomObject(className, customObjectId);

                QBCustomObjects.getObject(customObject, new QBCallbackImpl() {
                    @Override
                    public void onComplete(Result result) {
                        printResultToConsole(result);

                        if (result.isSuccess()) {
                            QBCustomObjectResult customObjectResult = (QBCustomObjectResult) result;
                            QBCustomObject newCustomObject = customObjectResult.getCustomObject();

                            System.out.println(">>> custom object: " + newCustomObject);
                        }
                    }
                });
            } else {
                System.out.println(">>> Create Custom Object before retrieving.");
            }
        }
    };

    Snippet deleteCustomObject = new Snippet("delete object") {
        @Override
        public void execute() {
            if (customObjectId != null) {
                QBCustomObject customObject = new QBCustomObject(className, customObjectId);

                QBCustomObjects.deleteObject(customObject, new QBCallbackImpl() {
                    @Override
                    public void onComplete(Result result) {
                        printResultToConsole(result);
                    }
                });
            } else {
                System.out.println(">>> Create Custom Object before deleting.");
            }
        }
    };

    Snippet updateCustomObject = new Snippet("update object") {
        @Override
        public void execute() {
            QBCustomObject co = new QBCustomObject();
            co.setClassName(className);
            co.setCustomObjectId(customObjectId);

            QBCustomObjects.updateObject(co, new QBCallback() {
                @Override
                public void onComplete(Result result) {
                    printResultToConsole(result);
                }

                @Override
                public void onComplete(Result result, Object context) {
                }
            });
        }
    };
}