package com.quickblox.sample.groupchatwebrtc.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.sample.groupchatwebrtc.utils.Consts;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tereha on 17.05.16.
 */
public class QbUsersDbManager {
    private static String TAG = QbUsersDbManager.class.getSimpleName();

    public static ArrayList<QBUser> getAllUsers(Context context) {
        ArrayList<QBUser> allUsers = new ArrayList<>();
        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor c = db.query(Consts.DB_TABLE_NAME, null, null, null, null, null, null);

        if (c.moveToFirst()) {
            int userIdColIndex = c.getColumnIndex(Consts.DB_COLUMN_USER_ID);
            int userLoginColIndex = c.getColumnIndex(Consts.DB_COLUMN_USER_LOGIN);
            int userPassColIndex = c.getColumnIndex(Consts.DB_COLUMN_USER_PASSWORD);
            int userFullNameColIndex = c.getColumnIndex(Consts.DB_COLUMN_USER_FULL_NAME);
            int userTagColIndex = c.getColumnIndex(Consts.DB_COLUMN_USER_TAG);

            do {
                QBUser qbUser = new QBUser();

                qbUser.setFullName(c.getString(userFullNameColIndex));
                qbUser.setLogin(c.getString(userLoginColIndex));
                qbUser.setId(c.getInt(userIdColIndex));
                qbUser.setPassword(c.getString(userPassColIndex));

                StringifyArrayList<String> tags = new StringifyArrayList<>();
                tags.add(c.getString(userTagColIndex));
                qbUser.setTags(tags);

                allUsers.add(qbUser);
            } while (c.moveToNext());
        }

        if (c != null) {
            c.close();
        }
        dbHelper.close();

        return allUsers;
    }

    public static QBUser getUserById(Context context, Integer userId) {
        QBUser qbUser = new QBUser();
        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor c = db.query(Consts.DB_TABLE_NAME, null, null, null, null, null, null);

        if (c.moveToFirst()) {
            int userIdColIndex = c.getColumnIndex(Consts.DB_COLUMN_USER_ID);
            int userLoginColIndex = c.getColumnIndex(Consts.DB_COLUMN_USER_LOGIN);
            int userPassColIndex = c.getColumnIndex(Consts.DB_COLUMN_USER_PASSWORD);
            int userFullNameColIndex = c.getColumnIndex(Consts.DB_COLUMN_USER_FULL_NAME);
            int userTagColIndex = c.getColumnIndex(Consts.DB_COLUMN_USER_TAG);

            do {
                if (c.getInt(userIdColIndex) == userId) {
                    qbUser.setFullName(c.getString(userFullNameColIndex));
                    qbUser.setLogin(c.getString(userLoginColIndex));
                    qbUser.setId(c.getInt(userIdColIndex));
                    qbUser.setPassword(c.getString(userPassColIndex));

                    StringifyArrayList<String> tags = new StringifyArrayList<>();
                    tags.add(c.getString(userTagColIndex));
                    qbUser.setTags(tags);
                    break;
                }
            } while (c.moveToNext());
        }

        if (c != null) {
            c.close();
        }
        dbHelper.close();

        return qbUser;
    }

    public static void saveAllUsers(Context context, ArrayList<QBUser> allUsers) {
        for (QBUser qbUser : allUsers) {
            saveUser(context, qbUser);
        }
        Log.d(TAG, "saveAllUsers");
    }

    public static void saveUser(Context context, QBUser qbUser) {
        ContentValues cv = new ContentValues();
        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        cv.put(Consts.DB_COLUMN_USER_FULL_NAME, qbUser.getFullName());
        cv.put(Consts.DB_COLUMN_USER_LOGIN, qbUser.getLogin());
        cv.put(Consts.DB_COLUMN_USER_ID, qbUser.getId());
        cv.put(Consts.DB_COLUMN_USER_PASSWORD, qbUser.getPassword());
        cv.put(Consts.DB_COLUMN_USER_TAG, qbUser.getTags().get(0));

        db.insert(Consts.DB_TABLE_NAME, null, cv);
        dbHelper.close();
    }

    public static void clearDB(Context context) {
        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(Consts.DB_TABLE_NAME, null, null);
        dbHelper.close();
    }

    public static String getUserNameById(Context context, Integer userId){
        QBUser userById = getUserById(context, userId);
        String fullName = userById.getFullName();
        return TextUtils.isEmpty(fullName) ? String.valueOf(userId) : fullName;
    }

    public static ArrayList<QBUser> getUsersByIds(Context context, List<Integer> usersIds){
        ArrayList<QBUser> qbUsers = new ArrayList<>();

        for (Integer userId : usersIds){
            qbUsers.add(getUserById(context, userId));
        }

        return qbUsers;
    }
}

