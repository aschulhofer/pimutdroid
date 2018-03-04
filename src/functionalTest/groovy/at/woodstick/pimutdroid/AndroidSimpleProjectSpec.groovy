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

	private static final String PROJECT_SIMPLE = "simple-android-application"
	
	def "plugin tasks created"() {
		when:
			setupAndroidProject(PROJECT_SIMPLE)
		then:
			verifyAndroidProject()
		when:
			ExecutionResult result = runTasksSuccessfully('tasks')
		then:
			result.getStandardOutput().contains("pimutInfo")
			result.getStandardOutput().contains("mutateClasses")
	}
	
	def "run pimutInfo task"() {
		when:
			setupAndroidProject(PROJECT_SIMPLE)
		then:
			verifyAndroidProject()
		when:
			ExecutionResult result = runTasksSuccessfully('pimutInfo')
		then:
			result.getSuccess()
	}
	
	def "run mutateClasses task"() {
		when:
			setupAndroidProject(PROJECT_SIMPLE)
		then:
			verifyAndroidProject()
		when:
			ExecutionResult result = runTasksSuccessfully('mutateClasses')
		then:
			result.getSuccess()
	}
	
	def "run availableDevices task"() {
		when:
			setupAndroidProject(PROJECT_SIMPLE)
		then:
			verifyAndroidProject()
		when:
			ExecutionResult result = runTasksSuccessfully('availableDevices')
		then:
			result.getSuccess()
			result.getStandardOutput().contains("Found 1 device(s)")
			result.getStandardOutput().contains("emulator-${emulatorId}")
	}
	
	def "run task prepareMutation"() {
		when:
			setupAndroidProject(PROJECT_SIMPLE)
		then:
			verifyAndroidProject()
		when:
			ExecutionResult result = runTasksSuccessfully('preMutation', 'mutateClasses', 'postMutation', 'prepareMutationGenerateTestResult')
		then:
			result.getSuccess()
	}
	
	def "run task buildAllMutantApks"() {
		when:
			setupAndroidProject(PROJECT_SIMPLE)
		then:
			verifyAndroidProject()
		when:
			ExecutionResult result = runTasksSuccessfully('buildAllMutantApks')
		then:
			result.getSuccess()
	}
}
