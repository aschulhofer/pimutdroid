package at.woodstick.pimutdroid;

import nebula.test.functional.ExecutionResult

/**
 * ! GradleBuild tasks are not running with GradleTestKit -> UnkownPluginException on tested plugin  
 * 
 * ! Feature methods with long name result in android compilation errors because paths get too long on Windows.
 * 
 * org.gradle.api.GradleException: Build aborted because of an internal error.
 * ...
 * Caused by: org.gradle.internal.exceptions.LocationAwareException: Execution failed for task ':app:processFreeDebugResources'.
 * ...
 * Caused by: com.android.ide.common.process.ProcessException: Failed to execute aapt
 * ...
 */
public class AndroidSimpleProjectSpec extends BaseIntegrationSpec {

	private static final String BUILD_FILE = "pimutdroid/simple-pimutdroid.build.gradle"
	
	def "plugin tasks created"() {
		when:
			setupBaseProjectForBuild(BUILD_FILE)
		then:
			verifyAndroidProject()
		when:
			ExecutionResult result = runTasksSuccessfully('tasks')
		then:
			result.getStandardOutput().contains("pimutInfo")
			result.getStandardOutput().contains("mutateClasses")
			result.getStandardOutput().contains("availableDevices")
	}
	
	def "run pimutInfo task"() {
		when:
			setupBaseProjectForBuild(BUILD_FILE)
		then:
			verifyAndroidProject()
		when:
			ExecutionResult result = runTasksSuccessfully('pimutInfo')
		then:
			result.getSuccess()
	}
	
	def "run mutateClasses task"() {
		when:
			setupBaseProjectForBuild(BUILD_FILE)
		then:
			verifyAndroidProject()
		when:
			ExecutionResult result = runTasksSuccessfully('mutateClasses')
		then:
			result.getSuccess()
	}
	
	def "run availableDevices task"() {
		when:
			setupBaseProjectForBuild(BUILD_FILE)
		then:
			verifyAndroidProject()
		when:
			ExecutionResult result = runTasksSuccessfully('availableDevices')
		then:
			result.getSuccess()
			result.getStandardOutput().contains("Found 1 device(s)")
			result.getStandardOutput().contains("emulator-${emulatorId}")
	}
	
	def "run task prepareMutationFiles"() {
		when:
			setupBaseProjectForBuild(BUILD_FILE)
		then:
			verifyAndroidProject()
		when:
			ExecutionResult result = runTasksSuccessfully('prepareMutationFiles')
		then:
			result.getSuccess()
	}
	
	def "run task buildMutantApks"() {
		when:
			setupBaseProjectForBuild(BUILD_FILE)
		then:
			verifyAndroidProject()
		when:
			ExecutionResult result = runTasksSuccessfully('buildMutantApks')
		then:
			result.getSuccess()
	}
	
	def "run generateExpectedResult"() {
		when:
			setupBaseProjectForBuild(BUILD_FILE)
		then:
			verifyAndroidProject()
		when:
			ExecutionResult result = runTasksSuccessfully('generateExpectedResult')
		then:
			result.getSuccess()
	}
}
