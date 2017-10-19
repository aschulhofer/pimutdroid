package at.woodstick.pimutdroid;

import org.gradle.api.Plugin
import org.gradle.api.Project

class PimutdroidPlugin implements Plugin<Project> {

	private Project target;
	
	@Override
	public void apply(Project project) {
		this.target = project;
		
		
		project.task("pidroidInfo") {
			doLast {
				println "Hello from pidroid!"
			}
		}
	}

}
