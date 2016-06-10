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

        boolean isCorrect;
        Pattern p = Pattern.compile("\\A[^><;]{3,50}\\z");
        Matcher m = p.matcher(text);
        isCorrect = m.matches();

        if (!isCorrect) {
            editText.setError(String.format(
                    context.getString(R.string.error_name_must_do_not_contain_special_characters_from_app),
                    context.getString(isName
                            ? R.string.field_name_user_name
                            : R.string.field_name_chat_room_name)));
            return false;
        } else {
            return true;
        }
    }

    public static boolean isUserNameValid(Context context, EditText editText, String text){
        return isUserNameOrRoomNameValid(context, editText, text, true);
    }

    public static boolean isRoomNameValid(Context context, EditText editText, String text){
        return isUserNameOrRoomNameValid(context, editText, text, false);
    }
}
