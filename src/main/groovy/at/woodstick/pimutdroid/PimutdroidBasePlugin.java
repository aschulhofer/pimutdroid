package at.woodstick.pimutdroid;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import info.solidsoft.gradle.pitest.PitestPlugin;

public class PimutdroidBasePlugin implements Plugin<Project> {

	private final static Logger LOGGER = Logging.getLogger(PimutdroidBasePlugin.class);
	
	public static final String PROPERTY_NAME_MUID = "pimut.muid";
	public static final String RUNNER = "android.support.test.runner.AndroidJUnitRunner";
	
	public static final String PLUGIN_EXTENSION  = "pimut";
	public static final String PLUGIN_TASK_GROUP = "Mutation";

	@Override
	public void apply(Project project) {
//		if(!project.plugins.hasPlugin(AndroidBasePlugin.class)) {
//			throw new GradleException(String.format("Android plugin must be applied to project"));
//		}
		
		if(!project.getPlugins().hasPlugin(PitestPlugin.class)) {
			project.getPluginManager().apply(PitestPlugin.class);
		} else {
			LOGGER.info("pitest plugin already applied.");
		}
	}
}