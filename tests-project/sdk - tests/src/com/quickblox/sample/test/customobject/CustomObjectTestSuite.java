package com.quickblox.sample.test.customobject;

import android.app.Instrumentation;
import android.test.InstrumentationTestSuite;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Created by vfite on 13.12.13.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({TestGetObject.class})
public class CustomObjectTestSuite {

    @BeforeClass
    public static void setUp() {
        System.out.println("setting up");
    }

    @AfterClass
    public static void tearDown() {
        System.out.println("tearing down");
    }


}
