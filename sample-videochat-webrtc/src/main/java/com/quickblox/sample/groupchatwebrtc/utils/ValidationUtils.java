package com.quickblox.sample.groupchatwebrtc.utils;

import android.content.Context;
import android.widget.EditText;

import com.quickblox.sample.groupchatwebrtc.R;

/**
 * Created by tereha on 03.06.16.
 */
public class ValidationUtils {

    private static boolean isUserNameOrRoomNameValid(Context context, EditText editText, String text, boolean isName) {

        int counterSymbols = 0;
        char[] fullNameTextToCharArray = text.toCharArray();
        char[] symbols = {'<', '>', ';', '^'};

        for (int i = 0; i < fullNameTextToCharArray.length; i++) {
            for (int j = 0; j < symbols.length; j++) {
                if (fullNameTextToCharArray[i] == symbols[j]) {
                    counterSymbols++;
                    break;
                }
            }
        }

        if (!text.isEmpty()) {
            if (text.length() < 3) {
                editText.setError(String.format(
                        context.getString(R.string.error_name_must_be_more_than_2_characters_from_app),
                        context.getString(isName
                                ? R.string.field_name_user_name
                                : R.string.field_name_chat_room_name)));
                return false;
            } else if (text.length() > 50){
                editText.setError(String.format(
                        context.getString(R.string.error_is_too_long_maximum_is_50_characters_from_app),
                        context.getString(isName
                                ? R.string.field_name_user_name
                                : R.string.field_name_chat_room_name)));
                return false;
            } else if (counterSymbols != 0) {
                editText.setError(String.format(
                        context.getString(R.string.error_name_must_do_not_contain_special_characters_from_app),
                        context.getString(isName
                                ? R.string.field_name_user_name
                                : R.string.field_name_chat_room_name)));
                return false;
            } else {
                return true;
            }
        } else {
            editText.setError(String.format(
                    context.getString(R.string.dlg_not_fullname_field_entered),
                    context.getString(isName
                            ? R.string.field_name_user_name
                            : R.string.field_name_chat_room_name)));
            return false;
        }
    }

    public static boolean isUserNameValid(Context context, EditText editText, String text){
        return isUserNameOrRoomNameValid(context, editText, text, true);
    }

    public static boolean isRoomNameValid(Context context, EditText editText, String text){
        return isUserNameOrRoomNameValid(context, editText, text, false);
    }
}
