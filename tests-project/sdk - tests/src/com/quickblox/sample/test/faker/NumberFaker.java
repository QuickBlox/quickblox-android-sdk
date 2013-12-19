package com.quickblox.sample.test.faker;

import java.math.BigDecimal;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: vfite
 * Date: 28.11.13
 * Time: 15:40
 * To change this template use File | Settings | File Templates.
 */
public class NumberFaker {
    public static Random rand = new Random();


    public static int getInt() {

        return getInt(100, 999);

    }


    // Returns [0, topBound)

    public static int getInt(int topBound) {

        return getInt(0, topBound -1);

    }


    // Returns [bottomBound, topBound]

    public static int getInt(int bottomBound, int topBound) {

        if (topBound > bottomBound) {

            int k = topBound - bottomBound;

            int r = rand.nextInt(k + 1);

            int result = r + bottomBound;

            return result;

        } else if (topBound == bottomBound) {

            return topBound;

        } else {

            return 0;

        }

    }


    // If bound < 0, returns [bound, 0), else returns (0; bound]

    public static int getIntWithRespectToZero(int bound) {

        if (bound == 0) {

            return 0;

        } else if (bound > 0) {

            return rand.nextInt(bound) + 1;

        } else {

            int r = rand.nextInt(bound) + 1;

            return r;

        }

    }


    public static double getDouble(double bottomBound, double topBound, int accuracy) {

        double random = new Random().nextDouble();

        double result = bottomBound + (random * (topBound - bottomBound));

        BigDecimal x = new BigDecimal(result);

        x = x.setScale(accuracy, BigDecimal.ROUND_HALF_UP);

        return x.doubleValue();

    }
}
