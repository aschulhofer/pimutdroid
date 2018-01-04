package at.woodstick.pimutdroid;

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.api.file.FileTree
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.Delete

import at.woodstick.pimutdroid.internal.AndroidTestResult
import at.woodstick.pimutdroid.internal.AppApk
import at.woodstick.pimutdroid.internal.AppClassFiles
import at.woodstick.pimutdroid.internal.Device
import at.woodstick.pimutdroid.internal.DeviceLister
import at.woodstick.pimutdroid.internal.DeviceTestOptionsProvider
import at.woodstick.pimutdroid.internal.MutantFile
import at.woodstick.pimutdroid.internal.MutantTestHandler
import at.woodstick.pimutdroid.internal.MutationFilesProvider
import at.woodstick.pimutdroid.internal.RunTestOnDevice
import at.woodstick.pimutdroid.task.AfterMutationTask
import at.woodstick.pimutdroid.task.InfoTask
import at.woodstick.pimutdroid.task.MutantTask
import at.woodstick.pimutdroid.task.MutationTestExecutionTask
import groovy.transform.CompileStatic
import info.solidsoft.gradle.pitest.PitestPlugin

//@CompileStatic
class PimutdroidPlugin implements Plugin<Project> {

	private final static Logger LOGGER = Logging.getLogger(PimutdroidPlugin);
	
	static final String PLUGIN_EXTENSION  = "pimut";
	static final String PLUGIN_TASK_GROUP = "Mutation";
	static final String PLUGIN_TASK_SINGLE_MUTANT_GROUP = "Mutant";
	
	private Project project;
	private PimutdroidPluginExtension extension;
	
	private String runner = "android.support.test.runner.AndroidJUnitRunner";
	
	private File adbExecuteable;
	private MutationFilesProvider mutationFilesProvider;
	private DeviceTestOptionsProvider deviceTestOptionsProvider;
	private DeviceLister deviceLister;
	
	private AppClassFiles appClassFiles;
	private AndroidTestResult androidTestResult;
	private AppApk appApk;
	private AppApk appTestApk;
	
	private FileTree mutantClassFiles;

	private Task createTask(String name, Closure closure) {
		return project.task([group: PLUGIN_TASK_GROUP], name, closure);
	}
	
	private Task createTask(String name, Map<String, ?> args, boolean useDefaultGroup = true, Closure closure) {
		if(useDefaultGroup) {
			args["group"] = PLUGIN_TASK_GROUP;
		}
		return project.task(args, name, closure);
	}
	
	private void generateMutationTasks() {
		mutantClassFiles = mutationFilesProvider.getMutantClassFiles();
		
		mutantClassFiles.eachWithIndex { File file, index ->

			MutantFile mutantDataFile = new MutantFile(index, file);
			
			if(extension.outputMutantCreation) {
				LOGGER.lifecycle "Create mutation task $index for mutant file $file"
			}
			
			def mutantTask = createTask("mutant$index", [type: MutantTask, group: PLUGIN_TASK_SINGLE_MUTANT_GROUP], false) {
				dependsOn "connectedDebugAndroidTest"
				finalizedBy "afterMutantTask"
				
				mutantFile = mutantDataFile;
				copyApk = true;
				storeTestResults = true;
			}
			
			mutantTask.appClassFiles = appClassFiles;
			mutantTask.androidTestResult = androidTestResult;
			mutantTask.mutantApk = appApk;
			mutantTask.mutantRootDir = extension.mutantResultRootDir;
			
			def mutantBuildOnlyTask = createTask("mutant${index}BuildOnly", [type: MutantTask, group: PLUGIN_TASK_SINGLE_MUTANT_GROUP], false) {
				dependsOn "assembleDebug"
				finalizedBy "afterMutantTask"
				
				mutantFile = mutantDataFile;
				copyApk = true;
			}
			
			mutantBuildOnlyTask.appClassFiles = appClassFiles;
			mutantBuildOnlyTask.androidTestResult = androidTestResult;
			mutantBuildOnlyTask.mutantApk = appApk;
			mutantBuildOnlyTask.mutantRootDir = extension.mutantResultRootDir;
		}
	}
	
	private boolean projectHasConfiguration(final String configurationName) {
		return ( project.configurations.find({ conf -> return conf.getName().equalsIgnoreCase(configurationName) }) != null );
	}
	
	private String getAndroidTestConfigurationName() {
		return projectHasConfiguration("androidTestImplementation") ? "androidTestImplementation" : "androidTestCompile";
	}
	
