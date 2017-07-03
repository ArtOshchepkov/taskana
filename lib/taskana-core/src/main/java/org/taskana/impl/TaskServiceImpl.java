package org.taskana.impl;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.taskana.TaskService;
import org.taskana.TaskanaEngine;
import org.taskana.exceptions.NotAuthorizedException;
import org.taskana.exceptions.TaskNotFoundException;
import org.taskana.exceptions.WorkbasketNotFoundException;
import org.taskana.model.DueWorkbasketCounter;
import org.taskana.model.Task;
import org.taskana.model.TaskState;
import org.taskana.model.TaskStateCounter;
import org.taskana.model.WorkbasketAuthorization;
import org.taskana.model.mappings.TaskMapper;

public class TaskServiceImpl implements TaskService {

	private static final Logger logger = LoggerFactory.getLogger(TaskServiceImpl.class);

	private TaskanaEngine taskanaEngine;
	private TaskMapper taskMapper;

	public TaskServiceImpl(TaskanaEngine taskanaEngine, TaskMapper taskMapper) {
		super();
		this.taskanaEngine = taskanaEngine;
		this.taskMapper = taskMapper;
	}

	@Override
	public void claim(String id, String userName) throws TaskNotFoundException {
		Task task = taskMapper.findById(id);
		if (task != null) {
			Timestamp now = new Timestamp(System.currentTimeMillis());
			task.setOwner(userName);
			task.setModified(now);
			task.setClaimed(now);
			task.setState(TaskState.CLAIMED);
			taskMapper.update(task);
			logger.debug("User '{}' claimed task '{}'.", userName, id);
		} else {
			throw new TaskNotFoundException(id);
		}
	}

	@Override
	public void complete(String id) throws TaskNotFoundException {
		Task task = taskMapper.findById(id);
		if (task != null) {
			Timestamp now = new Timestamp(System.currentTimeMillis());
			task.setCompleted(now);
			task.setModified(now);
			task.setState(TaskState.COMPLETED);
			taskMapper.update(task);
			logger.debug("Task '{}' completed.", id);
		} else {
			throw new TaskNotFoundException(id);
		}
	}

	@Override
	public Task create(Task task) throws NotAuthorizedException {
		taskanaEngine.getWorkbasketService().checkPermission(task.getWorkbasketId(), WorkbasketAuthorization.APPEND);

		Timestamp now = new Timestamp(System.currentTimeMillis());
		task.setId(UUID.randomUUID().toString());
		task.setState(TaskState.READY);
		task.setCreated(now);
		task.setModified(now);
		taskMapper.insert(task);
		logger.debug("Task '{}' created.", task.getId());
		return task;
	}

	@Override
	public Task getTaskById(String id) throws TaskNotFoundException {
		Task task = taskMapper.findById(id);
		if (task != null) {
			return task;
		} else {
			throw new TaskNotFoundException(id);
		}
	}

	@Override
	public List<Task> getTasksForWorkbasket(String workbasketId) throws NotAuthorizedException {
		taskanaEngine.getWorkbasketService().checkPermission(workbasketId, WorkbasketAuthorization.OPEN);

		return taskMapper.findByWorkBasketId(workbasketId);
	}

	@Override
	public List<Task> findTasks(List<TaskState> states) {
		return taskMapper.findByStates(states);
	}

	@Override
	public List<Task> getTasksForWorkbasket(List<String> workbasketIds, List<TaskState> states)
			throws NotAuthorizedException {

		for (String workbasket : workbasketIds) {
			taskanaEngine.getWorkbasketService().checkPermission(workbasket, WorkbasketAuthorization.OPEN);
		}

		return taskMapper.findByWorkbasketIdsAndStates(workbasketIds, states);
	}

	@Override
	public List<Task> getTasks() {
		return taskMapper.findAll();
	}

	@Override
	public List<TaskStateCounter> getTaskCountForState(List<TaskState> states) {
		return taskMapper.getTaskCountForState(states);
	}

	@Override
	public long getTaskCountForWorkbasketByDaysInPastAndState(String workbasketId, long daysInPast,
			List<TaskState> states) {
		LocalDate time = LocalDate.now();
		time = time.minusDays(daysInPast);
		Date fromDate = Date.valueOf(time);
		return taskMapper.getTaskCountForWorkbasketByDaysInPastAndState(workbasketId, fromDate, states);
	}

	@Override
	public Task transfer(String taskId, String destinationWorkbasketId)
			throws TaskNotFoundException, WorkbasketNotFoundException, NotAuthorizedException {
		Task task = getTaskById(taskId);

		// transfer requires TRANSFER in source and APPEND on destination
		// workbasket
		taskanaEngine.getWorkbasketService().checkPermission(destinationWorkbasketId, WorkbasketAuthorization.APPEND);
		taskanaEngine.getWorkbasketService().checkPermission(task.getWorkbasketId(), WorkbasketAuthorization.TRANSFER);

		// if security is disabled, the implicit existance check on the
		// destination workbasket has been skipped and needs to be performed
		if (!taskanaEngine.getConfiguration().isSecurityEnabled()) {
			taskanaEngine.getWorkbasketService().getWorkbasket(destinationWorkbasketId);
		}

		// transfer task from source to destination workbasket
		task.setWorkbasketId(destinationWorkbasketId);
		task.setModified(Timestamp.valueOf(LocalDateTime.now()));
		taskMapper.update(task);

		return getTaskById(taskId);
	}

	@Override
	public List<DueWorkbasketCounter> getTaskCountByWorkbasketAndDaysInPastAndState(long daysInPast,
			List<TaskState> states) {
		LocalDate time = LocalDate.now();
		time = time.minusDays(daysInPast);
		Date fromDate = Date.valueOf(time);
		return taskMapper.getTaskCountByWorkbasketIdAndDaysInPastAndState(fromDate, states);
	}

}
