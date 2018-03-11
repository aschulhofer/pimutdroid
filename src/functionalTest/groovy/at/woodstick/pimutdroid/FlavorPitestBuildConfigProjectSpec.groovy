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
public class FlavorPitestBuildConfigProjectSpec extends BaseIntegrationSpec {

	private static final String BUILD_FILE = "pimutdroid-pitest/flavor-configs.build.gradle"
	
	def "plugin tasks created"() {
		when:
			setupBaseProjectWithoutExportForBuild(BUILD_FILE)
		then:
			verifyAndroidProject()
		when:
			ExecutionResult result = runTasksSuccessfully('tasks')
		then:
			result.getStandardOutput().contains("mutateClasses")
			result.getStandardOutput().contains("mutateClassesConfigA")
			result.getStandardOutput().contains("mutateClassesConfigB")
	}
	
	def "run mutateClasses"() {
		when:
			setupBaseProjectWithoutExportForBuild(BUILD_FILE)
		then:
			verifyAndroidProject()
		when:
			ExecutionResult result = runTasksSuccessfully('mutateClasses')
		then:
			result.getSuccess()
			result.getStandardOutput().contains("Generated 2 mutations")
			result.getStandardOutput().contains("Targeted mutants were [at.woodstick.test.simple.*]")
			result.getStandardOutput().contains("Max mutants per class were 0")
	}
	
	def "run mutateClassesConfigA"() {
		when:
			setupBaseProjectWithoutExportForBuild(BUILD_FILE)
		then:
			verifyAndroidProject()
		when:
			ExecutionResult result = runTasksSuccessfully('mutateClassesConfigA')
		then:
			result.getSuccess()
			result.getStandardOutput().contains("Generated 2 mutations")
			result.getStandardOutput().contains("Targeted mutants were [at.woodstick.test.simple.MainActivity]")
			result.getStandardOutput().contains("Max mutants per class were 2")
	}
	
	def "run mutateClassesConfigB"() {
		when:
			setupBaseProjectWithoutExportForBuild(BUILD_FILE)
		then:
			verifyAndroidProject()
		when:
			ExecutionResult result = runTasksSuccessfully('mutateClassesConfigB')
		then:
			result.getSuccess()
			result.getStandardOutput().contains("Generated 1 mutations")
			result.getStandardOutput().contains("Targeted mutants were [at.woodstick.test.simple.*]")
			result.getStandardOutput().contains("Max mutants per class were 1")
	}
}
