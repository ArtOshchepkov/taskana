package pro.taskana.impl.integration;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import javax.security.auth.login.LoginException;
import javax.sql.DataSource;

import org.h2.store.fs.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import pro.taskana.Classification;
import pro.taskana.ClassificationService;
import pro.taskana.KeyDomain;
import pro.taskana.Task;
import pro.taskana.TaskSummary;
import pro.taskana.TaskanaEngine;
import pro.taskana.TaskanaEngine.ConnectionManagementMode;
import pro.taskana.Workbasket;
import pro.taskana.WorkbasketAccessItem;
import pro.taskana.WorkbasketService;
import pro.taskana.configuration.TaskanaEngineConfiguration;
import pro.taskana.exceptions.ClassificationAlreadyExistException;
import pro.taskana.exceptions.ClassificationNotFoundException;
import pro.taskana.exceptions.InvalidArgumentException;
import pro.taskana.exceptions.InvalidWorkbasketException;
import pro.taskana.exceptions.NotAuthorizedException;
import pro.taskana.exceptions.SystemException;
import pro.taskana.exceptions.TaskAlreadyExistException;
import pro.taskana.exceptions.TaskNotFoundException;
import pro.taskana.exceptions.WorkbasketNotFoundException;
import pro.taskana.impl.ClassificationImpl;
import pro.taskana.impl.JunitHelper;
import pro.taskana.impl.ObjectReference;
import pro.taskana.impl.TaskImpl;
import pro.taskana.impl.TaskServiceImpl;
import pro.taskana.impl.TaskState;
import pro.taskana.impl.TaskanaEngineImpl;
import pro.taskana.impl.WorkbasketImpl;
import pro.taskana.impl.WorkbasketType;
import pro.taskana.impl.configuration.DBCleaner;
import pro.taskana.impl.configuration.TaskanaEngineConfigurationTest;
import pro.taskana.security.CurrentUserContext;
import pro.taskana.security.JAASRunner;
import pro.taskana.security.WithAccessId;

/**
 * Integration Test for TaskServiceImpl transactions with connection management mode AUTOCOMMIT.
 *
 * @author EH
 */
@RunWith(JAASRunner.class)
public class TaskServiceImplIntAutocommitTest {

    private DataSource dataSource;
    private TaskServiceImpl taskServiceImpl;
    private TaskanaEngineConfiguration taskanaEngineConfiguration;
    private TaskanaEngine taskanaEngine;
    private TaskanaEngineImpl taskanaEngineImpl;
    private ClassificationService classificationService;
    private WorkbasketService workbasketService;

    @BeforeClass
    public static void resetDb() throws SQLException {
        DataSource ds = TaskanaEngineConfigurationTest.getDataSource();
        DBCleaner cleaner = new DBCleaner();
        cleaner.clearDb(ds, true);
        FileUtils.deleteRecursive("~/data", true);
    }

    @Before
    public void setup() throws FileNotFoundException, SQLException, LoginException {
        dataSource = TaskanaEngineConfigurationTest.getDataSource();
        taskanaEngineConfiguration = new TaskanaEngineConfiguration(dataSource, false, false);

        taskanaEngine = taskanaEngineConfiguration.buildTaskanaEngine();
        taskanaEngineImpl = (TaskanaEngineImpl) taskanaEngine;
        taskanaEngineImpl.setConnectionManagementMode(ConnectionManagementMode.AUTOCOMMIT);
        taskServiceImpl = (TaskServiceImpl) taskanaEngine.getTaskService();
        classificationService = taskanaEngine.getClassificationService();
        workbasketService = taskanaEngine.getWorkbasketService();
        DBCleaner cleaner = new DBCleaner();
        cleaner.clearDb(dataSource, false);
    }

