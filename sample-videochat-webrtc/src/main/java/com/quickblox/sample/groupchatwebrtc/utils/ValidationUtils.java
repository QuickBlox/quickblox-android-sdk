package com.quickblox.sample.groupchatwebrtc.utils;

import android.content.Context;
import android.widget.EditText;

import com.quickblox.sample.groupchatwebrtc.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by tereha on 03.06.16.
 */
public class ValidationUtils {

    private static boolean isEnteredTextValid(Context context, EditText editText, int resFieldName, int maxLength, boolean checkName) {

        boolean isCorrect;
        Pattern p;
        if (checkName) {
            p = Pattern.compile("^[a-zA-Z][a-zA-Z 0-9]{2," + (maxLength - 1) + "}+$");
        } else {
            p = Pattern.compile("^[a-zA-Z][a-zA-Z0-9]{2," + (maxLength - 1) + "}+$");
        }

        Matcher m = p.matcher(editText.getText().toString().trim());
        isCorrect = m.matches();

        if (!isCorrect) {
            editText.setError(String.format(context.getString(R.string.error_name_must_not_contain_special_characters_from_app),
                    context.getString(resFieldName),
                    maxLength));
            return false;
        } else {
            return true;
        }
    }

    public static boolean isUserNameValid(Context context, EditText editText) {
        return isEnteredTextValid(context, editText, R.string.field_name_user_name, 20, true);
    }

    public static boolean isRoomNameValid(Context context, EditText editText) {
        return isEnteredTextValid(context, editText, R.string.field_name_chat_room_name, 15, false);
    }
}
