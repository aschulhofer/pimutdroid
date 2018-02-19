package at.woodstick.pimutdroid.task;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.gradle.api.GradleException;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import at.woodstick.pimutdroid.internal.AppClassFiles;

public class CompiledClassesTask extends PimutBaseTask {
	
	static final Logger LOGGER = Logging.getLogger(CompiledClassesTask.class);
	
	private Boolean backup;
	
	private Path classFilesRootDirPath;
	private Path classFilesBackupDirPath;
	
	private AppClassFiles appClassFiles;
	
	@Override
	protected void beforeTaskAction() {
		if(backup == null) {
			throw new GradleException("'backup' property must be set");
		}
		
		if(classFilesRootDirPath == null) {
			classFilesRootDirPath = Paths.get(extension.getClassFilesDir());
		}
		
		if(classFilesBackupDirPath == null) {
			classFilesBackupDirPath = Paths.get(extension.getClassFilesBackupDir());
		}
		
		appClassFiles = new AppClassFiles(getProject(), classFilesRootDirPath, classFilesBackupDirPath);
	}
	
	@Override
	protected void exec() {
		if(backup) {
			LOGGER.lifecycle("Backup class files.");
			appClassFiles.backup();
		} else {
			LOGGER.lifecycle("Restore class files.");
			appClassFiles.restore();
		}
	}

	public Boolean getBackup() {
		return backup;
	}

	public void setBackup(Boolean backup) {
		this.backup = backup;
	}

	public Path getClassFilesRootDirPath() {
		return classFilesRootDirPath;
	}

	public void setClassFilesRootDirPath(Path classFilesRootDirPath) {
		this.classFilesRootDirPath = classFilesRootDirPath;
	}

	public Path getClassFilesBackupDirPath() {
		return classFilesBackupDirPath;
	}

	public void setClassFilesBackupDirPath(Path classFilesBackupDirPath) {
		this.classFilesBackupDirPath = classFilesBackupDirPath;
	}
}
