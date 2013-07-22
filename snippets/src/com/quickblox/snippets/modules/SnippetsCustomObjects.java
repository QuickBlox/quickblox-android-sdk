package com.quickblox.snippets.modules;

import android.content.Context;
import android.util.Log;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.internal.core.helper.StringifyArrayList;
import com.quickblox.internal.module.custom.request.QBCustomObjectRequestBuilder;
import com.quickblox.module.custom.QBCustomObjects;
import com.quickblox.module.custom.model.QBCustomObject;
import com.quickblox.module.custom.model.QBPermissions;
import com.quickblox.module.custom.model.QBPermissionsLevel;
import com.quickblox.module.custom.result.QBCustomObjectLimitedResult;
import com.quickblox.module.custom.result.QBCustomObjectPermissionResult;
import com.quickblox.module.custom.result.QBCustomObjectResult;
import com.quickblox.snippets.Snippet;
import com.quickblox.snippets.Snippets;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * User: Igor Khomenko
 * Date: 11.10.12
 * Time: 12:46
 */
public class SnippetsCustomObjects extends Snippets {

    // Define custom object model in QB Admin Panel
    private static final String CLASS_NAME = "SuperSample";
    private static final String RATING_FIELD = "rating";
    private static final String DESCRIPTION_FIELD = "description";
    private static final String NAME_FIELD = "name";

    public SnippetsCustomObjects(Context context) {
        super(context);

        // regular queries
        snippets.add(createCustomObject);
        snippets.add(getCustomObjectById);
        snippets.add(getGetCustomObjectsByIds);
        snippets.add(getCustomObjects);
        snippets.add(getCustomsObjectWithFilters);
        snippets.add(updateCustomObject);
        snippets.add(deleteCustomObject);

        // permissions
        snippets.add(createCustomObjectWithPermissions);
        snippets.add(updateCustomObjectPermissions);
        snippets.add(getCustomObjectsPermissions);
    }

