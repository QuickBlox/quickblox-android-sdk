package com.quickblox.snippets.modules;

import android.content.Context;
import android.util.Log;

import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.internal.core.helper.FileHelper;
import com.quickblox.internal.core.helper.StringifyArrayList;
import com.quickblox.internal.module.custom.request.QBCustomObjectRequestBuilder;
import com.quickblox.module.content.result.QBFileDownloadResult;
import com.quickblox.module.custom.QBCustomObjects;
import com.quickblox.module.custom.QBCustomObjectsFiles;
import com.quickblox.module.custom.model.QBCustomObject;
import com.quickblox.module.custom.model.QBCustomObjectFileField;
import com.quickblox.module.custom.model.QBPermissions;
import com.quickblox.module.custom.model.QBPermissionsLevel;
import com.quickblox.module.custom.result.QBCOFileUploadResult;
import com.quickblox.module.custom.result.QBCustomObjectDeletedResult;
import com.quickblox.module.custom.result.QBCustomObjectLimitedResult;
import com.quickblox.module.custom.result.QBCustomObjectMultiUpdatedResult;
import com.quickblox.module.custom.result.QBCustomObjectPermissionResult;
import com.quickblox.module.custom.result.QBCustomObjectResult;
import com.quickblox.module.custom.result.QBCustomObjectTaskResult;
import com.quickblox.snippets.R;
import com.quickblox.snippets.Snippet;
import com.quickblox.snippets.Snippets;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * User: Oleg Soroka
 * Date: 11.10.12
 * Time: 12:46
 */
public class SnippetsCustomObjects extends Snippets {

    private static final String TAG = SnippetsCustomObjects.class.getSimpleName();
    // Define custom object model in QB Admin Panel
    // http://quickblox.com/developers/Custom_Objects
    //
    private final String CLASS_NAME = "SuperSample";
    private final String RATING_FIELD = "rating";
    private final String DESCRIPTION_FIELD = "description";
    private final String AVATAR_FIELD = "avatar";
    File file1 = null;
    File file2 = null;

    private final String NOTE1_ID = "51d816e0535c12d75f006537";

    public SnippetsCustomObjects(Context context) {
        super(context);

        snippets.add(createCustomObject);
        snippets.add(createCustomObjects);
        snippets.add(getCustomObjectById);
        snippets.add(deleteCustomObject);
        snippets.add(deleteCustomObjects);
        snippets.add(getCustomObjects);
        snippets.add(updateCustomObject);
        snippets.add(updateCustomObjects);
        snippets.add(getGetCustomObjectsByIds);
        snippets.add(getCustomObjectPermissionById);
        snippets.add(getCustomsObjectWithFilters);
        snippets.add(downloadFile);
        snippets.add(updateFile);
        snippets.add(uploadFile);
        snippets.add(deleteFile);

        // get file
        file1 = getFileFormRaw(R.raw.sample_file);
        file2 = getFileFormRaw(R.raw.sample_file2);
    }

    private File getFileFormRaw(int fileId){
        InputStream is = context.getResources().openRawResource(fileId);
        File file = FileHelper.getFileInputStream(is, "sample" + fileId + ".txt", "qb_snippets12");
        return file;
    }

