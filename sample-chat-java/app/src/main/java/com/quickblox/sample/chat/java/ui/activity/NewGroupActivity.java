package com.quickblox.sample.chat.java.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.sample.chat.java.R;
import com.quickblox.sample.chat.java.utils.ValidationUtils;

import androidx.annotation.Nullable;

public class NewGroupActivity extends BaseActivity{
    private static final long CLICK_DELAY = 1000;

    private Menu menu;
    private EditText etDialogName;
    private TextView tvDialogNameHint;
    private ImageView ivClear;
    private long lastClickTime = 0;

    public static void startForResult(Activity activity, int code) {
        Intent intent = new Intent(activity, NewGroupActivity.class);
        activity.startActivityForResult(intent, code);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_chat_name);
        etDialogName = findViewById(R.id.et_dialog_name);
        tvDialogNameHint = findViewById(R.id.tv_group_name_hint);
        ivClear = findViewById(R.id.iv_clear);
        etDialogName.addTextChangedListener(new TextWatcherListener(etDialogName));
        ivClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etDialogName.setText("");
            }
        });
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.select_users_create_chat_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_activity_new_group, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (SystemClock.uptimeMillis() - lastClickTime < CLICK_DELAY) {
            return super.onOptionsItemSelected(item);
        }
        lastClickTime = SystemClock.uptimeMillis();

        if (item.getItemId() == R.id.menu_finish) {
            passResultToCallerActivity();
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void passResultToCallerActivity() {
        String dialogName = etDialogName.getText().toString().trim();
        Intent intent = new Intent();
        if (!TextUtils.isEmpty(dialogName)) {
            intent.putExtra(SelectUsersActivity.EXTRA_CHAT_NAME, dialogName);
        }
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private void validateFields() {
        menu.getItem(0).setVisible(ValidationUtils.isDialogNameValid(etDialogName));
        tvDialogNameHint.setVisibility(ValidationUtils.isDialogNameValid(etDialogName)? View.GONE : View.VISIBLE);
    }

    private class TextWatcherListener implements TextWatcher {

        private EditText editText;

        public TextWatcherListener(EditText editText){
            this.editText = editText;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String text = s.toString().replace("  ", " ");
            if (!editText.getText().toString().equals(text)) {
                editText.setText(text);
                editText.setSelection(text.length());
            }
            validateFields();
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }
}