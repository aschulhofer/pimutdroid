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
public class AndroidFlavorPitestProjectSpec extends BaseIntegrationSpec {

	private static final String BUILD_FILE = "pimutdroid-pitest/flavor-pimutdroid-pitest.build.gradle"
	
	def "plugin tasks created"() {
		when:
			setupBaseProjectWithoutExportForBuild(BUILD_FILE)
		then:
			verifyAndroidProject()
		when:
			ExecutionResult result = runTasksSuccessfully('tasks')
		then:
			result.getStandardOutput().contains("mutateClasses")
			
			! result.getStandardOutput().contains("pimutInfo")
			! result.getStandardOutput().contains("preMutation")
			! result.getStandardOutput().contains("postMutation")
	}
	
	def "run task mutateClasses"() {
		when:
			setupBaseProjectWithoutExportForBuild(BUILD_FILE)
		then:
			verifyAndroidProject()
		when:
			ExecutionResult result = runTasksSuccessfully('mutateClasses')
		then:
			result.getSuccess()
	}
}