    Snippet getCustomObjects = new Snippet("get objects") {
        @Override
        public void execute() {
            QBCustomObjects.getObjects(CLASS_NAME, new QBCallbackImpl() {
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
            // Create new record
            //
            QBCustomObject newRecord = new QBCustomObject(CLASS_NAME);
            newRecord.put(RATING_FIELD, 99);
            newRecord.put(DESCRIPTION_FIELD, "Hello world");
            newRecord.setParentId("50d9bf2d535c12344701c43a");
            //
            // set permissions:
            // READ
            QBPermissions permissions = new QBPermissions();
            permissions.setReadPermission(QBPermissionsLevel.OPEN);
            //
            // DELETE
            ArrayList<String> openPermissionsForUserIDS = new  ArrayList<String>();
            openPermissionsForUserIDS.add("33");
            openPermissionsForUserIDS.add("92");
            permissions.setDeletePermission(QBPermissionsLevel.OPEN_FOR_USER_IDS, openPermissionsForUserIDS);
            //
            // UPDATE
            permissions.setUpdatePermission(QBPermissionsLevel.OWNER);
            newRecord.setPermission(permissions);

            QBCustomObjects.createObject(newRecord, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBCustomObjectResult customObjectResult = (QBCustomObjectResult) result;
                        QBCustomObject newCustomObject = customObjectResult.getCustomObject();
                        Log.i(TAG, ">>> created record: " + newCustomObject);
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet createCustomObjects = new Snippet("create objects") {

        public static  final int NUM_RECORDS = 4;

        private QBCustomObject createObject(){
            Random random = new Random();
            QBCustomObject customObject = new QBCustomObject(CLASS_NAME);
            customObject.put(RATING_FIELD, random.nextInt(100));
            customObject.put(DESCRIPTION_FIELD, "Hello world");
            return customObject;
        }


        @Override
        public void execute() {
            List<QBCustomObject> qbCustomObjectList = new ArrayList<QBCustomObject>(NUM_RECORDS);
            for (int i = 0; i < NUM_RECORDS; i++){
                QBCustomObject qbCustomObject = createObject();
                qbCustomObjectList.add(qbCustomObject);
            }

            QBCustomObjects.createObjects(qbCustomObjectList, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {

                    if (result.isSuccess()) {
                        QBCustomObjectLimitedResult customObjectsResult = (QBCustomObjectLimitedResult) result;
                        ArrayList<QBCustomObject> newCustomObjects = customObjectsResult.getCustomObjects();
                        Log.i(TAG, ">>> custom object list: " + newCustomObjects.toString());

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
            coIDs.add("50e67e6e535c121c66004c74");
            coIDs.add("50e67e6d535c127f66004f47");
            coIDs.add("50e67e6b535c121c66004c72");
            coIDs.add("50e59f81535c121c660015fd");

            QBCustomObjects.getObjectsByIds(CLASS_NAME, coIDs, new QBCallbackImpl() {
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

    Snippet getCustomsObjectWithFilters = new Snippet("get object with filters") {
        @Override
        public void execute() {
            String fieldName = "title";
            String fieldForSort = "integer_field";
            QBCustomObjectRequestBuilder requestBuilder = new QBCustomObjectRequestBuilder();
//            requestBuilder.sortAsc(fieldName);
//            requestBuilder.sortDesc(fieldName);

            // search records which contains exactly specified value
//            String fieldValue = "1";
//            requestBuilder.eq(fieldName, fieldValue);

            // Limit search results to N records. Useful for pagination. Maximum value - 100 (by default). If limit is equal to -1 only last record will be returned
//            requestBuilder.setPagesLimit(2);

            //Skip N records in search results. Useful for pagination. Default (if not specified): 0
            requestBuilder.setPagesSkip(4);

            // Search record with field which contains value according to specified value and operator
//            requestBuilder.lt("integer_field", 60);
//            requestBuilder.lte(fieldForSort, 1);
//            requestBuilder.gt(fieldForSort, 60);
//            requestBuilder.gte(fieldForSort, 99);
//            requestBuilder.ne(fieldForSort, 99);
//            requestBuilder.ctn(fieldName, "son");

            // for arrays
//            ArrayList<String> healthList = new ArrayList<String>();
//            healthList.add("man");
//            healthList.add("girl");
//            requestBuilder.in("tags", "man", "girl");
//            requestBuilder.or(fieldName, "sam", "igor");
//            requestBuilder.nin("tags", healthList);
//            requestBuilder.count();


            List<Object> objectList = new ArrayList<Object>();
            objectList.add(fieldName);
            QBCustomObjects.getObjects(CLASS_NAME, objectList, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBCustomObjectLimitedResult coresult = (QBCustomObjectLimitedResult) result;
                        ArrayList<QBCustomObject> co = coresult.getCustomObjects();
                        Log.i(TAG, ">>> custom object list: " + co.toString());

                    } else {
                        handleErrors(result);
                    }

                    // if we use requestBuilder.count()
//                    QBCustomObjectCountResult countResult = (QBCustomObjectCountResult) result;
//                    Log.d("Count", String.valueOf(countResult.getCount()));
                }
            });
        }
    };

    Snippet getCustomObjectPermissionById = new Snippet("get object permissions") {
        @Override
        public void execute() {
            String OBJ_ID = "52b88399535c12c51c001140";

            QBCustomObjects.getObjectPermissions(CLASS_NAME, OBJ_ID, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBCustomObjectPermissionResult customObjectPermissionResult = (QBCustomObjectPermissionResult) result;
                        QBPermissions permissions = customObjectPermissionResult.getPermissions();
                        Log.i(TAG, ">>> custom object's permissions: " + permissions.toString());
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
            QBCustomObject customObject = new QBCustomObject(CLASS_NAME, "50e3f8c7535c126073000d52");

            QBCustomObjects.getObject(customObject, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBCustomObjectResult customObjectResult = (QBCustomObjectResult) result;
                        QBCustomObject newCustomObject = customObjectResult.getCustomObject();

                        Log.i(TAG, ">>> custom object: " + newCustomObject);
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
            QBCustomObject customObject = new QBCustomObject(CLASS_NAME, "af3514342afbbb3555");

            QBCustomObjects.deleteObject(customObject, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        Log.i(TAG, ">>> custom object deleted OK");
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet deleteCustomObjects = new Snippet("delete objects") {
        @Override
        public void execute() {

            StringifyArrayList<String> deleteIds = new StringifyArrayList<String>();
            deleteIds.add("50e3f85f535c123376000d31");
            deleteIds.add("50e3f85f535c123376000d32");
            QBCustomObjects.deleteObjects(CLASS_NAME, deleteIds, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBCustomObjectDeletedResult qbCustomObjectDeletedResult = (QBCustomObjectDeletedResult) result;
                        Log.i(TAG, ">>> deletedObjs: " + qbCustomObjectDeletedResult.getDeleted().toString());
                        Log.i(TAG, ">>> notFoundObjs: " + qbCustomObjectDeletedResult.getNotFound().toString());
                        Log.i(TAG, ">>> wrongPermissionsObjs: " + qbCustomObjectDeletedResult.getWrongPermissions().toString());
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
            QBCustomObject record = new QBCustomObject();
            //
            // set Class name and record ID:
            record.setClassName(CLASS_NAME);
            record.setCustomObjectId("52b30274535c12fbf80121bd");
            //
            // set fields:
            HashMap<String, Object> fields = new HashMap<String, Object>();
            fields.put(DESCRIPTION_FIELD, "Hello world");
            fields.put(RATING_FIELD, 10);
            record.setFields(fields);
            //
            // update permissions:
            // READ
            QBPermissions permissions = new QBPermissions();
            permissions.setReadPermission(QBPermissionsLevel.OPEN);
            //
            // DELETE
            ArrayList<String> openPermissionsForUserIDS = new  ArrayList<String>();
            openPermissionsForUserIDS.add("33");
            openPermissionsForUserIDS.add("92");
            permissions.setDeletePermission(QBPermissionsLevel.OPEN_FOR_USER_IDS, openPermissionsForUserIDS);
            //
            // UPDATE
            permissions.setUpdatePermission(QBPermissionsLevel.OWNER);
            record.setPermission(permissions);

            QBCustomObjects.updateObject(record, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBCustomObjectResult updateResult = (QBCustomObjectResult) result;

                        Log.i(TAG, ">>> updated record: : " + updateResult.getCustomObject().toString());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet updateCustomObjects = new Snippet("update objects") {

        public static  final int NUM_RECORDS = 4;

        private QBCustomObject createObject(){
            Random random = new Random();
            QBCustomObject customObject = new QBCustomObject(CLASS_NAME);
            customObject.put(RATING_FIELD, random.nextInt(100));
            customObject.put(DESCRIPTION_FIELD, "Hello world");
            return customObject;
        }

        @Override
        public void execute() {
            QBCustomObject co1 = createObject();
            co1.setCustomObjectId("50e3f85f535c123376000d31");
            QBCustomObject co2 = createObject();
            co2.setCustomObjectId("50e3f85f535c123376000d32");
            QBCustomObject co3 = createObject();
            co3.setCustomObjectId("50e3f85f535c123376000d33");
            List<QBCustomObject> qbCustomObjectList = new LinkedList<QBCustomObject>();
            qbCustomObjectList.add(co1);
            qbCustomObjectList.add(co2);
            qbCustomObjectList.add(co3);

            QBCustomObjects.updateObjects(qbCustomObjectList, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBCustomObjectMultiUpdatedResult updateResult = (QBCustomObjectMultiUpdatedResult) result;

                        Log.i(TAG, ">>> updatedObjects: " + updateResult.getCustomObjects().toString());
                        Log.i(TAG, ">>> notFoundObjects: " + updateResult.getNotFoundIds().toString());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet uploadFile = new Snippet("upload CO file") {
        @Override
        public void execute() {
            QBCustomObject qbCustomObject = new QBCustomObject(CLASS_NAME, NOTE1_ID);
            QBCustomObjectsFiles.uploadFile(file1, qbCustomObject, AVATAR_FIELD, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {

                        QBCustomObjectFileField customObjectFileField = ((QBCOFileUploadResult) result).getCustomObjectFileField();
                        Log.i(TAG, ">>>upload response:" + customObjectFileField.getFileName() + " " + customObjectFileField.getFileId() + " " +
                                customObjectFileField.getContentType());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet updateFile = new Snippet("update CO file") {
        @Override
        public void execute() {
            QBCustomObject qbCustomObject = new QBCustomObject(CLASS_NAME, NOTE1_ID);
            QBCustomObjectsFiles.uploadFile(file2, qbCustomObject, AVATAR_FIELD, null, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        Log.i(TAG, ">>> file updated successfully");
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet deleteFile = new Snippet("delete CO file") {
        @Override
        public void execute() {
            QBCustomObject qbCustomObject = new QBCustomObject(CLASS_NAME, NOTE1_ID);
            QBCustomObjectsFiles.deleteFile(qbCustomObject, AVATAR_FIELD, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        Log.i(TAG, ">>> file deleted successfully");
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };


    Snippet downloadFile = new Snippet("download CO file") {
        @Override
        public void execute() {
            QBCustomObject qbCustomObject = new QBCustomObject(CLASS_NAME, NOTE1_ID);
            QBCustomObjectsFiles.downloadFile(qbCustomObject, AVATAR_FIELD, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    QBFileDownloadResult downloadResult = (QBFileDownloadResult) result;
                    if (result.isSuccess()) {

                        byte[] content = downloadResult.getContent();       // that's downloaded file content
                        InputStream is = downloadResult.getContentStream(); // that's downloaded file content

                        Log.i(TAG, ">>> file downloaded successfully" + getContentFromFile(is));
                        if(is!=null){
                            try{
                                is.close();
                            }
                            catch(IOException e){
                                e.printStackTrace();
                            }
                        }
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };


    public String getContentFromFile( InputStream is){
        char[] buffer = new char[1024];
        StringBuilder stringBuilder = new StringBuilder();
        try{
            InputStreamReader inputStreamReader = new InputStreamReader(is, "UTF-8");

            while ( inputStreamReader.read(buffer, 0, 1024) != -1){
                stringBuilder.append(buffer);
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

}