    Snippet createCustomObject = new Snippet("create object") {
        @Override
        public void execute() {

            QBCustomObject customObject = new QBCustomObject(CLASS_NAME);

            // fields
            HashMap<String, Object> fields = new HashMap<String, Object>();
            fields.put(RATING_FIELD, "4.5");
            fields.put(NAME_FIELD, "QuickBlox");
            fields.put(DESCRIPTION_FIELD, "QuickBlox to conquer the world!");
            customObject.setFields(fields);

            QBCustomObjects.createObject(customObject, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {

                    if (result.isSuccess()) {
                        QBCustomObjectResult customObjectResult = (QBCustomObjectResult) result;
                        QBCustomObject newCustomObject = customObjectResult.getCustomObject();
                        System.out.println(">>> custom object: " + newCustomObject.toString());
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
            QBCustomObject customObject = new QBCustomObject(CLASS_NAME, "51ecf455535c129569019f60");

            QBCustomObjects.getObject(customObject, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBCustomObjectResult customObjectResult = (QBCustomObjectResult) result;
                        QBCustomObject newCustomObject = customObjectResult.getCustomObject();

                        // print record
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

            StringifyArrayList<String> coIDs = new StringifyArrayList<String>();
            coIDs.add("51ecf455535c129569019f60");
            coIDs.add("51ecf484535c12ecb0016709");

            QBCustomObjects.getObjectsByIds(CLASS_NAME, coIDs, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBCustomObjectLimitedResult taskResult = (QBCustomObjectLimitedResult) result;

                        // print records
                        System.out.format(">>> custom objects: " + taskResult.getCustomObjects().toString());
                    }
                }
            });
        }
    };

    Snippet getCustomObjects = new Snippet("get objects") {
        @Override
        public void execute() {
            QBCustomObjects.getObjects(CLASS_NAME, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBCustomObjectLimitedResult coResult = (QBCustomObjectLimitedResult) result;
                        ArrayList<QBCustomObject> co = coResult.getCustomObjects();

                        // print records
                        System.out.println(">>> custom object list: " + co.toString());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet getCustomsObjectWithFilters = new Snippet("get object with filters") {
        @Override
        public void execute() {
            QBCustomObjectRequestBuilder requestBuilder = new QBCustomObjectRequestBuilder();
//            requestBuilder.sortAsc(fieldName);
//            requestBuilder.sortDesc(fieldName);

            // search records which contains exactly specified value
//            String fieldValue = "1";
//            requestBuilder.eq(fieldName, fieldValue);

            // Limit search results to N records. Useful for pagination. Maximum value - 100 (by default). If limit is equal to -1 only last record will be returned
//            requestBuilder.setPagesLimit(2);

            //Skip N records in search results. Useful for pagination. Default (if not specified): 0
//            requestBuilder.setPagesSkip(4);

            // Search record with field which contains value according to specified value and operator
//            requestBuilder.lt("integer_field", 60);
//            requestBuilder.lte(fieldForSort, 1);
//            requestBuilder.gt(fieldForSort, 60);
            requestBuilder.gte(RATING_FIELD, 3);
//            requestBuilder.ne(fieldForSort, 99);

            // for arrays
//            ArrayList<String> healthList = new ArrayList<String>();
//            healthList.add("man");
//            healthList.add("girl");
//            requestBuilder.in("tags", "man", "girl");
//            requestBuilder.nin("tags", healthList);
//            requestBuilder.count();

            QBCustomObjects.getObjects(CLASS_NAME, requestBuilder, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBCustomObjectLimitedResult coresult = (QBCustomObjectLimitedResult) result;
                        ArrayList<QBCustomObject> co = coresult.getCustomObjects();
                        System.out.println(">>> custom objects: " + co.toString());

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
            co.setCustomObjectId("51ecf484535c12ecb0016709");
            co.setClassName(CLASS_NAME);
            HashMap<String, Object> fields = new HashMap<String, Object>();
            fields.put(RATING_FIELD, "3");
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

    Snippet deleteCustomObject = new Snippet("delete object") {
        @Override
        public void execute() {
            QBCustomObject customObject = new QBCustomObject(CLASS_NAME, "51ecf51c535c129ec40063ae");

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


    //////////////// PERMISSIONS ///////////////////
    ////////////////////////////////////////////////

    Snippet createCustomObjectWithPermissions = new Snippet("create object with permissions") {
        @Override
        public void execute() {

            QBCustomObject customObject = new QBCustomObject(CLASS_NAME);

            // fields
            HashMap<String, Object> fields = new HashMap<String, Object>();
            fields.put(RATING_FIELD, "2.5");
            fields.put(NAME_FIELD, "QuickBlox");
            fields.put(DESCRIPTION_FIELD, "QuickBlox to conquer the world!");
            customObject.setFields(fields);

            // permissions
            QBPermissions qbPermissions = new QBPermissions();
            //
            // READ
            qbPermissions.setReadPermission(QBPermissionsLevel.OPEN);
            //
            // UPDATE
            ArrayList<String> groups = new ArrayList<String>();
            groups.add("car");
            groups.add("friends");
            qbPermissions.setUpdatePermission(QBPermissionsLevel.OPEN_FOR_GROUPS, groups);
            //
            // DELETE
            qbPermissions.setDeletePermission(QBPermissionsLevel.OPEN);
            customObject.setPermission(qbPermissions);

            QBCustomObjects.createObject(customObject, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {

                    if (result.isSuccess()) {
                        QBCustomObjectResult customObjectResult = (QBCustomObjectResult) result;
                        QBCustomObject newCustomObject = customObjectResult.getCustomObject();
                        System.out.println(">>> custom object: " + newCustomObject.toString());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet updateCustomObjectPermissions = new Snippet("update object's permissions") {
        @Override
        public void execute() {
            QBCustomObject record = new QBCustomObject();
            record.setClassName(CLASS_NAME);
            record.setCustomObjectId("51ecf455535c129569019f60");

            // fields
            HashMap<String, Object> fields = new HashMap<String, Object>();
            fields.put(RATING_FIELD, "4.5");
            record.setFields(fields);

            // permissions
            QBPermissions qbPermissions = new QBPermissions();
            //
            // READ
            qbPermissions.setReadPermission(QBPermissionsLevel.OWNER);
            //
            // UPDATE
            qbPermissions.setUpdatePermission(QBPermissionsLevel.OPEN);
            //
            // DELETE
            ArrayList<String> ids = new ArrayList<String>();
            ids.add("2131");
            ids.add("300");
            qbPermissions.setDeletePermission(QBPermissionsLevel.OPEN_FOR_USER_IDS, ids);
            record.setPermission(qbPermissions);

            QBCustomObjects.updateObject(record, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBCustomObjectResult updateResult = (QBCustomObjectResult) result;
                        System.out.println(">>> object : " + updateResult.getCustomObject().toString());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet getCustomObjectsPermissions = new Snippet("get custom object permission") {
        @Override
        public void execute() {
            String coId = "51ecf455535c129569019f60";
            QBCustomObjects.getObjectPermissions(CLASS_NAME, coId, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBCustomObjectPermissionResult qbCustomObjectPermissionResult = (QBCustomObjectPermissionResult) result;
                        Log.e("Permissions", qbCustomObjectPermissionResult.getPermissions().toString());
                    }
                }
            });
        }
    };
}