	protected void addDependencies(Project project) {
		project.rootProject.buildscript.configurations.maybeCreate(PitestPlugin.PITEST_CONFIGURATION_NAME);
		project.rootProject.buildscript.dependencies.add(PitestPlugin.PITEST_CONFIGURATION_NAME, project.files("${project.projectDir}/libs/pitest-export-plugin-0.1-SNAPSHOT.jar"));
		
		project.dependencies.add(getAndroidTestConfigurationName(), "de.schroepf:android-xml-run-listener:0.2.0");
	}
	
	@Override
	public void apply(Project project) {
		this.project = project;
		
		addDependencies(project);
		
		project.getPluginManager().apply(PitestPlugin);
		
		
		extension = project.extensions.create(PLUGIN_EXTENSION, PimutdroidPluginExtension);
		extension.pitest = project.extensions[PitestPlugin.PITEST_CONFIGURATION_NAME];

		adbExecuteable = project.android.getAdbExecutable();
		mutationFilesProvider = new MutationFilesProvider(project, extension);
		deviceLister = new DeviceLister(adbExecuteable);
		
		if(project.android.testOptions.resultsDir == null) {
			project.android.testOptions.resultsDir = "${project.reporting.baseDir.path}/mutation/test-results"
		}
		
		if(project.android.testOptions.reportDir == null) {
			project.android.testOptions.reportDir = "${project.reporting.baseDir.path}/mutation/test-reports"
		}
		
		project.afterEvaluate {
			
			if(project.android.defaultConfig.testInstrumentationRunner != null) {
				runner = project.android.defaultConfig.testInstrumentationRunner
			}
			
			if(extension.packageDir == null) {
				extension.packageDir = project.android.defaultConfig.applicationId.replaceAll("\\.", "/")
			}
			
			if(extension.mutantsDir == null) {
				extension.mutantsDir = "${extension.pitest.reportDir}/debug"
			}
			
			if(extension.instrumentationTestOptions.targetMutants == null || extension.instrumentationTestOptions.targetMutants.empty) {
				extension.instrumentationTestOptions.targetMutants = [extension.packageDir]
			}
			
			if(extension.outputMutateAll == null) {
				extension.outputMutateAll = false;
			}
			
			if(extension.outputMutantCreation == null) {
				extension.outputMutantCreation = false;
			}
			
			if(extension.maxFirstMutants == null) {
				extension.maxFirstMutants = 0;
			}
			
			if(extension.outputDir == null) {
				extension.outputDir = "${project.buildDir}/mutation/result";
			}
			
			if(extension.testResultDir == null) {
				extension.testResultDir = project.android.testOptions.resultsDir
			}
			
			if(extension.testReportDir == null) {
				extension.testReportDir = project.android.testOptions.reportDir
			}
			
			if(extension.mutantResultRootDir == null) {
				extension.mutantResultRootDir = "${extension.outputDir}/mutants"
			}
			
			if(extension.appResultRootDir == null) {
				extension.appResultRootDir = "${extension.outputDir}/app/debug"
			}
			
			if(extension.classFilesDir == null) {
				extension.classFilesDir = "${project.buildDir}/intermediates/classes/debug"
			}

			appClassFiles = new AppClassFiles(
				project, 
				extension.classFilesDir, 
				"${extension.appResultRootDir}/backup/classes"
			);
			androidTestResult = new AndroidTestResult(project, extension.testResultDir);
			appApk = new AppApk(project, "${project.buildDir}/outputs/apk/debug/", "${project.name}-debug.apk");
			appTestApk = new AppApk(project, "${project.buildDir}/outputs/apk/androidTest/debug/", "${project.name}-debug-androidTest.apk");
			
			deviceTestOptionsProvider = new DeviceTestOptionsProvider(
				extension.instrumentationTestOptions,
				"de.schroepf.androidxmlrunlistener.XmlRunListener"
			);
			
			def mutateAllAdbTask = createTask("mutateAllAdb", [type: MutationTestExecutionTask]) {}
			
			mutateAllAdbTask.adbExecuteable = adbExecuteable
			mutateAllAdbTask.deviceLister = deviceLister
			mutateAllAdbTask.mutationFilesProvider = mutationFilesProvider
			mutateAllAdbTask.deviceTestOptionsProvider = deviceTestOptionsProvider
			mutateAllAdbTask.testApk = appTestApk
			mutateAllAdbTask.appApk = appApk
			mutateAllAdbTask.targetMutants = extension.instrumentationTestOptions.targetMutants
			mutateAllAdbTask.appResultRootDir = extension.appResultRootDir
			mutateAllAdbTask.mutantResultRootDir = extension.mutantResultRootDir
			mutateAllAdbTask.appPackage = project.android.defaultConfig.applicationId
			mutateAllAdbTask.testPackage = project.android.defaultConfig.testApplicationId
			mutateAllAdbTask.runner = runner
			
			createTask("cleanMutation", [type: Delete]) {
				delete extension.outputDir, extension.mutantsDir
			}
			
			createTask("availableDevices") {
				doLast {
					deviceLister.retrieveDevices();
					
					LOGGER.quiet "Found ${deviceLister.getNumberOfDevices()} device(s)";
					deviceLister.getStoredDeviceList().each { Device device -> 
						LOGGER.quiet "${device.getId()}"
					}
					
				}		
			}
			
			createTask("pimutInfo", [type: InfoTask]) {}
			
			createTask("mutateAll") {
				doLast {
					final MutantTestHandler handler = new MutantTestHandler(project, "mutant{mutantId}");
					
					def numMutants = mutantClassFiles.files.size();
					
					LOGGER.lifecycle "Start mutation of all mutants ($numMutants, ${extension.maxFirstMutants}, ${extension.outputMutateAll})";
					
					handler.execute(numMutants, extension.maxFirstMutants, extension.outputMutateAll);
				}
			}
			
			createTask("mutateAllBuildOnly") {
				doLast {
					final MutantTestHandler handler = new MutantTestHandler(project, "mutant{mutantId}BuildOnly");
					
					def numMutants = mutantClassFiles.files.size();
					
					LOGGER.lifecycle "Start mutation of all mutants (build only) ($numMutants, ${extension.maxFirstMutants}, ${extension.outputMutateAll})";
					
					handler.execute(numMutants, extension.maxFirstMutants, extension.outputMutateAll);
				}
			}
			
			Task afterMutationTask = createTask("afterMutation", [type: AfterMutationTask]) {
				outputDir = extension.outputDir
				appResultDir = extension.appResultRootDir
				mutantsResultDir = extension.mutantResultRootDir
				
				doLast {
					println "Finished after mutation."
				}
			}
			afterMutationTask.mutationFilesProvider = mutationFilesProvider;
			
			createTask("mutateAllGenerateResult", [type: AfterMutationTask]) {
				dependsOn "mutateAll"
				
				outputDir = extension.outputDir
				appResultDir = extension.appResultRootDir
				mutantsResultDir = extension.mutantResultRootDir
				
				doLast {
					println "Finished after mutation."
				}
			}
			
			createTask("mutantsList") {
	            doLast {
	                int numberMutants = 0;
	
	                mutantClassFiles.each { File file ->
	                    numberMutants++;
	
	                    LOGGER.quiet "Mutant $numberMutants" + "\t" + file.parentFile.getName() + "\t" +  file.getName()
	
	                }
	            }
	        }
			
			createTask("preMutation") {
				dependsOn "assembleDebug"
				dependsOn "assembleAndroidTest"
				
				doFirst {
					// Backup compiled debug class files
					appClassFiles.backup();
					
					// Copy unmutated apk
					appApk.copyTo(extension.appResultRootDir);
					
					// Copy test apk
					appTestApk.copyTo(extension.appResultRootDir)
				}
			}
			
			createTask("createMutants") {
				finalizedBy "pitestDebug"
				
				doLast {
					LOGGER.info "mutants ready."
				}
			}
			
			createTask("generateMutants") {
				dependsOn "preMutation"
	            finalizedBy "pitestDebug"
	
	            doLast {
	                LOGGER.info "mutants ready."
	            }
	        }
			
			createTask("unitTestMutants") {
				finalizedBy "pitestDebug"
	
				doLast {
					LOGGER.info "mutants ready."
				}
			}
			
			createTask("prepareMutation") {
				dependsOn "assembleDebug"
				dependsOn "assembleAndroidTest"
				dependsOn "preMutation"
				dependsOn "generateMutants"
	
				finalizedBy "postPrepareMutation"
				
				doLast {
					LOGGER.lifecycle "Preparations for mutation finished."
				}
			}
			
			createTask("postPrepareMutation") {
				dependsOn "prepareMutation"
				finalizedBy "postPrepareMutationAfterConnectedTest"
	
				doLast {
					project.file("${extension.mutantsDir}/${extension.packageDir}").listFiles()
					.findAll { file ->
						if(file.getName().matches(/(.*)\$(.*)/)) {
							return file
						}
					}
					.each { File file ->
						String parentClassName = file.getName().replaceAll(/\$(.*)/, "");
						Path moveTargetPath = file.getParentFile().toPath().resolve(parentClassName).resolve(file.getName())
						
						if(Files.exists(moveTargetPath)) {
							moveTargetPath.toFile().deleteDir();
						}
						
						Files.move(file.toPath(), moveTargetPath, StandardCopyOption.REPLACE_EXISTING);
					}
					
					LOGGER.lifecycle "Starting connected tests"
				}
			}
			
			createTask("postPrepareMutationAfterConnectedTest") {
				doLast {
					deviceLister.retrieveDevices();
					
					AppApk appApk = new AppApk(project, extension.appResultRootDir, "${project.name}-debug.apk");
					
					RunTestOnDevice rtod = new RunTestOnDevice(
						deviceLister.getFirstDevice(),
						adbExecuteable, 
						deviceTestOptionsProvider.getOptions(),
						[appApk.getPath().toString()],
						appTestApk.getPath().toString(),
						project.android.defaultConfig.testApplicationId,
						project.android.defaultConfig.applicationId,
						runner
					);
					
					rtod.run();
					
					LOGGER.lifecycle "Connected tests finished. Storing expected results."	
				}
			}
			
			createTask("afterMutantTask") {
				doLast {
					appClassFiles.restore();
				}
			}
			
			createTask("mutateAfterCompile") {
	            ext {
					mfile = null
	            }
	
	            doFirst {
	                LOGGER.debug "compileSources done."
	            }
	
	            doLast {
	                def targetFileInfo = mfile.getTargetFileInfo()
	
	                LOGGER.debug "Copy mutant class over debug class"
	                LOGGER.debug "Mutant file: ${mfile.getName()}"
	
	                LOGGER.debug "Target file name: ${targetFileInfo.name}"
	                LOGGER.debug "Target file path: ${targetFileInfo.path}"
	
	                project.copy {
	                    from mfile.getFile().parentFile.absolutePath
	                    into "${extension.classFilesDir}/${targetFileInfo.path}"
	
	                    include mfile.getName()
	                    rename { filename ->  
							filename = targetFileInfo.name
	                    }
	                }
	
	                LOGGER.lifecycle "mutateAfterCompile done for mutant ${mfile.getId()}."
	            }
	        }
			
			// Create mutation tasks and hook mutation tasks into android tasks
			generateMutationTasks();
			
			project.tasks.compileDebugSources.finalizedBy "mutateAfterCompile"
		}

		project.gradle.taskGraph.whenReady { TaskExecutionGraph graph -> 
			LOGGER.info "Taskgraph ready"
			
			if(!graph.hasTask(project.tasks.postPrepareMutation)) {
				LOGGER.lifecycle "Disable prepare mutation connected tests tasks"
//				project.tasks.postPrepareMutationAfterConnectedTest.enabled = false
			}
			
			def mutantTasks = graph.getAllTasks().findAll { Task task ->
				task instanceof MutantTask
			}
		
			if(mutantTasks.isEmpty()) {
				LOGGER.lifecycle "Disable mutation tasks found ${mutantTasks.size()} 'mutant*' tasks";
				project.tasks.mutateAfterCompile.enabled = false
			}
			else {
				LOGGER.lifecycle "Enable mutation tasks found ${mutantTasks.size()} 'mutant*' tasks";
				
				Task mutantTask = mutantTasks.first();
				
				LOGGER.lifecycle "Mutant task ${mutantTask.name}";
				LOGGER.lifecycle "Mutant file ${mutantTask.mutantFile}";
				LOGGER.lifecycle "Mutant copy apk ${mutantTask.copyApk}";
				
				project.tasks.connectedDebugAndroidTest.ignoreFailures = true
				
				Task mutateAfterCompileTask = project.tasks.mutateAfterCompile;
				
				if(project.gradle.taskGraph.hasTask(mutateAfterCompileTask)) {
					LOGGER.lifecycle "Set mutation config for 'mutateAfterCompile' task";
					
					mutateAfterCompileTask.mfile = mutantTask.getMutantFile()
				}
			}
		}
	}

}
