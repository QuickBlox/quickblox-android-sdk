package com.quickblox.snippets.modules;

import android.content.Context;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.internal.core.helper.GenericQueryRule;
import com.quickblox.internal.module.custom.request.QBCustomObjectRequestBuilder;
import com.quickblox.internal.module.custom.request.QBCustomObjectUpdateBuilder;
import com.quickblox.internal.module.custom.request.QueryRule;
import com.quickblox.module.custom.QBCustomObjects;
import com.quickblox.module.custom.model.QBCustomObject;
import com.quickblox.module.custom.result.QBCustomObjectLimitedResult;
import com.quickblox.module.custom.result.QBCustomObjectResult;
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
    String fieldTags = "tags";


    public SnippetsCustomObjects(Context context) {
        super(context);

        snippets.add(createCustomObject);
        snippets.add(getCustomObjectById);
        snippets.add(deleteCustomObject);

        snippets.add(updateCustomObject);
        snippets.add(updateCustomObjectWithSpecialUpdateParameters);

        snippets.add(getCustomObjects);
        snippets.add(getCustomObjectsWithParams);
    }

    Snippet createCustomObject = new Snippet("create object") {
        @Override
        public void execute() {
            QBCustomObject customObject = new QBCustomObject(className);
            customObject.put(fieldHealth, 99);
            customObject.put(fieldPower, 123.45);
            ArrayList<String> tags = new ArrayList<String>();
            tags.add("man");
            tags.add("car");
            customObject.put(fieldTags, tags);
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

    Snippet getCustomObjectById = new Snippet("get object") {
        @Override
        public void execute() {
            QBCustomObject customObject = new QBCustomObject(className, "50e59f81535c121c660015fd");

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
            QBCustomObject customObject = new QBCustomObject(className, "50e59dc7535c121f6600155b");

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
            co.setCustomObjectId("50e59f81535c121c660015fd");
            //
            HashMap<String, Object> fields = new HashMap<String, Object>();
            fields.put(fieldPower, 1);
            fields.put(fieldHealth, 10);
            co.setFields(fields);

            QBCustomObjects.updateObject(co, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBCustomObjectResult updateResult = (QBCustomObjectResult) result;

                        System.out.println(">>> co : " + updateResult.getCustomObject().toString());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet updateCustomObjectWithSpecialUpdateParameters = new Snippet("update object", "with special update parameters") {
        @Override
        public void execute() {
            QBCustomObject co = new QBCustomObject();
            co.setClassName(className);
            co.setCustomObjectId("50e59f81535c121c660015fd");
            //
            HashMap<String, Object> fields = new HashMap<String, Object>();
            fields.put(fieldHealth, 100);
            co.setFields(fields);

            // special update operators
            QBCustomObjectUpdateBuilder updateBuilder = new QBCustomObjectUpdateBuilder();
            updateBuilder.push(fieldTags, "girl");
            updateBuilder.push(fieldTags, "circle");

            QBCustomObjects.updateObject(co, updateBuilder, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBCustomObjectResult updateResult = (QBCustomObjectResult) result;

                        System.out.println(">>> co : " + updateResult.getCustomObject().toString());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };


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

    Snippet getCustomObjectsWithParams = new Snippet("get objects", "with parameters, search operators, aggregation operators") {
        @Override
        public void execute() {

            QBCustomObjectRequestBuilder requestBuilder = new QBCustomObjectRequestBuilder();

            ArrayList<GenericQueryRule> rules =  new ArrayList<GenericQueryRule>();
            QueryRule rule = new QueryRule(fieldPower, QueryRule.EQ, "345");
            rules.add(rule);
            requestBuilder.setRules(rules);

            QBCustomObjects.getObjects(className, requestBuilder, new QBCallbackImpl() {
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
}
