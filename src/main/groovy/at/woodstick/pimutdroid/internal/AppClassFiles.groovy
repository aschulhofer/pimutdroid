package at.woodstick.pimutdroid.internal;

import java.nio.file.Path

import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.WorkResult

public class AppClassFiles {
	private final static Logger LOGGER = Logging.getLogger(AppClassFiles);
	
	private Project project;
	private String originalClassesDir;
	private String backupClassesDir;

	public AppClassFiles(Project project, Path originalClassesDir, Path backupClassesDir) {
		this.project = project;
		this.originalClassesDir = originalClassesDir.toString();
		this.backupClassesDir = backupClassesDir.toString();
	}
	
	public AppClassFiles(Project project, String originalClassesDir, String backupClassesDir) {
		this.project = project;
		this.originalClassesDir = originalClassesDir;
		this.backupClassesDir = backupClassesDir;
	}

	public void backup() {
		WorkResult result = project.copy {
			from originalClassesDir
			into backupClassesDir
		};
		
		LOGGER.lifecycle("Backup worked: {}", result.getDidWork());
		
		if(!result.getDidWork()) {
			LOGGER.lifecycle("Failed to copy class files");
			LOGGER.lifecycle("From: '{}'", originalClassesDir);
			LOGGER.lifecycle("To: '{}'", backupClassesDir);
		}
	}
	
	public void restore() {
		WorkResult result = project.copy {
			from backupClassesDir
			into originalClassesDir
		};
		
		LOGGER.lifecycle("Restore worked: {}", result.getDidWork());
		
		if(!result.getDidWork()) {
			LOGGER.lifecycle("Failed to copy class files");
			LOGGER.lifecycle("From: '{}'", backupClassesDir);
			LOGGER.lifecycle("To: '{}'", originalClassesDir);
		}
	}
}
