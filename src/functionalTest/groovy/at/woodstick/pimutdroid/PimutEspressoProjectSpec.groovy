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
public class PimutEspressoProjectSpec extends BaseIntegrationSpec {

	def setupProject() {
		setupBaseProject("base-project/espresso-android-test", true);
		setupSettingsFile();
		
		addFileToAppModule("gradle-files/dependencies.gradle", "dependencies.gradle");
		
		File pimutConfigFile = file("app/pimutdroid-config.gradle");
		pimutConfigFile.text = ''
		pimutConfigFile << """
			apply plugin: "at.woodstick.pimutdroid"

			pimut {

			    pitest {
			        failWhenNoMutations = true
			        exportLineCoverage = true
			        outputFormats = ["XML", "HTML"]
			        timeoutConstInMillis = 15000
			        mutators = [
			            "INCREMENTS",
			            "VOID_METHOD_CALLS",
			            "RETURN_VALS",
			            "MATH",
			            "NEGATE_CONDITIONALS",
			            "INVERT_NEGS",
			            "CONDITIONALS_BOUNDARY",
			            "REMOVE_CONDITIONALS"
			        ]
					verbose = true
					//targetTests = ["at.woodstick.mysampleapplication.test.*"]
			    }
		
				instrumentationTestOptions {
			        targetMutants = [
			            "at.woodstick.mysampleapplication.MainActivity"
			        ]
			    }

			}
		"""
	}
	
	def "plugin tasks created"() {
		when:
			setupProject()
		then:
			verifyAndroidProject()
		when:
			ExecutionResult result = runTasksSuccessfully('tasks')
		then:
			result.getStandardOutput().contains("mutateClasses")
	}
	
	def "run mutateClasses"() {
		when:
			setupProject()
		then:
			verifyAndroidProject()
		when:
			ExecutionResult result = runTasks('mutateClasses')
		then:
			result.getSuccess()
	}
	
	def "run task buildMutantApks"() {
		when:
			setupProject()
		then:
			verifyAndroidProject()
		when:
			ExecutionResult result = runTasksSuccessfully('buildMutantApks')
		then:
			result.getSuccess()
	}
}
