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

    private static boolean isUserNameOrRoomNameValid(Context context, EditText editText, String text, boolean isName) {

        final int USER_NAME_MAX_LENGTH = 50;
        final int ROOM_NAME_MAX_LENGTH = 15;

        int currentFieldMaxLength = isName ? USER_NAME_MAX_LENGTH : ROOM_NAME_MAX_LENGTH;

        boolean isCorrect;
        Pattern p = Pattern.compile("\\A[^><;]{3," + currentFieldMaxLength + "}\\z");
        Matcher m = p.matcher(text);
        isCorrect = m.matches();

        if (!isCorrect) {
            editText.setError(String.format(
                    context.getString(R.string.error_name_must_do_not_contain_special_characters_from_app),
                    context.getString(isName
                            ? R.string.field_name_user_name
                            : R.string.field_name_chat_room_name),
                    currentFieldMaxLength));
            return false;
        } else {
            return true;
        }
    }

    public static boolean isUserNameValid(Context context, EditText editText, String text){
        return isUserNameOrRoomNameValid(context, editText, text.trim(), true);
    }

    public static boolean isRoomNameValid(Context context, EditText editText, String text){
        return isUserNameOrRoomNameValid(context, editText, text.trim(), false);
    }
}
