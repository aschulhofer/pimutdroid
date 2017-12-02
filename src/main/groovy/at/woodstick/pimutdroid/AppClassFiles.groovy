package at.woodstick.pimutdroid;

import java.nio.file.Path
import java.nio.file.Paths

import org.gradle.api.Project
import org.gradle.api.file.FileTree
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import groovy.transform.CompileStatic

public class AppClassFiles {
	private final static Logger LOGGER = Logging.getLogger(AppClassFiles);
	
	private Project project;

	public AppClassFiles(Project project) {
		this.project = project;
	}
	
	public void backup() {
		project.copy {
			from "${project.buildDir}/intermediates/classes/debugOrg"
			into "${project.buildDir}/intermediates/classes/debug"
		}
	}
	
	public void restore() {
		project.copy {
			from "${project.buildDir}/intermediates/classes/debug"
			into "${project.buildDir}/intermediates/classes/debugOrg"
		}
	}
}
