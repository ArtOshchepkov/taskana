package pro.taskana;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pro.taskana.exceptions.NotAuthorizedException;
import pro.taskana.model.Task;

@Component
@Transactional
public class TaskanaComponent {

	@Autowired
	TaskService taskService;

	public TaskService getTaskService() {
		return taskService;
	}

	public void triggerRollback() throws NotAuthorizedException {
		Task task = new Task();
		task.setName("Unit Test Task");
		task.setWorkbasketId("1");
		task = taskService.create(task);
		throw new RuntimeException();
	}
}
