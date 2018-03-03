package at.woodstick.pimutdroid;

import java.io.File

import groovy.transform.CompileStatic

abstract class BaseIntegrationSpec extends nebula.test.IntegrationSpec {

	def setup() {
		System.setProperty('ignoreDeprecations', 'true')
		
		fork = true
		
		setProjectDir()
	}
	
	File setProjectDir() {
		projectDir = new File("build/nebulatest/${this.class.simpleName}/${testName.methodName.replaceAll(/\W+/, '-')}").absoluteFile
		if (projectDir.exists()) {
			projectDir.deleteDir()
		}
		projectDir.mkdirs()
		
		return projectDir
	}

	def setupAndroidProject(String relativeProjectPath) {
		copyResources(relativeProjectPath, ".")
	}
	
	void verifyAndroidProject() {
		assert fileExists('app/build.gradle') == true
	}
}
