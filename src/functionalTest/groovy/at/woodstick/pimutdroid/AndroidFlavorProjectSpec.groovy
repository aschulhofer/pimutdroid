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
public class AndroidFlavorProjectSpec extends BaseIntegrationSpec {

	private static final String PROJECT_FLAVOR = "flavor-android-application"
	
	def "plugin tasks created"() {
		when:
			setupAndroidProject(PROJECT_FLAVOR)
		then:
			verifyAndroidProject()
		when:
			ExecutionResult result = runTasksSuccessfully('tasks')
		then:
			result.getStandardOutput().contains("pimutInfo")
			result.getStandardOutput().contains("mutateClasses")
	}
	
	def "run task preMutation"() {
		when:
			setupAndroidProject(PROJECT_FLAVOR)
		then:
			verifyAndroidProject()
		when:
			ExecutionResult result = runTasksSuccessfully('preMutation')
		then:
			result.getSuccess()
	}
	
	def "run task mutateClasses"() {
		when:
			setupAndroidProject(PROJECT_FLAVOR)
		then:
			verifyAndroidProject()
		when:
			ExecutionResult result = runTasksSuccessfully('mutateClasses')
		then:
			result.getSuccess()
	}
	
	def "run task prepareMutation"() {
		when:
			setupAndroidProject(PROJECT_FLAVOR)
		then:
			verifyAndroidProject()
		when:
			ExecutionResult result = runTasksSuccessfully('preMutation', 'mutateClasses', 'postMutation', 'prepareMutationGenerateTestResult')
		then:
			result.getSuccess()
	}
	
	def "run task buildAllMutantApks"() {
		when:
			setupAndroidProject(PROJECT_FLAVOR)
		then:
			verifyAndroidProject()
		when:
			ExecutionResult result = runTasksSuccessfully('buildAllMutantApks')
		then:
			result.getSuccess()
	}
}
