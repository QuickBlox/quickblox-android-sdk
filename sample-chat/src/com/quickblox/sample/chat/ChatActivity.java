package com.quickblox.sample.chat;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.quickblox.module.chat.QBChat;
import com.quickblox.module.users.model.QBUser;
import org.jivesoftware.smack.packet.Message;

/**
 * Date: 24.10.12
 * Time: 22:16
 */

/**
 * List of users QuickBlox applications available to chat.
 *
 * @author <a href="mailto:oleg@quickblox.com">Oleg Soroka</a>
 */
public class ChatActivity extends Activity {

    private EditText messageText;
    private TextView meLabel;
    private TextView friendLabel;
    private ViewGroup messagesContainer;
    private ScrollView scrollContainer;

    private MyChatController myChatController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);

        // Load QBUser objects from bundle (passed from previous activity).
        Bundle extras = getIntent().getExtras();

        QBUser me = new QBUser();
        me.setId(extras.getInt("myId"));
        me.setLogin(extras.getString("myLogin"));
        me.setPassword(extras.getString("myPassword"));

        QBUser friend = new QBUser();
        friend.setId(extras.getInt("friendId"));
        friend.setLogin(extras.getString("friendLogin"));
        friend.setPassword(extras.getString("friendPassword"));

        // UI stuff
        messagesContainer = (ViewGroup) findViewById(R.id.messagesContainer);
        scrollContainer = (ScrollView) findViewById(R.id.scrollContainer);

        Button sendMessageButton = (Button) findViewById(R.id.sendButton);
        sendMessageButton.setOnClickListener(onSendMessageClickListener);

        messageText = (EditText) findViewById(R.id.messageEdit);
        meLabel = (TextView) findViewById(R.id.meLabel);
        friendLabel = (TextView) findViewById(R.id.friendLabel);
        meLabel.setText(me.getLogin() + " (me)");
        friendLabel.setText(friend.getLogin());

        // ================= QuickBlox ===== Step 5 =================
        // Get chat login based on QuickBlox user account.
        // Note, that to start chat you should use only short login,
        // that looks like '17744-1028' (<qb_user_id>-<qb_app_id>).
        String chatLogin = QBChat.getChatLoginShort(me);

        // Our current (me) user's password.
        String password = me.getPassword();

        if (me != null && friend != null) {


            // ================= QuickBlox ===== Step 6 =================
            // All chat logic can be implemented by yourself using
            // ASMACK library (https://github.com/Flowdalic/asmack/downloads)
            // -- Android wrapper for Java XMPP library (http://www.igniterealtime.org/projects/smack/).
            myChatController = new MyChatController(chatLogin, password);
            myChatController.setOnMessageReceivedListener(onMessageReceivedListener);

            // ================= QuickBlox ===== Step 7 =================
            // Get friend's login based on QuickBlox user account.
            // Note, that for your companion you should use full chat login,
            // that looks like '17792-1028@chat.quickblox.com' (<qb_user_id>-<qb_app_id>@chat.quickblox.com).
            // Don't use short login, it
            String friendLogin = QBChat.getChatLoginFull(friend);

            myChatController.startChat(friendLogin);
        }
    }

    private void sendMessage() {
        if (messageText != null) {
            String messageString = messageText.getText().toString();
            myChatController.sendMessage(messageString);
            messageText.setText("");
            showMessage(messageString, true);
        }
    }

    private MyChatController.OnMessageReceivedListener onMessageReceivedListener = new MyChatController.OnMessageReceivedListener() {
        @Override
        public void onMessageReceived(final Message message) {
            String messageString = message.getBody();
            showMessage(messageString, false);
        }
    };

    private void showMessage(String message, boolean leftSide) {
        final TextView textView = new TextView(ChatActivity.this);
        textView.setTextColor(Color.BLACK);
        textView.setText(message);

        int bgRes = R.drawable.left_message_bg;

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        if (!leftSide) {
            bgRes = R.drawable.right_message_bg;
            params.gravity = Gravity.RIGHT;
        }

        textView.setLayoutParams(params);

        textView.setBackgroundResource(bgRes);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messagesContainer.addView(textView);

                // Scroll to bottom
                if (scrollContainer.getChildAt(0) != null) {
                    scrollContainer.scrollTo(scrollContainer.getScrollX(), scrollContainer.getChildAt(0).getHeight());
                }
                scrollContainer.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    private View.OnClickListener onSendMessageClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            sendMessage();
        }
    };
}