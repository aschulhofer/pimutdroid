package at.woodstick.pimutdroid.internal;

import java.util.Collection;
import java.util.stream.Collectors;

import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.execution.TaskExecutionGraph;

public class TaskGraphAdaptor {

	private final TaskExecutionGraph graph;

	public TaskGraphAdaptor(TaskExecutionGraph graph) {
		this.graph = graph;
	}
	
	public Collection<Task> getAllTasks() {
		return graph.getAllTasks();
	}
	
	public void whenReady(final Action<TaskGraphAdaptor> readyAction) {
		graph.whenReady( (TaskExecutionGraph graph) -> { 
			readyAction.execute(TaskGraphAdaptor.forGraph(graph));
		});
	}
	
	public <T extends Task> Collection<Task> getTasks(Class<T> taskClass) {
		return graph.getAllTasks().stream().filter((Task task) -> {
			return taskClass.isInstance(task);
		}).collect(Collectors.toList());
	}
	
	protected <T extends Task> boolean hasTask(Class<T> taskClass) {
		return !hasNotTask(taskClass);
	}
	
	protected <T extends Task> boolean hasNotTask(Class<T> taskClass) {
		Collection<Task> buildMutantTasks = getTasks(taskClass);
		
		return buildMutantTasks.isEmpty();
	}
	
	public static TaskGraphAdaptor forGraph(final TaskExecutionGraph graph) {
		return new TaskGraphAdaptor(graph);
	}
}
