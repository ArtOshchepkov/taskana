package acceptance.report;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pro.taskana.common.api.exceptions.InvalidArgumentException;
import pro.taskana.common.api.exceptions.NotAuthorizedException;
import pro.taskana.monitor.api.MonitorService;
import pro.taskana.monitor.api.reports.CustomFieldValueReport;
import pro.taskana.monitor.api.reports.header.TimeIntervalColumnHeader;
import pro.taskana.security.JaasExtension;
import pro.taskana.security.WithAccessId;
import pro.taskana.task.api.CustomField;
import pro.taskana.task.api.TaskState;

/** Acceptance test for all "classification report" scenarios. */
@ExtendWith(JaasExtension.class)
class ProvideCustomFieldValueReportAccTest extends AbstractReportAccTest {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(ProvideCustomFieldValueReportAccTest.class);

  @Test
  void testRoleCheck() {
    MonitorService monitorService = taskanaEngine.getMonitorService();

    Assertions.assertThrows(
        NotAuthorizedException.class,
        () ->
            monitorService.createCustomFieldValueReportBuilder(CustomField.CUSTOM_1).buildReport());
  }

  @WithAccessId(userName = "monitor")
  @Test
  void testGetTotalNumbersOfTasksOfCustomFieldValueReportForCustom1()
      throws InvalidArgumentException, NotAuthorizedException {
    MonitorService monitorService = taskanaEngine.getMonitorService();

    CustomFieldValueReport report =
        monitorService.createCustomFieldValueReportBuilder(CustomField.CUSTOM_1).buildReport();

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(reportToString(report));
    }

    assertNotNull(report);
    assertEquals(3, report.rowSize());

    assertEquals(25, report.getRow("Geschaeftsstelle A").getTotalValue());
    assertEquals(10, report.getRow("Geschaeftsstelle B").getTotalValue());
    assertEquals(15, report.getRow("Geschaeftsstelle C").getTotalValue());
    assertEquals(0, report.getRow("Geschaeftsstelle A").getCells().length);
    assertEquals(0, report.getRow("Geschaeftsstelle B").getCells().length);
    assertEquals(0, report.getRow("Geschaeftsstelle C").getCells().length);

