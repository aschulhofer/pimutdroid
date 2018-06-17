package at.woodstick.pimutdroid.internal;

import java.io.File;

import at.woodstick.pimutdroid.task.AvailableDevicesTask;
import at.woodstick.pimutdroid.task.ConfiguredDevicesTask;
import at.woodstick.pimutdroid.task.InfoTask;

public class InfoTasksCreator {

	public static final String TASK_PLUGIN_INFO_NAME 			= "pimutInfo";
	public static final String TASK_AVAILABLE_DEVICES_NAME 		= "availableDevices";
	public static final String TASK_CONFIGURED_DEVICES_NAME		= "configuredDevices";
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
		createPimutInfoTask();
		createAvailableDevicesTask();
		createConfiguredDevicesTask();
		createListMutantClasses();
		createListMutantMarker();
		createListMutantXmlResults();
	}
	
	// ########################################################################
	
	protected void createAvailableDevicesTask() {
		taskFactory.create(TASK_AVAILABLE_DEVICES_NAME, AvailableDevicesTask.class);
	}
	
	protected void createConfiguredDevicesTask() {
		taskFactory.create(TASK_CONFIGURED_DEVICES_NAME, ConfiguredDevicesTask.class);
	}
	
	protected void createPimutInfoTask() {
		taskFactory.create(TASK_PLUGIN_INFO_NAME, InfoTask.class);
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