    @Test
    public void testStart() throws FileNotFoundException, SQLException, TaskNotFoundException,
        WorkbasketNotFoundException, NotAuthorizedException, ClassificationNotFoundException,
        ClassificationAlreadyExistException, TaskAlreadyExistException, InvalidWorkbasketException,
        InvalidArgumentException {
        Workbasket wb = workbasketService.newWorkbasket("workbasket", "novatec");
        wb.setName("workbasket");
        wb.setType(WorkbasketType.GROUP);
        taskanaEngine.getWorkbasketService().createWorkbasket(wb);
        Classification classification = classificationService.newClassification("TEST", "novatec", "t1");
        taskanaEngine.getClassificationService().createClassification(classification);

        Task task = taskServiceImpl.newTask(wb.getId());
        task.setName("Unit Test Task");

        task.setClassificationKey(classification.getKey());
        task.setPrimaryObjRef(JunitHelper.createDefaultObjRef());
        task = taskServiceImpl.createTask(task);
        // skanaEngineImpl.getSqlSession().commit(); // needed so that the change is visible in the other session

        TaskanaEngine te2 = taskanaEngineConfiguration.buildTaskanaEngine();
        TaskServiceImpl taskServiceImpl2 = (TaskServiceImpl) te2.getTaskService();
        Task resultTask = taskServiceImpl2.getTask(task.getId());
        Assert.assertNotNull(resultTask);
    }

    @Test(expected = TaskNotFoundException.class)
    public void testStartTransactionFail()
        throws FileNotFoundException, SQLException, TaskNotFoundException, NotAuthorizedException,
        WorkbasketNotFoundException, ClassificationNotFoundException, ClassificationAlreadyExistException,
        TaskAlreadyExistException, InvalidWorkbasketException, InvalidArgumentException {
        Workbasket wb = workbasketService.newWorkbasket("wb1k1", "novatec");
        wb.setName("sdf");
        wb.setType(WorkbasketType.GROUP);
        taskanaEngine.getWorkbasketService().createWorkbasket(wb);

        Classification classification = classificationService.newClassification("TEST", "novatec", "t1");
        classification = taskanaEngine.getClassificationService()
            .createClassification(classification);
        classification = taskanaEngine.getClassificationService().getClassification(
            classification.getKey(),
            classification.getDomain());

        TaskImpl task = (TaskImpl) taskServiceImpl.newTask(wb.getKey(), "novatec");
        task.setName("Unit Test Task");
        task.setClassificationKey(classification.getKey());
        task.setPrimaryObjRef(JunitHelper.createDefaultObjRef());
        taskServiceImpl.createTask(task);
        taskServiceImpl.getTask(task.getId());

        TaskanaEngineImpl te2 = (TaskanaEngineImpl) taskanaEngineConfiguration.buildTaskanaEngine();
        TaskServiceImpl taskServiceImpl2 = (TaskServiceImpl) te2.getTaskService();
        taskServiceImpl2.getTask(wb.getId());
    }

    @Test
    public void testCreateTaskInTaskanaWithDefaultDb()
        throws FileNotFoundException, SQLException, TaskNotFoundException, NotAuthorizedException,
        WorkbasketNotFoundException, ClassificationNotFoundException, ClassificationAlreadyExistException,
        TaskAlreadyExistException, InvalidWorkbasketException, InvalidArgumentException {
        DBCleaner cleaner = new DBCleaner();
        cleaner.clearDb(TaskanaEngineConfiguration.createDefaultDataSource(), false);
        TaskanaEngineConfiguration taskanaEngineConfiguration = new TaskanaEngineConfiguration(null, false, false);
        TaskanaEngine te = taskanaEngineConfiguration.buildTaskanaEngine();
        ((TaskanaEngineImpl) te).setConnectionManagementMode(ConnectionManagementMode.AUTOCOMMIT);
        TaskServiceImpl taskServiceImpl = (TaskServiceImpl) te.getTaskService();

        Workbasket wb = workbasketService.newWorkbasket("workbasket", "novatec");
        wb.setName("workbasket");
        wb.setType(WorkbasketType.GROUP);
        te.getWorkbasketService().createWorkbasket(wb);
        Classification classification = te.getClassificationService().newClassification("TEST", "novatec", "t1");
        te.getClassificationService().createClassification(classification);

        Task task = taskServiceImpl.newTask(wb.getId());
        task.setName("Unit Test Task");
        task.setClassificationKey(classification.getKey());
        task.setPrimaryObjRef(JunitHelper.createDefaultObjRef());
        task = taskServiceImpl.createTask(task);

        Assert.assertNotNull(task);
        Assert.assertNotNull(task.getId());
    }

