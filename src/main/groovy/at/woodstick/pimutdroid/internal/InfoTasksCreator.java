package at.woodstick.pimutdroid.internal;

import java.io.File;

public class InfoTasksCreator {

	public static final String TASK_LIST_MUTANT_XML_RESULT_NAME = "mutantXmlResultList";
	public static final String TASK_LIST_MUTANT_MARKER_NAME 	= "mutantMarkerList";
	public static final String TASK_LIST_MUTANT_CLASSES_NAME 	= "mutantClassesList";
	
	private PluginInternals pluginInternals;
	private TaskFactory taskFactory;
	
	// ########################################################################
	
	public InfoTasksCreator(PluginInternals pluginInternals, TaskFactory taskFactory) {
		this.pluginInternals = pluginInternals;
		this.taskFactory = taskFactory;
	}
	
	// ########################################################################
	
	public void createTasks() {
		createListMutantClasses();
		createListMutantMarker();
		createListMutantXmlResults();
	}
	
	// ########################################################################

	protected void createListMutantClasses() {
		taskFactory.create(TASK_LIST_MUTANT_CLASSES_NAME, (task) -> {
			task.doLast((ignore) -> {
				int numberMutants = 0;
				for(File file : pluginInternals.getMutationFilesProvider().getMutantClassFiles()) {
					numberMutants++;
					task.getLogger().quiet("Mutant {}\t{}\t{}", numberMutants, file.getParentFile().getName(), file.getName());
				}
			});
		});
	}

	protected void createListMutantMarker() {
		taskFactory.create(TASK_LIST_MUTANT_MARKER_NAME, (task) -> {
			task.doLast((ignore) -> {
				int numberMutants = 0;
				for(File file : pluginInternals.getMutationFilesProvider().getMutantMarkerFiles()) {
					numberMutants++;
					task.getLogger().quiet("Mutant {}\t{}\t{}", numberMutants, file.getParentFile().getName(), file.getName());
				}
			});
		});
	}
	
	protected void createListMutantXmlResults() {
		taskFactory.create(TASK_LIST_MUTANT_XML_RESULT_NAME, (task) -> {
			task.doLast((ignore) -> {
				int numberMutants = 0;
				for(File file : pluginInternals.getMutationFilesProvider().getMutantResultTestFiles()) {
					numberMutants++;
					task.getLogger().quiet("Mutant {}\t{}\t{}", numberMutants, file.getParentFile().getName(), file.getName());
				}
			});
		});
	}
}
