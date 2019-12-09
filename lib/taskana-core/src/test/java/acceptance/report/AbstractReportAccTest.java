package acceptance.report;

import java.io.IOException;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.BeforeClass;
import org.junit.jupiter.api.BeforeAll;

import pro.taskana.TaskanaEngine;
import pro.taskana.configuration.TaskanaEngineConfiguration;
import pro.taskana.database.TestDataGenerator;
import pro.taskana.impl.configuration.TaskanaEngineConfigurationTest;
import pro.taskana.sampledata.DBCleaner;

/**
 * Abstract test class for all report building tests.
 */
public class AbstractReportAccTest {

    protected static TaskanaEngineConfiguration taskanaEngineConfiguration;
    protected static TaskanaEngine taskanaEngine;

    // checkstyle needs this constructor, since this is only a "utility" class
    protected AbstractReportAccTest() {
    }

    @BeforeClass
    @BeforeAll
    public static void setupTest() throws Exception {
        resetDb();
    }

    private static void resetDb() throws SQLException, IOException {
        DataSource dataSource = TaskanaEngineConfigurationTest.getDataSource();
        String schemaName = TaskanaEngineConfigurationTest.getSchemaName();
        DBCleaner cleaner = new DBCleaner();
        taskanaEngineConfiguration = new TaskanaEngineConfiguration(dataSource, false,
            schemaName);
        taskanaEngineConfiguration.setGermanPublicHolidaysEnabled(false);
        taskanaEngine = taskanaEngineConfiguration.buildTaskanaEngine();
        taskanaEngine.setConnectionManagementMode(TaskanaEngine.ConnectionManagementMode.AUTOCOMMIT);
        cleaner.clearDb(dataSource, schemaName);
        TestDataGenerator testDataGenerator = new TestDataGenerator();
        testDataGenerator.generateMonitoringTestData(dataSource);
    }
}