    @Test
    public void should_ReturnList_when_BuilderIsUsed() throws SQLException, NotAuthorizedException,
        WorkbasketNotFoundException, ClassificationNotFoundException, ClassificationAlreadyExistException,
        TaskAlreadyExistException, InvalidWorkbasketException, InvalidArgumentException, SystemException {
        Workbasket wb = workbasketService.newWorkbasket("key", "novatec");
        wb.setName("workbasket");
        wb.setType(WorkbasketType.GROUP);
        taskanaEngine.getWorkbasketService().createWorkbasket(wb);
        Classification classification = classificationService.newClassification("TEST", "novatec", "t1");
        taskanaEngine.getClassificationService().createClassification(classification);

        Task task = taskServiceImpl.newTask(wb.getKey(), wb.getDomain());
        task.setName("Unit Test Task");
        task.setClassificationKey(classification.getKey());
        task.setPrimaryObjRef(JunitHelper.createDefaultObjRef());
        taskServiceImpl.createTask(task);

        List<TaskSummary> results = taskServiceImpl.createTaskQuery()
            .nameIn("bla", "test")
            .descriptionLike("test")
            .priorityIn(1, 2, 2)
            .stateIn(TaskState.CLAIMED)
            .workbasketKeyDomainIn(new KeyDomain("asd", "novatec"), new KeyDomain("asdasdasd", "novatec"))
            .ownerIn("test", "test2", "bla")
            .customFieldsIn("test")
            .classificationKeyIn("pId1", "pId2")
            .primaryObjectReferenceCompanyIn("first comp", "sonstwo gmbh")
            .primaryObjectReferenceSystemIn("sys")
            .primaryObjectReferenceTypeIn("type1", "type2")
            .primaryObjectReferenceSystemInstanceIn("sysInst1", "sysInst2")
            .primaryObjectReferenceValueIn("val1", "val2", "val3")
            .list();

        Assert.assertEquals(0, results.size());
    }

    @Test
    public void shouldTransferTaskToOtherWorkbasket()
        throws WorkbasketNotFoundException, ClassificationNotFoundException, NotAuthorizedException,
        ClassificationAlreadyExistException, TaskNotFoundException, InterruptedException, TaskAlreadyExistException,
        InvalidWorkbasketException, InvalidArgumentException {
        Workbasket sourceWB;
        Workbasket destinationWB;
        WorkbasketImpl wb;
        ClassificationImpl classification;
        TaskImpl task;
        Task resultTask;
        final int sleepTime = 100;

        // Source Workbasket
        wb = (WorkbasketImpl) workbasketService.newWorkbasket("key1", "domain");
        wb.setName("Basic-Workbasket");
        wb.setDescription("Just used as base WB for Task here");
        wb.setType(WorkbasketType.GROUP);
        wb.setOwner("The Tester ID");
        sourceWB = workbasketService.createWorkbasket(wb);

        // Destination Workbasket
        wb = (WorkbasketImpl) workbasketService.newWorkbasket("k1", "domain");
        wb.setName("Desination-WorkBasket");

        wb.setType(WorkbasketType.CLEARANCE);
        wb.setDescription("Destination WB where Task should be transfered to");
        wb.setOwner("The Tester ID");
        destinationWB = workbasketService.createWorkbasket(wb);

        // Classification required for Task
        classification = (ClassificationImpl) classificationService.newClassification("KEY", "domain", "t1");
        classification.setCategory("Test Classification");
        classification.setName("Transfert-Task Classification");
        classificationService.createClassification(classification);

        // Task which should be transfered
        task = (TaskImpl) taskServiceImpl.newTask(sourceWB.getId());
        task.setName("Task Name");
        task.setDescription("Task used for transfer Test");
        task.setRead(true);
        task.setTransferred(false);
        task.setModified(null);
        task.setClassificationKey(classification.getKey());
        task.setPrimaryObjRef(JunitHelper.createDefaultObjRef());
        task = (TaskImpl) taskServiceImpl.createTask(task);
        Thread.sleep(sleepTime);    // Sleep for modification-timestamp

        resultTask = taskServiceImpl.transfer(task.getId(), destinationWB.getId());
        assertThat(resultTask.isRead(), equalTo(false));
        assertThat(resultTask.isTransferred(), equalTo(true));
        assertThat(resultTask.getWorkbasketSummary().getId(), equalTo(destinationWB.getId()));
        assertThat(resultTask.getModified(), not(equalTo(null)));
        assertThat(resultTask.getModified(), not(equalTo(task.getModified())));
        assertThat(resultTask.getCreated(), not(equalTo(null)));
        assertThat(resultTask.getCreated(), equalTo(task.getCreated()));
    }

