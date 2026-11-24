package ar.com.osmosys.pswdplus;

import com.haulmont.cuba.testsupport.TestContainer;

import java.util.ArrayList;
import java.util.Arrays;

public class PswdplusTestContainer extends TestContainer {

    public PswdplusTestContainer() {
        super();
        appComponents = new ArrayList<>(Arrays.asList(
                "com.haulmont.cuba"
                // add CUBA premium add-ons here
                // "com.haulmont.bpm",
                // "com.haulmont.charts",
                // "com.haulmont.fts",
                // "com.haulmont.reports",
                // and custom app components if any
        ));
        appPropertiesFiles = Arrays.asList(
                // List the files defined in your web.xml
                // in appPropertiesConfig context parameter of the core module
                "ar/com/osmosys/pswdplus/app.properties",
                // Add this file which is located in CUBA and defines some properties
                // specifically for test environment. You can replace it with your own
                // or add another one in the end.
                "com/haulmont/cuba/testsupport/test-app.properties",
                // Overrides the production settings that don't apply to the HSQLDB test database
                "ar/com/osmosys/pswdplus/test-app.properties");
        initDbProperties();
    }

    /*
    Tests run against a local HSQLDB database, started and recreated by the
    createTestDb gradle task (connection settings must match that task)
     */
    private void initDbProperties() {
        dbDriver = "org.hsqldb.jdbc.JDBCDriver";
        dbUrl = "jdbc:hsqldb:hsql://localhost:9111/pswdplus_test";
        dbUser = "sa";
        dbPassword = "";
    }

    public static class Common extends PswdplusTestContainer {

        public static final PswdplusTestContainer.Common INSTANCE = new PswdplusTestContainer.Common();

        private static volatile boolean initialized;

        private Common() {
        }

        @Override
        public void before() throws Throwable {
            if (!initialized) {
                super.before();
                initialized = true;
            }
            setupContext();
        }

        @Override
        public void after() {
            cleanupContext();
            // never stops - do not call super
        }
    }
}