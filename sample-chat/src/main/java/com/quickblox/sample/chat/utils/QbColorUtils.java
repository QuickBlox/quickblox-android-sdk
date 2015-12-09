package com.quickblox.sample.chat.utils;

import com.quickblox.sample.chat.App;

import java.util.Random;

public class QbColorUtils {

    private static final Random random = new Random();
    private static int previousColor;

    private QbColorUtils() {}

    public static int getRandomColor() {
        int randomNumber = random.nextInt(10) + 1;
        String colorIdName = String.format("random_color_%d", randomNumber);

        int colorId = App.getInstance().getResources().getIdentifier(colorIdName, "color", App.getInstance().getPackageName());

        int generatedColor = App.getInstance().getResources().getColor(colorId);
        if (generatedColor != previousColor) {
            previousColor = generatedColor;
            return generatedColor;
        } else {
            do {
                generatedColor = getRandomColor();
            } while (generatedColor != previousColor);
        }
        return previousColor;
    }
}
