package com.quickblox.sample.test.faker;

import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: vfite
 * Date: 28.11.13
 * Time: 15:44
 * To change this template use File | Settings | File Templates.
 */
public class BooleanFaker {
    public static boolean getNext() {

        return (new Random()).nextBoolean();

    }
}
