package com.sdk.snippets;

import android.content.Context;

import com.quickblox.core.helper.FileHelper;
import com.quickblox.core.helper.Lo;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * Created by igorkhomenko on 10/22/15.
 */
public class Utils {
    public static String getContentFromFile(InputStream is) {
        char[] buffer = new char[1024];
        StringBuilder stringBuilder = new StringBuilder();
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(is, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            while (bufferedReader.read(buffer, 0, 1024) != -1) {
                stringBuilder.append(buffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    public static File getFileFromRawResource(int fileId, Context context) {
        InputStream is = context.getResources().openRawResource(fileId);
        File file = FileHelper.getFileInputStream(is, "sample" + fileId + ".txt", "qb_snippets12");
        return file;
    }

    public static void downloadFile(final InputStream inputStream, final Context ctx){
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    String filePath = ctx.getFilesDir().getPath().toString() + "/bigFile.pkg";
                    File file = new File(filePath);
                    OutputStream stream = new BufferedOutputStream(new FileOutputStream(file));
                    int bufferSize = 1024;
                    byte[] buffer = new byte[bufferSize];
                    int len;
                    while ((len = inputStream.read(buffer)) != -1) {
                        stream.write(buffer, 0, len);
                    }
                    if(stream != null) {
                        Lo.g("download done");
                        stream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }
}