    assertEquals(50, report.getSumRow().getTotalValue());
  }

  @WithAccessId(userName = "monitor")
  @Test
  void testGetTotalNumbersOfTasksOfCustomFieldValueReportForCustom2()
      throws InvalidArgumentException, NotAuthorizedException {
    MonitorService monitorService = taskanaEngine.getMonitorService();

    CustomFieldValueReport report =
        monitorService.createCustomFieldValueReportBuilder(CustomField.CUSTOM_2).buildReport();

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(reportToString(report));
    }

    assertNotNull(report);
    assertEquals(2, report.rowSize());

    assertEquals(21, report.getRow("Vollkasko").getTotalValue());
    assertEquals(29, report.getRow("Teilkasko").getTotalValue());

    assertEquals(0, report.getRow("Vollkasko").getCells().length);
    assertEquals(0, report.getRow("Teilkasko").getCells().length);

    assertEquals(50, report.getSumRow().getTotalValue());
  }

  @WithAccessId(userName = "monitor")
  @Test
  void testGetCustomFieldValueReportWithReportLineItemDefinitions()
      throws InvalidArgumentException, NotAuthorizedException {
    MonitorService monitorService = taskanaEngine.getMonitorService();

    CustomField customField = CustomField.CUSTOM_1;
    List<TimeIntervalColumnHeader> columnHeaders = getListOfColumnHeaders();

    CustomFieldValueReport report =
        monitorService
            .createCustomFieldValueReportBuilder(customField)
            .withColumnHeaders(columnHeaders)
            .inWorkingDays()
            .buildReport();

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(reportToString(report, columnHeaders));
    }

    assertNotNull(report);
    assertEquals(3, report.rowSize());

    assertEquals(25, report.getRow("Geschaeftsstelle A").getTotalValue());
    assertEquals(10, report.getRow("Geschaeftsstelle B").getTotalValue());
    assertEquals(15, report.getRow("Geschaeftsstelle C").getTotalValue());

    assertArrayEquals(new int[] {10, 9, 11, 0, 4, 0, 7, 4, 5}, report.getSumRow().getCells());

    assertEquals(50, report.getSumRow().getTotalValue());
  }

  @WithAccessId(userName = "monitor")
  @Test
  void testEachItemOfCustomFieldValueReport()
      throws InvalidArgumentException, NotAuthorizedException {
    MonitorService monitorService = taskanaEngine.getMonitorService();

    List<TimeIntervalColumnHeader> columnHeaders = getShortListOfColumnHeaders();

    CustomFieldValueReport report =
        monitorService
            .createCustomFieldValueReportBuilder(CustomField.CUSTOM_1)
            .withColumnHeaders(columnHeaders)
            .inWorkingDays()
            .buildReport();

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(reportToString(report, columnHeaders));
    }

    assertNotNull(report);
    assertEquals(3, report.rowSize());

    int[] row1 = report.getRow("Geschaeftsstelle A").getCells();
    assertArrayEquals(new int[] {11, 4, 3, 4, 3}, row1);

    int[] row2 = report.getRow("Geschaeftsstelle B").getCells();
    assertArrayEquals(new int[] {5, 3, 0, 2, 0}, row2);

    int[] row3 = report.getRow("Geschaeftsstelle C").getCells();
    assertArrayEquals(new int[] {3, 4, 1, 1, 6}, row3);
  }

  @WithAccessId(userName = "monitor")
  @Test
  void testEachItemOfCustomFieldValueReportNotInWorkingDays()
      throws InvalidArgumentException, NotAuthorizedException {
    MonitorService monitorService = taskanaEngine.getMonitorService();

    List<TimeIntervalColumnHeader> columnHeaders = getShortListOfColumnHeaders();

    CustomFieldValueReport report =
        monitorService
            .createCustomFieldValueReportBuilder(CustomField.CUSTOM_1)
            .withColumnHeaders(columnHeaders)
            .buildReport();

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(reportToString(report, columnHeaders));
    }

    assertNotNull(report);
    assertEquals(3, report.rowSize());

    int[] row1 = report.getRow("Geschaeftsstelle A").getCells();
    assertArrayEquals(new int[] {15, 0, 3, 0, 7}, row1);

    int[] row2 = report.getRow("Geschaeftsstelle B").getCells();
    assertArrayEquals(new int[] {8, 0, 0, 0, 2}, row2);

    int[] row3 = report.getRow("Geschaeftsstelle C").getCells();
    assertArrayEquals(new int[] {7, 0, 1, 0, 7}, row3);
  }

  @WithAccessId(userName = "monitor")
  @Test
  void testEachItemOfCustomFieldValueReportWithWorkbasketFilter()
      throws InvalidArgumentException, NotAuthorizedException {
    MonitorService monitorService = taskanaEngine.getMonitorService();

    List<String> workbasketIds =
        Collections.singletonList("WBI:000000000000000000000000000000000001");
    List<TimeIntervalColumnHeader> columnHeaders = getShortListOfColumnHeaders();

    CustomFieldValueReport report =
        monitorService
            .createCustomFieldValueReportBuilder(CustomField.CUSTOM_1)
            .withColumnHeaders(columnHeaders)
            .inWorkingDays()
            .workbasketIdIn(workbasketIds)
            .buildReport();

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(reportToString(report, columnHeaders));
    }

    assertNotNull(report);
    assertEquals(3, report.rowSize());

    int[] row1 = report.getRow("Geschaeftsstelle A").getCells();
    assertArrayEquals(new int[] {6, 1, 1, 1, 1}, row1);

    int[] row2 = report.getRow("Geschaeftsstelle B").getCells();
    assertArrayEquals(new int[] {4, 1, 0, 0, 0}, row2);

    int[] row3 = report.getRow("Geschaeftsstelle C").getCells();
    assertArrayEquals(new int[] {3, 1, 0, 0, 1}, row3);
  }

  @WithAccessId(userName = "monitor")
  @Test
  void testEachItemOfCustomFieldValueReportWithStateFilter()
      throws InvalidArgumentException, NotAuthorizedException {
    MonitorService monitorService = taskanaEngine.getMonitorService();

    List<TaskState> states = Collections.singletonList(TaskState.READY);
    List<TimeIntervalColumnHeader> columnHeaders = getShortListOfColumnHeaders();

    CustomFieldValueReport report =
        monitorService
            .createCustomFieldValueReportBuilder(CustomField.CUSTOM_1)
            .withColumnHeaders(columnHeaders)
            .inWorkingDays()
            .stateIn(states)
            .buildReport();

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(reportToString(report, columnHeaders));
    }

    assertNotNull(report);
    assertEquals(3, report.rowSize());

    int[] row1 = report.getRow("Geschaeftsstelle A").getCells();
    assertArrayEquals(new int[] {11, 4, 3, 4, 0}, row1);

    int[] row2 = report.getRow("Geschaeftsstelle B").getCells();
    assertArrayEquals(new int[] {5, 3, 0, 2, 0}, row2);

    int[] row3 = report.getRow("Geschaeftsstelle C").getCells();
    assertArrayEquals(new int[] {3, 4, 1, 1, 0}, row3);
  }

  @WithAccessId(userName = "monitor")
  @Test
  void testEachItemOfCustomFieldValueReportWithCategoryFilter()
      throws InvalidArgumentException, NotAuthorizedException {
    MonitorService monitorService = taskanaEngine.getMonitorService();

    List<String> categories = Arrays.asList("AUTOMATIC", "MANUAL");
    List<TimeIntervalColumnHeader> columnHeaders = getShortListOfColumnHeaders();

    CustomFieldValueReport report =
        monitorService
            .createCustomFieldValueReportBuilder(CustomField.CUSTOM_1)
            .withColumnHeaders(columnHeaders)
            .inWorkingDays()
            .categoryIn(categories)
            .buildReport();

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(reportToString(report, columnHeaders));
    }

    assertNotNull(report);
    assertEquals(3, report.rowSize());

    int[] row1 = report.getRow("Geschaeftsstelle A").getCells();
    assertArrayEquals(new int[] {2, 1, 2, 1, 3}, row1);

    int[] row2 = report.getRow("Geschaeftsstelle B").getCells();
    assertArrayEquals(new int[] {2, 0, 0, 0, 0}, row2);

    int[] row3 = report.getRow("Geschaeftsstelle C").getCells();
    assertArrayEquals(new int[] {0, 2, 0, 0, 4}, row3);
  }

  @WithAccessId(userName = "monitor")
  @Test
  void testEachItemOfCustomFieldValueReportWithDomainFilter()
      throws InvalidArgumentException, NotAuthorizedException {
    MonitorService monitorService = taskanaEngine.getMonitorService();

    List<String> domains = Collections.singletonList("DOMAIN_A");
    List<TimeIntervalColumnHeader> columnHeaders = getShortListOfColumnHeaders();

    CustomFieldValueReport report =
        monitorService
            .createCustomFieldValueReportBuilder(CustomField.CUSTOM_1)
            .withColumnHeaders(columnHeaders)
            .inWorkingDays()
            .domainIn(domains)
            .buildReport();

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(reportToString(report, columnHeaders));
    }

    assertNotNull(report);
    assertEquals(3, report.rowSize());

    int[] row1 = report.getRow("Geschaeftsstelle A").getCells();
    assertArrayEquals(new int[] {8, 1, 1, 4, 1}, row1);

    int[] row2 = report.getRow("Geschaeftsstelle B").getCells();
    assertArrayEquals(new int[] {2, 2, 0, 1, 0}, row2);

    int[] row3 = report.getRow("Geschaeftsstelle C").getCells();
    assertArrayEquals(new int[] {1, 1, 1, 0, 3}, row3);
  }

  @WithAccessId(userName = "monitor")
  @Test
  void testEachItemOfCustomFieldValueReportWithCustomFieldValueFilter()
      throws InvalidArgumentException, NotAuthorizedException {
    MonitorService monitorService = taskanaEngine.getMonitorService();

    Map<CustomField, String> customAttributeFilter = new HashMap<>();
    customAttributeFilter.put(CustomField.CUSTOM_1, "Geschaeftsstelle A");
    List<TimeIntervalColumnHeader> columnHeaders = getShortListOfColumnHeaders();

    CustomFieldValueReport report =
        monitorService
            .createCustomFieldValueReportBuilder(CustomField.CUSTOM_1)
            .customAttributeFilterIn(customAttributeFilter)
            .withColumnHeaders(columnHeaders)
            .inWorkingDays()
            .buildReport();

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(reportToString(report, columnHeaders));
    }

    assertNotNull(report);
    assertEquals(1, report.rowSize());

    int[] row1 = report.getRow("Geschaeftsstelle A").getCells();
    assertArrayEquals(new int[] {11, 4, 3, 4, 3}, row1);
  }

  private List<TimeIntervalColumnHeader> getListOfColumnHeaders() {
    List<TimeIntervalColumnHeader> columnHeaders = new ArrayList<>();
    columnHeaders.add(new TimeIntervalColumnHeader(Integer.MIN_VALUE, -11));
    columnHeaders.add(new TimeIntervalColumnHeader(-10, -6));
    columnHeaders.add(new TimeIntervalColumnHeader(-5, -2));
    columnHeaders.add(new TimeIntervalColumnHeader(-1));
    columnHeaders.add(new TimeIntervalColumnHeader(0));
    columnHeaders.add(new TimeIntervalColumnHeader(1));
    columnHeaders.add(new TimeIntervalColumnHeader(2, 5));
    columnHeaders.add(new TimeIntervalColumnHeader(6, 10));
    columnHeaders.add(new TimeIntervalColumnHeader(11, Integer.MAX_VALUE));
    return columnHeaders;
  }

  private List<TimeIntervalColumnHeader> getShortListOfColumnHeaders() {
    List<TimeIntervalColumnHeader> columnHeaders = new ArrayList<>();
    columnHeaders.add(new TimeIntervalColumnHeader(Integer.MIN_VALUE, -6));
    columnHeaders.add(new TimeIntervalColumnHeader(-5, -1));
    columnHeaders.add(new TimeIntervalColumnHeader(0));
    columnHeaders.add(new TimeIntervalColumnHeader(1, 5));
    columnHeaders.add(new TimeIntervalColumnHeader(6, Integer.MAX_VALUE));
    return columnHeaders;
  }

  private String reportToString(CustomFieldValueReport report) {
    return reportToString(report, null);
  }

  private String reportToString(
      CustomFieldValueReport report, List<TimeIntervalColumnHeader> columnHeaders) {
    String formatColumWidth = "| %-7s ";
    String formatFirstColumn = "| %-36s  %-4s ";
    final String formatFirstColumnFirstLine = "| %-29s %12s ";
    final String formatFirstColumnSumLine = "| %-36s  %-5s";
    int reportWidth = columnHeaders == null ? 46 : columnHeaders.size() * 10 + 46;

    StringBuilder builder = new StringBuilder();
    builder.append("\n");
    for (int i = 0; i < reportWidth; i++) {
      builder.append("-");
    }
    builder.append("\n");
    builder.append(String.format(formatFirstColumnFirstLine, "Custom field values", "Total"));
    if (columnHeaders != null) {
      for (TimeIntervalColumnHeader def : columnHeaders) {
        if (def.getLowerAgeLimit() == Integer.MIN_VALUE) {
          builder.append(String.format(formatColumWidth, "< " + def.getUpperAgeLimit()));
        } else if (def.getUpperAgeLimit() == Integer.MAX_VALUE) {
          builder.append(String.format(formatColumWidth, "> " + def.getLowerAgeLimit()));
        } else if (def.getLowerAgeLimit() == def.getUpperAgeLimit()) {
          if (def.getLowerAgeLimit() == 0) {
            builder.append(String.format(formatColumWidth, "today"));
          } else {
            builder.append(String.format(formatColumWidth, def.getLowerAgeLimit()));
          }
        } else {
          builder.append(
              String.format(
                  formatColumWidth, def.getLowerAgeLimit() + ".." + def.getUpperAgeLimit()));
        }
      }
    }
    builder.append("|\n");

    for (int i = 0; i < reportWidth; i++) {
      builder.append("-");
    }
    builder.append("\n");

    for (String rl : report.rowTitles()) {
      builder.append(String.format(formatFirstColumn, rl, report.getRow(rl).getTotalValue()));
      if (columnHeaders != null) {
        for (int cell : report.getRow(rl).getCells()) {
          builder.append(String.format(formatColumWidth, cell));
        }
      }
      builder.append("|\n");
      for (int i = 0; i < reportWidth; i++) {
        builder.append("-");
      }
      builder.append("\n");
    }
    builder.append(
        String.format(formatFirstColumnSumLine, "Total", report.getSumRow().getTotalValue()));
    for (int cell : report.getSumRow().getCells()) {
      builder.append(String.format(formatColumWidth, cell));
    }
    builder.append("|\n");
    for (int i = 0; i < reportWidth; i++) {
      builder.append("-");
    }
    builder.append("\n");
    return builder.toString();
  }
}
