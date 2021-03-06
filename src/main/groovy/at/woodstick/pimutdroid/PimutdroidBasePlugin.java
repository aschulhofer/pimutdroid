package at.woodstick.pimutdroid;

import java.io.File;

import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.PluginContainer;

import com.android.build.gradle.api.AndroidBasePlugin;

import info.solidsoft.gradle.pitest.PitestPlugin;

public class PimutdroidBasePlugin implements Plugin<Project> {

	private final static Logger LOGGER = Logging.getLogger(PimutdroidBasePlugin.class);

	public static final String PROPERTY_NAME_MUID = "pimut.muid";
	public static final String RUNNER = "android.support.test.runner.AndroidJUnitRunner";
	
	public static final String PLUGIN_EXTENSION  = "pimut";
	public static final String PLUGIN_TASK_GROUP = "Mutation";
	
	public static final String PITEST_VERSION = "1.2.2";
	public static final String PITEST_CONFIGURATION_NAME = PitestPlugin.PITEST_CONFIGURATION_NAME;

	static final String PITEST_GROUP = "org.pitest";
	static final String PITEST_DEPENDENCY_NAME = "pitest-command-line";
	static final String FORCED_PITEST_DEPENDENCY = PITEST_GROUP + ":" + PITEST_DEPENDENCY_NAME + ":" + PITEST_VERSION;
	
	static final String REPORTS_DIR_NAME = "reports";
	
	@Override
	public void apply(Project project) {
		PluginContainer pluginContainer = project.getPlugins();
		
		if(!pluginContainer.hasPlugin(AndroidBasePlugin.class)) {
			throw new GradleException(String.format("Android plugin must be applied to project"));
		}
		
		if(!pluginContainer.hasPlugin(PitestPlugin.class)) {
			project.getPluginManager().apply(PitestPlugin.class);
		} else {
			LOGGER.info("pitest plugin already applied.");
		}
	}
	
	static File getBuildDir(Project project) {
		return project.getBuildDir();
	}
	
	static File getReportsDir(Project project) {
		return project.getBuildDir().toPath().resolve(REPORTS_DIR_NAME).toFile();
	}
}
