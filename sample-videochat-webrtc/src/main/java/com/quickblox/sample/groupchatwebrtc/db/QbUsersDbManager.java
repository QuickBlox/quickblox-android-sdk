package com.quickblox.sample.groupchatwebrtc.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tereha on 17.05.16.
 */
public class QbUsersDbManager {
    private static String TAG = QbUsersDbManager.class.getSimpleName();

    private static QbUsersDbManager instance;
    private Context mContext;

    private QbUsersDbManager(Context context) {
        this.mContext = context;
    }

    public static QbUsersDbManager getInstance(Context context) {
        if (instance == null) {
            instance = new QbUsersDbManager(context);
        }

        return instance;
    }

    public ArrayList<QBUser> getAllUsers() {
        ArrayList<QBUser> allUsers = new ArrayList<>();
        DbHelper dbHelper = new DbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor c = db.query(DbHelper.DB_TABLE_NAME, null, null, null, null, null, null);

        if (c.moveToFirst()) {
            int userIdColIndex = c.getColumnIndex(DbHelper.DB_COLUMN_USER_ID);
            int userLoginColIndex = c.getColumnIndex(DbHelper.DB_COLUMN_USER_LOGIN);
            int userPassColIndex = c.getColumnIndex(DbHelper.DB_COLUMN_USER_PASSWORD);
            int userFullNameColIndex = c.getColumnIndex(DbHelper.DB_COLUMN_USER_FULL_NAME);
            int userTagColIndex = c.getColumnIndex(DbHelper.DB_COLUMN_USER_TAG);

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

        c.close();
        dbHelper.close();

        return allUsers;
    }

    public QBUser getUserById(Integer userId) {
        QBUser qbUser = null;
        DbHelper dbHelper = new DbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor c = db.query(DbHelper.DB_TABLE_NAME, null, null, null, null, null, null);

        if (c.moveToFirst()) {
            int userIdColIndex = c.getColumnIndex(DbHelper.DB_COLUMN_USER_ID);
            int userLoginColIndex = c.getColumnIndex(DbHelper.DB_COLUMN_USER_LOGIN);
            int userPassColIndex = c.getColumnIndex(DbHelper.DB_COLUMN_USER_PASSWORD);
            int userFullNameColIndex = c.getColumnIndex(DbHelper.DB_COLUMN_USER_FULL_NAME);
            int userTagColIndex = c.getColumnIndex(DbHelper.DB_COLUMN_USER_TAG);

            do {
                if (c.getInt(userIdColIndex) == userId) {
                    qbUser = new QBUser();
                    qbUser.setFullName(c.getString(userFullNameColIndex));
                    qbUser.setLogin(c.getString(userLoginColIndex));
                    qbUser.setId(c.getInt(userIdColIndex));
                    qbUser.setPassword(c.getString(userPassColIndex));

                    StringifyArrayList<String> tags = new StringifyArrayList<>();
                    tags.add(c.getString(userTagColIndex).split(","));
                    qbUser.setTags(tags);
                    break;
                }
            } while (c.moveToNext());
        }

        c.close();
        dbHelper.close();

        return qbUser;
    }

    public void saveAllUsers(ArrayList<QBUser> allUsers, boolean needRemoveOldData) {
        if (needRemoveOldData) {
            clearDB();
        }

        for (QBUser qbUser : allUsers) {
            saveUser(qbUser);
        }
        Log.d(TAG, "saveAllUsers");
    }

    public void saveUser(QBUser qbUser) {
        ContentValues cv = new ContentValues();
        DbHelper dbHelper = new DbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        cv.put(DbHelper.DB_COLUMN_USER_FULL_NAME, qbUser.getFullName());
        cv.put(DbHelper.DB_COLUMN_USER_LOGIN, qbUser.getLogin());
        cv.put(DbHelper.DB_COLUMN_USER_ID, qbUser.getId());
        cv.put(DbHelper.DB_COLUMN_USER_PASSWORD, qbUser.getPassword());
        cv.put(DbHelper.DB_COLUMN_USER_TAG, qbUser.getTags().getItemsAsString());

        db.insert(DbHelper.DB_TABLE_NAME, null, cv);
        dbHelper.close();
    }

    public void clearDB() {
        DbHelper dbHelper = new DbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DbHelper.DB_TABLE_NAME, null, null);
        dbHelper.close();
    }

    public ArrayList<QBUser> getUsersByIds(List<Integer> usersIds) {
        ArrayList<QBUser> qbUsers = new ArrayList<>();

        for (Integer userId : usersIds) {
            if (getUserById(userId) != null) {
                qbUsers.add(getUserById(userId));
            }
        }

        return qbUsers;
    }
}

