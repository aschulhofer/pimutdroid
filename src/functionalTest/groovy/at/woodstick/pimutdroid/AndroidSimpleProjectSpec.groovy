package at.woodstick.pimutdroid;

import nebula.test.functional.ExecutionResult

/**
 * 
 * ! Feature methods with long name result in android compilation errors because paths get too long on Windows.
 * 
 */
public class AndroidSimpleProjectSpec extends nebula.test.IntegrationSpec {
	
	def setup() {
		System.setProperty('ignoreDeprecations', 'true')
		
		fork = true
		
		setProjectDir()
	}
	
	File setProjectDir() {
		File projectDir = new File("build/nebulatest/${this.class.canonicalName}/${testName.methodName.replaceAll(/\W+/, '-')}").absoluteFile
		if (projectDir.exists()) {
			projectDir.deleteDir()
		}
		projectDir.mkdirs()
		
		return projectDir
	}
	
	def setupAndroidProject(String relativeProjectPath) {
		copyResources(relativeProjectPath, ".")
//		copyResources("AndroidManifest.xml", "app/src/main/AndroidManifest.xml")
	}
	
	void verifyAndroidProject() {
		assert fileExists('app/build.gradle') == true
//		assert fileExists('app/src/main/AndroidManifest.xml') == true
	}
	
	def "simple android project apply mutation plugin, pimutInfo task is created"() {
		when:
			setupAndroidProject("simple-android-application")
		then:
			verifyAndroidProject()
		when:
			ExecutionResult result = runTasksSuccessfully('tasks')
		then:
			result.getStandardOutput().contains("pimutInfo")
	}
	
	def "simple android project apply mutation plugin, mutateClasses task is created"() {
		when:
			setupAndroidProject("simple-android-application")
		then:
			verifyAndroidProject()
		when:
			ExecutionResult result = runTasksSuccessfully('tasks')
		then:
			result.getStandardOutput().contains("mutateClasses")
	}
	
	def "run pimutInfo task"() {
		when:
			setupAndroidProject("simple-android-application")
		then:
			verifyAndroidProject()
		when:
			ExecutionResult result = runTasksSuccessfully('pimutInfo')
		then:
			result.getSuccess()
	}
	
	def "run mutateClasses task"() {
		when:
			setupAndroidProject("simple-android-application")
		then:
			verifyAndroidProject()
		when:
			ExecutionResult result = runTasksSuccessfully('mutateClasses')
		then:
			result.getSuccess()
	}
}
