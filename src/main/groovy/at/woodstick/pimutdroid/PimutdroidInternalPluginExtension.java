package at.woodstick.pimutdroid;

import org.gradle.api.Project;

import at.woodstick.pimutdroid.internal.PluginInternals;

public class PimutdroidInternalPluginExtension {
	
	private final Project project;
	private final PimutdroidPluginExtension pluginExtension;
	
	private PluginInternals pluginInternals;
	
	public PimutdroidInternalPluginExtension(Project project, PimutdroidPluginExtension pluginExtension) {
		this.project = project;
		this.pluginExtension = pluginExtension;
	}

	public Project getProject() {
		return project;
	}

	public PimutdroidPluginExtension getPluginExtension() {
		return pluginExtension;
	}

	public PluginInternals getPluginInternals() {
		return pluginInternals;
	}

	public void setPluginInternals(PluginInternals pluginInternals) {
		this.pluginInternals = pluginInternals;
	}
}
