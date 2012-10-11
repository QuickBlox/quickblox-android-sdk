package com.quickblox.android.framework.snippets.modules;

import android.content.Context;
import com.quickblox.android.framework.base.definitions.QBCallback;
import com.quickblox.android.framework.base.net.results.Result;
import com.quickblox.android.framework.modules.custom.models.QBCustomObject;
import com.quickblox.android.framework.modules.custom.net.results.QBCustomObjectResult;
import com.quickblox.android.framework.modules.custom.net.server.QBCustomObjects;
import com.quickblox.android.framework.snippets.Snippet;
import com.quickblox.android.framework.snippets.Snippets;

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
        snippets.add(getCustomObject);
        snippets.add(deleteCustomObject);
    }

    Snippet createCustomObject = new Snippet("create CO") {
        @Override
        public void execute() {
            QBCustomObject customObject = new QBCustomObject(className);
            customObject.put(fieldHealth, 99);
            customObject.put(fieldPower, 123.45);
            customObject.put(fieldGodMode, true);
            customObject.put(fieldName, "Zombie Boy");

            QBCustomObjects.createObject(customObject, new QBCallback() {
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

    Snippet getCustomObject = new Snippet("get CO") {
        @Override
        public void execute() {
            if (customObjectId != null) {
                QBCustomObject customObject = new QBCustomObject(className, customObjectId);

                QBCustomObjects.getObject(customObject, new QBCallback() {
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

    Snippet deleteCustomObject = new Snippet("delete CO") {
        @Override
        public void execute() {
            if (customObjectId != null) {
                QBCustomObject customObject = new QBCustomObject(className, customObjectId);

                QBCustomObjects.deleteObject(customObject, new QBCallback() {
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
}