    @Test(expected = TaskNotFoundException.class)
    public void shouldNotTransferAnyTask()
        throws WorkbasketNotFoundException, NotAuthorizedException, TaskNotFoundException, InvalidWorkbasketException,
        ClassificationNotFoundException {
        taskServiceImpl.transfer(UUID.randomUUID() + "_X", "1");
    }

    @WithAccessId(userName = "User")
    @Test
    public void shouldNotTransferByFailingSecurity() throws WorkbasketNotFoundException,
        ClassificationNotFoundException, NotAuthorizedException, ClassificationAlreadyExistException, SQLException,
        TaskNotFoundException, TaskAlreadyExistException, InvalidWorkbasketException, InvalidArgumentException {
        final String user = CurrentUserContext.getUserid();

        // Set up Security for this Test
        dataSource = TaskanaEngineConfigurationTest.getDataSource();
        taskanaEngineConfiguration = new TaskanaEngineConfiguration(dataSource, false, true);
        taskanaEngine = taskanaEngineConfiguration.buildTaskanaEngine();
        taskanaEngineImpl = (TaskanaEngineImpl) taskanaEngine;
        taskanaEngineImpl.setConnectionManagementMode(ConnectionManagementMode.AUTOCOMMIT);
        taskServiceImpl = (TaskServiceImpl) taskanaEngine.getTaskService();
        classificationService = taskanaEngine.getClassificationService();
        workbasketService = taskanaEngine.getWorkbasketService();

        ClassificationImpl classification = (ClassificationImpl) classificationService.newClassification("KEY",
            "test-domain", "t1");
        classification.setCategory("Test Classification");
        classification.setName("Transfert-Task Classification");
        classificationService.createClassification(classification);

        WorkbasketImpl wb = (WorkbasketImpl) workbasketService.newWorkbasket("k5", "test-domain");
        wb.setName("BASE WB");
        wb.setDescription("Normal base WB");
        wb.setOwner(user);
        wb.setType(WorkbasketType.TOPIC);
        wb = (WorkbasketImpl) workbasketService.createWorkbasket(wb);
        createWorkbasketWithSecurity(wb, wb.getOwner(), true, true, true, true);

        WorkbasketImpl wbNoAppend = (WorkbasketImpl) workbasketService.newWorkbasket("key77", "d2");
        wbNoAppend.setName("Test-Security-WorkBasket-APPEND");
        wbNoAppend.setDescription("Workbasket without permission APPEND on Task");
        wbNoAppend.setOwner(user);
        wbNoAppend.setType(WorkbasketType.PERSONAL);

        wbNoAppend = (WorkbasketImpl) workbasketService.createWorkbasket(wbNoAppend);
        createWorkbasketWithSecurity(wbNoAppend, wbNoAppend.getOwner(), true, true, false, true);

        WorkbasketImpl wbNoTransfer = (WorkbasketImpl) workbasketService.newWorkbasket("k99", "test-domain");
        wbNoTransfer.setName("Test-Security-WorkBasket-TRANSFER");
        wbNoTransfer.setDescription("Workbasket without permission TRANSFER on Task");
        wbNoTransfer.setOwner(user);
        wbNoTransfer.setType(WorkbasketType.CLEARANCE);

        wbNoTransfer = (WorkbasketImpl) workbasketService.createWorkbasket(wbNoTransfer);
        createWorkbasketWithSecurity(wbNoTransfer, wbNoTransfer.getOwner(), true, true, true, false);

        TaskImpl task = (TaskImpl) taskServiceImpl.newTask(wb.getId());
        task.setName("Task Name");
        task.setDescription("Task used for transfer Test");
        task.setOwner(user);
        task.setClassificationKey(classification.getKey());
        task.setPrimaryObjRef(JunitHelper.createDefaultObjRef());
        task = (TaskImpl) taskServiceImpl.createTask(task);

        // Check failing with missing APPEND
        try {
            task = (TaskImpl) taskServiceImpl.transfer(task.getId(), wbNoAppend.getId());
            fail("Transfer Task should be FAILD, because there are no APPEND-Rights on destination WB.");
        } catch (NotAuthorizedException e) {
            if (!e.getMessage().contains("APPEND")) {
                fail("Transfer Task should be FAILD, because there are no APPEND-Rights on destination WB.");
            }
            assertThat(task.isTransferred(), equalTo(false));
            assertThat(task.getWorkbasketKey(), not(equalTo(wbNoAppend.getKey())));
            assertThat(task.getWorkbasketKey(), equalTo(wb.getKey()));
        }

        // Check failing with missing TRANSFER
        task.setId("");
        task.getWorkbasketSummaryImpl().setId(wbNoTransfer.getId());
        task.setWorkbasketKey(null);
        task = (TaskImpl) taskServiceImpl.createTask(task);
        try {
            task = (TaskImpl) taskServiceImpl.transfer(task.getId(), wb.getId());
            fail("Transfer Task should be FAILD, because there are no TRANSFER-Rights on current WB.");
        } catch (NotAuthorizedException e) {
            if (!e.getMessage().contains("TRANSFER")) {
                fail("Transfer Task should be FAILD, because there are no APPEND-Rights on current WB.");
            }
            assertThat(task.isTransferred(), equalTo(false));
            assertThat(task.getWorkbasketKey(), not(equalTo(wbNoAppend.getKey())));
        }
    }

