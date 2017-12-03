package at.woodstick.pimutdroid.internal;

import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

public class AppClassFiles {
	private final static Logger LOGGER = Logging.getLogger(AppClassFiles);
	
	private Project project;
	private String originalClassesDir;
	private String backupClassesDir;

	public AppClassFiles(Project project, String originalClassesDir, String backupClassesDir) {
		this.project = project;
		this.originalClassesDir = originalClassesDir;
		this.backupClassesDir = backupClassesDir;
	}

	public void backup() {
		project.copy {
			from originalClassesDir
			into backupClassesDir
		}
	}
	
	public void restore() {
		project.copy {
			from backupClassesDir
			into originalClassesDir
		}
	}
}
