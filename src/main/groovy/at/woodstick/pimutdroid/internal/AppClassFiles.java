package at.woodstick.pimutdroid.internal;

import java.nio.file.Path;

import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.file.CopySpec;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.WorkResult;
import org.slf4j.Logger;

public class AppClassFiles {
	private final static Logger LOGGER = Logging.getLogger(AppClassFiles.class);
	
	private Project project;
	private String originalClassesDir;
	private String backupClassesDir;

	public AppClassFiles(Project project, Path originalClassesDir, Path backupClassesDir) {
		this(project, originalClassesDir.toString(), backupClassesDir.toString());
	}
	
	public AppClassFiles(Project project, String originalClassesDir, String backupClassesDir) {
		this.project = project;
		this.originalClassesDir = originalClassesDir;
		this.backupClassesDir = backupClassesDir;
	}

	protected Action<CopySpec> withCopySpec(String from, String into) {
		return (copySpec) -> {
			copySpec.from(from).into(into);
		};
	}
	
	public void backup() {
		WorkResult result = project.copy(withCopySpec(originalClassesDir, backupClassesDir));
		boolean didWork = result.getDidWork();
		
		LOGGER.info("Backup worked: {}", didWork);
		
		if(!didWork) {
			LOGGER.error("Failed to copy class files");
			LOGGER.error("From: '{}'", originalClassesDir);
			LOGGER.error("To: '{}'", backupClassesDir);
			throw new GradleException("Failed to backup class files");
		}
	}
	
	public void restore() {
		WorkResult result =  project.copy((copySpec) -> {
			copySpec.from(backupClassesDir).into(originalClassesDir);
		});
		
		boolean didWork = result.getDidWork();
		
		LOGGER.info("Restore worked: {}", didWork);
		
		if(!didWork) {
			LOGGER.error("Failed to copy class files");
			LOGGER.error("From: '{}'", backupClassesDir);
			LOGGER.error("To: '{}'", originalClassesDir);
			throw new GradleException("Failed to restore class files");
		}
	}
}
