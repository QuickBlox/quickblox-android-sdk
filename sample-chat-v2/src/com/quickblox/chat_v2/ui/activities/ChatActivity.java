package com.quickblox.chat_v2.ui.activities;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.widget.TopBar;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew Dmitrenko
 * Date: 4/11/13
 * Time: 12:53 PM
 */
public class ChatActivity extends Activity {

    private TopBar topBar;
    private ViewGroup messagesContainer;
    private ScrollView scrollContainer;
    private EditText msgTxt;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_layout);
        initViews();
    }

    private void initViews() {
        topBar = (TopBar) findViewById(R.id.top_bar);
        topBar.setFragmentName(TopBar.CHAT_ACTIVITY);
        messagesContainer = (ViewGroup) findViewById(R.id.messagesContainer);
        scrollContainer = (ScrollView) findViewById(R.id.scrollContainer);
        msgTxt = (EditText)findViewById(R.id.messageEdit);
        showMessage("hello", true);
        showMessage("hello", false);
        showMessage("hi", false);
    }

    public void onSendBtnClick(View view) {
        showMessage(msgTxt.getText().toString(), true);
    }

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


}
