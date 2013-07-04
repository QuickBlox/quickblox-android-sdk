package com.quickblox.chat_v2.apis;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;

import com.quickblox.chat_v2.core.ChatApplication;
import com.quickblox.chat_v2.interfaces.OnFileUploadComplete;
import com.quickblox.chat_v2.interfaces.OnPictureDownloadComplete;
import com.quickblox.chat_v2.interfaces.OnUserProfileDownloaded;
import com.quickblox.chat_v2.utils.ContextForDownloadUser;
import com.quickblox.core.QBCallback;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.module.content.QBContent;
import com.quickblox.module.content.result.QBFileDownloadResult;
import com.quickblox.module.content.result.QBFileUploadTaskResult;
import com.quickblox.module.custom.QBCustomObjects;
import com.quickblox.module.custom.model.QBCustomObject;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.module.users.result.QBUserPagedResult;
import com.quickblox.module.users.result.QBUserResult;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class QuickBloxManager {

    private ChatApplication app;

    private List<OnUserProfileDownloaded> mUserProfileListener;
    private OnPictureDownloadComplete pictureDownloadComplete;
    private OnFileUploadComplete uploadListener;

    private boolean pictureMode;
    private int currentFileId;

    private Context mContext;

    public QuickBloxManager(Context context) {
        mUserProfileListener = new CopyOnWriteArrayList<OnUserProfileDownloaded>();
        app = ChatApplication.getInstance();
        mContext = context;

    }

    public void getQbUsersFromCollection(final ArrayList<String> usersIds, ContextForDownloadUser pContextForDownloadUser) {
        final ContextForDownloadUser contextForDownloadUser = pContextForDownloadUser;
        synchronized (contextForDownloadUser) {
            QBUsers.getUsersByIDs(usersIds, new QBCallbackImpl() {

                @Override
                public void onComplete(Result result, Object context) {

                    QBUserPagedResult usersResult = (QBUserPagedResult) result;

                    ContextForDownloadUser contextForDownloadUser = (ContextForDownloadUser) context;

                    switch (contextForDownloadUser) {
                        case DOWNLOAD_FOR_DIALOG:
                            if (app.getContactsMap() != null) {
                                for (QBUser qu : usersResult.getUsers()) {
                                    app.getDialogsUsersMap().put(String.valueOf(qu.getId()), qu);
                                }
                            }
                            break;

                        case DOWNLOAD_FOR_CONTACTS:
                        case DOWNLOAD_FOR_MAIN_ACTIVITY:
                            for (QBUser contact : usersResult.getUsers()) {
                                app.getContactsMap().put(String.valueOf(contact.getId()), contact);

                            }
                            break;

                    }


                    for (OnUserProfileDownloaded listener : mUserProfileListener) {
                        listener.downloadComlete(null, contextForDownloadUser);
                    }
                }
            }, pContextForDownloadUser);
        }
    }

    // WARNING ! upload section
    public void uploadPic(File file, boolean isFileTransferAttach) {
        pictureMode = isFileTransferAttach;

        QBContent.uploadFileTask(file, true, new QBCallbackImpl() {
            @Override
            public void onComplete(Result result) {

                if (result.isSuccess()) {
                    QBFileUploadTaskResult fileUploadTaskResultResult = (QBFileUploadTaskResult) result;
                    if (!pictureMode) {
                        app.getQbUser().setFileId(fileUploadTaskResultResult.getFile().getId());

                        updateQBUser(app.getQbUser());
                    } else {
                        uploadListener.uploadComplete(fileUploadTaskResultResult.getFile().getId(), fileUploadTaskResultResult.getFile().getPublicUrl());
                    }
                }
            }
        });
    }

    private void updateQBUser(QBUser upadtedUser) {

        final QBUser userToUpdate = new QBUser();
        userToUpdate.setId(upadtedUser.getId());
        userToUpdate.setFileId(upadtedUser.getFileId());

        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                QBUsers.updateUser(userToUpdate, new QBCallbackImpl() {

                    @Override
                    public void onComplete(Result result) {
                        uploadListener.uploadComplete(userToUpdate.getFileId(), null);
                    }

                });
            }
        });
    }


    public void downloadQBFile(QBUser currentUser) {
        if (currentUser.getFileId() == null) {
            return;
        }
        File targetFile = new File(mContext.getCacheDir(), String.valueOf(currentUser.getFileId()));

        if (targetFile.exists()) {
            Bitmap userPic = BitmapFactory.decodeFile(targetFile.getPath());
            pictureDownloadComplete.downloadComlete(userPic, targetFile);
            return;
        }

        currentFileId = currentUser.getFileId();
        QBContent.downloadFileTask(currentUser.getFileId(), new QBCallbackImpl() {

            @Override
            public void onComplete(Result result) {

                QBFileDownloadResult qbFileDownloadResult = (QBFileDownloadResult) result;
                if (result.isSuccess()) {

                    InputStream is = qbFileDownloadResult.getContentStream();

                    File userPicFile = new File(mContext.getCacheDir(), String.valueOf(currentFileId));
                    FileOutputStream fos;
                    try {
                        fos = new FileOutputStream(userPicFile);
                        writeFromInputToOutput(is, fos);
                        is.close();
                        fos.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Bitmap userPic = BitmapFactory.decodeFile(userPicFile.getPath());
                    pictureDownloadComplete.downloadComlete(userPic, userPicFile);
                }
            }
        });
    }

    public synchronized void getSingleUserInfo(final int userId, final ContextForDownloadUser pContextForDownloadUser) {

        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                QBUsers.getUser(userId, new QBCallback() {

                    @Override
                    public void onComplete(Result result, Object context) {
                        ContextForDownloadUser contextForDownloadUser = (ContextForDownloadUser) context;
                        for (OnUserProfileDownloaded listeners : mUserProfileListener) {
                            listeners.downloadComlete(((QBUserResult) result).getUser(), (ContextForDownloadUser) context);
                        }
                    }

                    @Override
                    public void onComplete(Result result) {

                    }
                }, pContextForDownloadUser);
            }
        });

    }


    private void writeFromInputToOutput(InputStream source, OutputStream dest) {
        final int BUFFER_SIZE = 2048;
        final int EOF_MARK = -1;

        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead = EOF_MARK;

        try {
            while ((bytesRead = source.read(buffer)) != EOF_MARK) {
                dest.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createSingleCustomObject(final QBCustomObject pCustomObject) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                QBCustomObjects.createObject(pCustomObject, new QBCallback() {
                    @Override
                    public void onComplete(Result result) {
                    }

                    @Override
                    public void onComplete(Result result, Object context) {

                    }
                });
            }
        });

    }

    // LISTENERS

    public void addUserProfileListener(OnUserProfileDownloaded friendProfileListener) {
        if (friendProfileListener != null) {
            this.mUserProfileListener.add(friendProfileListener);
        }
    }

    public void removeUserProfileListener(OnUserProfileDownloaded friendProfileListener) {
        this.mUserProfileListener.remove(friendProfileListener);

    }

    public void setPictureDownloadComplete(OnPictureDownloadComplete pictureDownloadComplete) {
        this.pictureDownloadComplete = pictureDownloadComplete;
    }

    public void setUploadListener(OnFileUploadComplete uploadListener) {
        this.uploadListener = uploadListener;
    }
}
