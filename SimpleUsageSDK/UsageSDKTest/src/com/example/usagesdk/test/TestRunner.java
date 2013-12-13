package com.example.usagesdk.test;

import android.test.InstrumentationTestRunner;
import android.test.InstrumentationTestSuite;
import com.example.usagesdk.test.customobject.*;
import junit.framework.TestSuite;

/**
 * Created with IntelliJ IDEA.
 * User: vfite
 * Date: 28.11.13
 * Time: 18:49
 * To change this template use File | Settings | File Templates.
 */
public class TestRunner extends InstrumentationTestRunner {

    @Override
    public TestSuite getAllTests() {
        TestConfig.loadConfig();
        TestSuite suite = new InstrumentationTestSuite(this);
        suite.addTestSuite(TestCreateCustomObject.class);
        suite.addTestSuite(TestDeleteObject.class);
        suite.addTestSuite(TestGetObject.class);
        suite.addTestSuite(TestGetObjects.class);
        suite.addTestSuite(TestUpdateObject.class);
        return suite;
    }

    @Override

    public ClassLoader getLoader() {
        return TestRunner.class.getClassLoader();

    }
}
