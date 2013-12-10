package com.quickblox.snippets.modules;

import android.content.Context;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.internal.core.helper.FileHelper;
import com.quickblox.module.content.model.QBFileObjectAccess;
import com.quickblox.module.content.result.QBFileDownloadResult;
import com.quickblox.module.custom.QBCustomObjectsFiles;
import com.quickblox.module.custom.model.QBCustomObject;
import com.quickblox.module.custom.model.QBCustomObjectFileField;
import com.quickblox.module.custom.result.QBCOFileUploadResult;
import com.quickblox.snippets.R;
import com.quickblox.snippets.Snippet;
import com.quickblox.snippets.Snippets;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by vfite on 06.12.13.
 */
public class SnippetsCustomObjectFiles extends Snippets {

    File file1 = null;
    File file2 = null;
    QBFileObjectAccess fileObjectAccess;

    private String fileName = "sample";

    private final String NOTE1_ID = "51d816e0535c12d75f006537";

    private final String CLASS_NAME = "note";
    private final String LICENSE = "license";

    public SnippetsCustomObjectFiles(Context context) {
        super(context);

        snippets.add(downloadFile);
        snippets.add(updateFile);
        snippets.add(uploadFile);
        snippets.add(deleteFile);

        // get file
        file1 = getFileFormRaw(R.raw.sample_file);
        file2 = getFileFormRaw(R.raw.sample_file1);
    }

    private File getFileFormRaw(int fileId){
        InputStream is = context.getResources().openRawResource(fileId);
        File file = FileHelper.getFileInputStream(is, "sample"+fileId+".txt", "qb_snippets12");
        return file;
    }

    Snippet uploadFile = new Snippet("upload CO file") {
        @Override
        public void execute() {
            QBCustomObject qbCustomObject = new QBCustomObject(CLASS_NAME, NOTE1_ID);
            QBCustomObjectsFiles.uploadFile(file1, qbCustomObject, LICENSE, fileName, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {

                        QBCustomObjectFileField customObjectFileField = ((QBCOFileUploadResult) result).getCustomObjectFileField();
                        System.out.println(">>>upload response:" + customObjectFileField.getFileName()+  " "+customObjectFileField.getFileId()+       " "+
                                customObjectFileField.getContentType() );
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
            QBCustomObjectsFiles.updateFile(file2, qbCustomObject, LICENSE,  new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        System.out.println(">>> file updated successfully");
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
            QBCustomObjectsFiles.deleteFile(qbCustomObject, LICENSE, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        System.out.println(">>> file deleted successfully");
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
            QBCustomObjectsFiles.downloadFile(qbCustomObject, LICENSE, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    QBFileDownloadResult downloadResult = (QBFileDownloadResult) result;
                    if (result.isSuccess()) {

                        byte[] content = downloadResult.getContent();       // that's downloaded file content
                        InputStream is = downloadResult.getContentStream(); // that's downloaded file content

                        System.out.println(">>> file downloaded successfully" + getContentFromFile(is));
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
