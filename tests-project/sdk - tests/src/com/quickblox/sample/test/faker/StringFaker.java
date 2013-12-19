package com.quickblox.sample.test.faker;

import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: vfite
 * Date: 28.11.13
 * Time: 15:42
 * To change this template use File | Settings | File Templates.
 */
public class StringFaker {
    private static String az = "abcdefghijklmnopqrstuvwxyz";

    private static String n = "0123456789";


    private static int defMinLength = 8;


    public static String getPassword() {

        return getPassword(defMinLength);

    }


    public static String getPassword(int length) {

        StringBuilder builder = new StringBuilder();


        for (int i = 0; i < length; i++) {

            builder.append(getChar());

        }


        return builder.toString();

    }

    public static String getRandomString(int length) {

        StringBuilder builder = new StringBuilder();


        for (int i = 0; i < length; i++) {

            builder.append(getChar());

        }


        return builder.toString();

    }


    public static char getChar() {

        String allSymbols = az + n;

        int length = allSymbols.length();

        int n = NumberFaker.getInt(length);

        return allSymbols.charAt(n);

    }


    public static String getRandomFromArray(String[] items) {

        int count = items.length;

        return items[(new Random()).nextInt(count)];

    }
}
