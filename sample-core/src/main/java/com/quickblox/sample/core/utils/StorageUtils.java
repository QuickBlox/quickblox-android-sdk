package com.quickblox.sample.core.utils;

import android.os.Environment;

import com.quickblox.sample.core.CoreApp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class StorageUtils {

    public static String getAppExternalDataDirectoryPath() {
        StringBuilder sb = new StringBuilder();
        sb.append(Environment.getExternalStorageDirectory())
                .append(File.separator)
                .append("Android")
                .append(File.separator)
                .append("data")
                .append(File.separator)
                .append(CoreApp.getInstance().getPackageName())
                .append(File.separator);

        return sb.toString();
    }

    public static File getAppExternalDataDirectoryFile() {
        File dataDirectoryFile = new File(getAppExternalDataDirectoryPath());
        dataDirectoryFile.mkdirs();

        return dataDirectoryFile;
    }

    public static void writeStringToFile(String string, File file) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(string.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void writeObjectToFile(Object object, File file) {
        ObjectOutputStream oos = null;
        try {
            FileOutputStream fos = new FileOutputStream(file);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(object);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (oos != null) {
                    oos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T readObjectFromFile(File file) {
        ObjectInputStream ois = null;
        T object = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            ois = new ObjectInputStream(fis);
            object = (T) ois.readObject();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return object;
    }
}