    @Test
    public void testWithPrimaryObjectRef() throws FileNotFoundException, SQLException, TaskNotFoundException,
        WorkbasketNotFoundException, NotAuthorizedException, ClassificationNotFoundException,
        ClassificationAlreadyExistException, TaskAlreadyExistException, InvalidWorkbasketException,
        InvalidArgumentException {
        Workbasket wb = workbasketService.newWorkbasket("workbasket", "novatec");
        wb.setName("workbasket");
        wb.setType(WorkbasketType.GROUP);
        taskanaEngine.getWorkbasketService().createWorkbasket(wb);
        Classification classification = classificationService.newClassification("TEST", "novatec", "t1");
        taskanaEngine.getClassificationService().createClassification(classification);

        Task task = taskServiceImpl.newTask(wb.getId());
        task.setName("Unit Test Task");

        task.setClassificationKey(classification.getKey());

        ObjectReference objRef = new ObjectReference();
        objRef.setCompany("novatec");
        objRef.setSystem("linux");
        objRef.setSystemInstance("inst1");
        objRef.setType("fast");
        objRef.setValue("4711");
        task.setPrimaryObjRef(objRef);

        task = taskServiceImpl.createTask(task);

        Task task2 = taskServiceImpl.getTask(task.getId());
        // skanaEngineImpl.getSqlSession().commit(); // needed so that the change is visible in the other session

        Assert.assertNotNull(task2);
    }

    private void createWorkbasketWithSecurity(Workbasket wb, String accessId, boolean permOpen,
        boolean permRead, boolean permAppend, boolean permTransfer) {
        WorkbasketAccessItem accessItem = workbasketService.newWorkbasketAccessItem(wb.getId(), accessId);
        accessItem.setPermOpen(permOpen);
        accessItem.setPermRead(permRead);
        accessItem.setPermAppend(permAppend);
        accessItem.setPermTransfer(permTransfer);
        workbasketService.createWorkbasketAuthorization(accessItem);
    }

    @AfterClass
    public static void cleanUpClass() {
        FileUtils.deleteRecursive("~/data", true);
    }
}
