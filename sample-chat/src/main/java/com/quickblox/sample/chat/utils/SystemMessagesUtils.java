package com.quickblox.sample.chat.utils;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBSystemMessagesManager;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.sample.chat.utils.qb.QbDialogUtils;

import org.jivesoftware.smack.SmackException;

public class SystemMessagesUtils {

    public static final String PROPERTY_OCCUPANTS_IDS = "occupants_ids";
    public static final String PROPERTY_DIALOG_TYPE = "dialog_type";
    public static final String PROPERTY_NOTIFICATION_TYPE = "notification_type";
    public static final String CREATING_DIALOG = "creating_dialog";

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
}
