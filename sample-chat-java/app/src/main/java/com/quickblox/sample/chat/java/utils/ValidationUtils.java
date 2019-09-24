package com.quickblox.sample.chat.java.utils;

import android.content.Context;
import android.text.TextUtils;
import android.widget.EditText;


import com.quickblox.sample.chat.java.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidationUtils {

    private static final int MAX_LOGIN_LENGTH = 15;
    private static final int MAX_FULLNAME_LENGTH = 20;

    private ValidationUtils() {

    }

    private static boolean isEnteredTextValid(Context context, EditText editText, int resFieldName, int maxLength, boolean checkLogin) {
        boolean isCorrect = false;
        Pattern pattern;
        if (checkLogin) {
            pattern = Pattern.compile("^[a-zA-Z][a-zA-Z0-9]{2," + (maxLength - 1) + "}+$");
        } else {
            pattern = Pattern.compile("^[a-zA-Z][a-zA-Z 0-9]{2," + (maxLength - 1) + "}+$");
        }

        if (!TextUtils.isEmpty(editText.getText().toString())) {
            Matcher matcher = pattern.matcher(editText.getText().toString().trim());
            isCorrect = matcher.matches();
        }

        if (!isCorrect) {
            editText.setError(String.format(context.getString(R.string.error_name_must_not_contain_special_characters_from_app), context.getString(resFieldName), String.valueOf(maxLength)));
            editText.requestFocus();
        }

        return isCorrect;
    }

    public static boolean isLoginValid(Context context, EditText editText) {
        return isEnteredTextValid(context, editText, R.string.field_name_user_login, MAX_LOGIN_LENGTH, true);
    }

    public static boolean isFullNameValid(Context context, EditText editText) {
        return isEnteredTextValid(context, editText, R.string.field_name_user_fullname, MAX_FULLNAME_LENGTH, false);
    }
}