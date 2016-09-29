package com.quickblox.sample.chat.managers;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBSystemMessagesManager;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.sample.chat.utils.chat.ChatHelper;
import com.quickblox.sample.chat.utils.qb.QbDialogHolder;
import com.quickblox.sample.chat.utils.qb.QbDialogUtils;
import com.quickblox.sample.chat.utils.qb.callback.QbEntityCallbackImpl;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.SmackException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class DialogsManager {

    public static final String PROPERTY_OCCUPANTS_IDS = "occupants_ids";
    public static final String PROPERTY_DIALOG_TYPE = "dialog_type";
    public static final String PROPERTY_NOTIFICATION_TYPE = "notification_type";
    public static final String CREATING_DIALOG = "creating_dialog";

    private Set<ManagingDialogsCallbacks> magingDialogsCallbackListener = new CopyOnWriteArraySet<>();

    public static boolean isMessageCreatingDialog(QBChatMessage systemMessage){
        return CREATING_DIALOG.equals(systemMessage.getProperty(PROPERTY_NOTIFICATION_TYPE));
    }

    public static QBChatMessage buildSystemMessageAboutCreatingGroupDialog(QBChatDialog dialog){
        QBChatMessage qbChatMessage = new QBChatMessage();
        qbChatMessage.setDialogId(dialog.getDialogId());
        qbChatMessage.setProperty(PROPERTY_OCCUPANTS_IDS, QbDialogUtils.getOccupantsIdsStringFromList(dialog.getOccupants()));
        qbChatMessage.setProperty(PROPERTY_DIALOG_TYPE, String.valueOf(dialog.getType().getCode()));
        qbChatMessage.setProperty(PROPERTY_NOTIFICATION_TYPE, CREATING_DIALOG);

        return qbChatMessage;
    }

    public static QBChatDialog buildChatDialogFromSystemMessage(QBChatMessage qbChatMessage){
        QBChatDialog chatDialog = new QBChatDialog();
        chatDialog.setDialogId(qbChatMessage.getDialogId());
        chatDialog.setOccupantsIds(QbDialogUtils.getOccupantsIdsListFromString((String) qbChatMessage.getProperty(PROPERTY_OCCUPANTS_IDS)));
        chatDialog.setType(QBDialogType.parseByCode(Integer.parseInt(qbChatMessage.getProperty(PROPERTY_DIALOG_TYPE).toString())));

        return chatDialog;
    }

    public static void sendSystemMessageAboutCreatingDialog(QBSystemMessagesManager systemMessagesManager, QBChatDialog dialog) {
        QBChatMessage systemMessageCreatingDialog = buildSystemMessageAboutCreatingGroupDialog(dialog);

        try {
            for (Integer recipientId : dialog.getOccupants()) {
                if (!recipientId.equals(QBChatService.getInstance().getUser().getId())) {
                    systemMessageCreatingDialog.setRecipientId(recipientId);
                    systemMessagesManager.sendSystemMessage(systemMessageCreatingDialog);
                }
            }
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }

    private void loadUsersFromDialog(QBChatDialog chatDialog){
        ChatHelper.getInstance().getUsersFromDialog(chatDialog, new QbEntityCallbackImpl<ArrayList<QBUser>>());
    }

    public void onGlobalMessageReceived(String dialogId, QBChatMessage chatMessage){
        if (chatMessage.getBody() != null && chatMessage.isMarkable()) { //for excluding status messages until will be released v.3.1
            if (QbDialogHolder.getInstance().hasDialogWithId(dialogId)) {
                QbDialogHolder.getInstance().updateDialog(dialogId, chatMessage);
                notifyListenersDialogUpdated(dialogId);
            } else {
                ChatHelper.getInstance().getDialogById(dialogId, new QbEntityCallbackImpl<QBChatDialog>() {
                    @Override
                    public void onSuccess(QBChatDialog chatDialog, Bundle bundle) {
                        loadUsersFromDialog(chatDialog);
                        QbDialogHolder.getInstance().addDialog(chatDialog);
                        notifyListenersNewDialogLoaded(chatDialog);
                    }
                });
            }
        }
    }

    public void onSystemMessageReceived(QBChatMessage systemMessage){
        if (isMessageCreatingDialog(systemMessage)) {
            QBChatDialog chatDialog = buildChatDialogFromSystemMessage(systemMessage);
            QbDialogHolder.getInstance().addDialog(chatDialog);
            notifyListenersDialogCreated(buildChatDialogFromSystemMessage(systemMessage));
        }
    }

    private void notifyListenersDialogCreated(final QBChatDialog chatDialog){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                for (ManagingDialogsCallbacks listener : getManagingDialogsCallbackListeners()){
                    listener.onDialogCreated(chatDialog);
                }
            }
        });
    }

    private void notifyListenersDialogUpdated(final String dialogId){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                for (ManagingDialogsCallbacks listener : getManagingDialogsCallbackListeners()){
                    listener.onDialogUpdated(dialogId);
                }
            }
        });
    }

    private void notifyListenersNewDialogLoaded(final QBChatDialog chatDialog){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                for (ManagingDialogsCallbacks listener : getManagingDialogsCallbackListeners()){
                    listener.onNewDialogLoaded(chatDialog);
                }
            }
        });
    }

    public void addMagingDialogsCallbackListener(ManagingDialogsCallbacks listener){
        if (listener != null){
            magingDialogsCallbackListener.add(listener);
        }
    }

    public void removeMagingDialogsCallbackListener(ManagingDialogsCallbacks listener) {
        magingDialogsCallbackListener.remove(listener);
    }

    public Collection<ManagingDialogsCallbacks> getManagingDialogsCallbackListeners() {
        return Collections.unmodifiableCollection(magingDialogsCallbackListener);
    }

    public interface ManagingDialogsCallbacks{

        void onDialogCreated(QBChatDialog chatDialog);

        void onDialogUpdated(String chatDialog);

        void onNewDialogLoaded(QBChatDialog chatDialog);
    }
}
