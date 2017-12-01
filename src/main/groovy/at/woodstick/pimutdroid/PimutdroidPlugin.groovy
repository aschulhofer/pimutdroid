package at.woodstick.pimutdroid;

import java.nio.file.CopyOption
import java.nio.file.Files
import java.nio.file.Path

import org.apache.tools.ant.types.optional.depend.DependScanner
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.api.file.FileTree
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskState
import org.gradle.execution.TaskGraphExecuter

import at.woodstick.pimutdroid.task.AfterMutationTask
import at.woodstick.pimutdroid.task.InfoTask
import at.woodstick.pimutdroid.task.MutantTask
import groovy.transform.CompileStatic
import info.solidsoft.gradle.pitest.PitestPlugin
import info.solidsoft.gradle.pitest.PitestTask

//@CompileStatic
class PimutdroidPlugin implements Plugin<Project> {

	private final static Logger LOGGER = Logging.getLogger(PimutdroidPlugin);
	
	static final String PLUGIN_EXTENSION  = "pimut";
	static final String PLUGIN_TASK_GROUP = "Mutation";
	static final String PLUGIN_TASK_SINGLE_MUTANT_GROUP = "Mutant";
	
	private Project project;
	private PimutdroidPluginExtension extension;
	
	private FileTree mutants;

	private Task createTask(String name, Closure closure) {
		return project.task([group: PLUGIN_TASK_GROUP], name, closure);
	}
	
	private Task createTask(String name, Map<String, ?> args, boolean useDefaultGroup = true, Closure closure) {
		if(useDefaultGroup) {
			args["group"] = PLUGIN_TASK_GROUP;
		}
		return project.task(args, name, closure);
	}
	
	private FileTree getMutantClassFiles() {
		final String mutationClassGlob = "**/mutants/**/*.class"
		
		Set<String> includes = extension.targetMutants.collect { mutantGlob ->
			mutantGlob = mutantGlob.replaceAll("\\.", "/") + "/" + mutationClassGlob;
			mutantGlob 
		}.toSet()
		
		LOGGER.lifecycle "Include mutants $includes" 
		
        FileTree mutantsTask = project.fileTree(
            dir: extension.mutantsDir,
            includes: includes
        )
		
		// Skip inner classes
		if(extension.skipInnerClasses) {
			mutantsTask = mutantsTask.matching { exclude "**/*\$*.class" } ;
		}
		
        return mutantsTask;
    }

	private void generateMutationTasks() {
		mutants = getMutantClassFiles();
		
		mutants.eachWithIndex { File file, index ->

			MutantFile mutantFile = new MutantFile(index, file);
			
			if(extension.outputMutantCreation) {
				LOGGER.lifecycle "Create mutation task $index for mutant file $file"
			}
			
			createTask("mutant$index", [type: MutantTask, group: PLUGIN_TASK_SINGLE_MUTANT_GROUP], false) {
				
				ext {
					mfile = mutantFile;
					copyApk = false;
				}
				
				doLast {
					LOGGER.lifecycle "Create mutant apk ${mfile.getId()} for mutant class ${mfile.getName()}" 
				}

				finalizedBy "connectedDebugAndroidTest"
			}
			
			createTask("mutant${index}BuildOnly", [type: MutantTask, group: PLUGIN_TASK_SINGLE_MUTANT_GROUP], false) {
				
				ext {
					mfile = mutantFile;
					copyApk = true;
				}
				
				doLast {
					LOGGER.lifecycle "Create mutant apk ${mfile.getId()} for mutant class ${mfile.getName()}"
				}

				finalizedBy "assembleDebug"
			}
		}
	}
	
	void setValueIfNull(String extensionProperty, def value) {
		def propertyValue = extension[extensionProperty];
		if(propertyValue == null) {
			extension[extensionProperty] = value;
		}
	}
	
	void copyAndroidTestResults(final String targetDir) {
		FileTree testResult = project.fileTree(extension.testResultDir)
		
		LOGGER.lifecycle "Copy test results from ${extension.testResultDir} to ${targetDir}"
		
		project.copy {
			from testResult.files
			into targetDir
			include "**/*.xml"
		}
	}
	
	@Override
	public void apply(Project project) {
		this.project = project;
		
		project.getPluginManager().apply(PitestPlugin);
				
		extension = project.extensions.create(PLUGIN_EXTENSION, PimutdroidPluginExtension);
		extension.pitest = project.extensions[PitestPlugin.PITEST_CONFIGURATION_NAME];
		
		if(project.android.testOptions.resultsDir == null) {
			project.android.testOptions.resultsDir = "${project.reporting.baseDir.path}/mutation/test-results"
		}
		
		if(project.android.testOptions.reportDir == null) {
			project.android.testOptions.reportDir = "${project.reporting.baseDir.path}/mutation/test-reports"
		}
		
		project.afterEvaluate {
			
			if(extension.packageDir == null) {
				extension.packageDir = project.android.defaultConfig.applicationId.replaceAll("\\.", "/")
			}
			
			if(extension.mutantsDir == null) {
				extension.mutantsDir = "${extension.pitest.reportDir}/debug"
			}
			
			if(extension.targetMutants == null || extension.targetMutants.empty) {
				extension.targetMutants = [extension.packageDir]
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
			
			if(extension.skipInnerClasses == null) {
				extension.skipInnerClasses = false;
			}
			
			if(extension.testResultDir == null) {
				extension.testResultDir = project.android.testOptions.resultsDir
			}
			
			if(extension.testReportDir == null) {
				extension.testReportDir = project.android.testOptions.reportDir
			}
			
			def resultOutputDir = "${extension.outputDir}"
			def resultAppResultDir = "${extension.outputDir}/app/debug"
			def resultMutantsResultDir = "${extension.outputDir}/mutants"
			
			createTask("pimutInfo", [type: InfoTask]) {}
			
			createTask("mutateAll") {
				doLast {
					final MutantTestHandler handler = new MutantTestHandler(project, "mutant{mutantId}");
					
					def numMutants = mutants.files.size();
					
					LOGGER.lifecycle "Start mutation of all mutants ($numMutants, ${extension.maxFirstMutants}, ${extension.outputMutateAll})";
					
					handler.execute(numMutants, extension.maxFirstMutants, extension.outputMutateAll);
				}
			}
			
			createTask("mutateAllBuildOnly") {
				doLast {
					final MutantTestHandler handler = new MutantTestHandler(project, "mutant{mutantId}BuildOnly");
					
					def numMutants = mutants.files.size();
					
					LOGGER.lifecycle "Start mutation of all mutants (build only) ($numMutants, ${extension.maxFirstMutants}, ${extension.outputMutateAll})";
					
					handler.execute(numMutants, extension.maxFirstMutants, extension.outputMutateAll);
				}
			}
			
			createTask("afterMutation", [type: AfterMutationTask]) {
				outputDir = resultOutputDir
				appResultDir = resultAppResultDir
				mutantsResultDir = resultMutantsResultDir
				
				doLast {
					println "Finished after mutation."
				}
			}
			
			createTask("mutateAllGenerateResult", [type: AfterMutationTask]) {
				dependsOn "mutateAll"
				
				outputDir = resultOutputDir
				appResultDir = resultAppResultDir
				mutantsResultDir = resultMutantsResultDir
				
				doLast {
					println "Finished after mutation."
				}
			}
			
			createTask("mutantsList") {
	            doLast {
	                int numberMutants = 0;
	
	                mutants.each { File file ->
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
					project.copy {
						from "${project.buildDir}/intermediates/classes/debug"
						into "${project.buildDir}/intermediates/classes/debugOrg"
					}
	
					// Backup original debug apk
					project.copy {
						from "${project.buildDir}/outputs/apk/debug/${project.name}-debug.apk"
						into "${project.buildDir}/outputs/apk/debug/"
	
						include "${project.name}-debug.apk"
	
						rename("${project.name}-debug.apk", "${project.name}-debug.org.apk")
					}
					
					project.copy {
						from "${project.buildDir}/outputs/apk/debug/${project.name}-debug.apk"
						into "${extension.outputDir}/app/debug"
						include "${project.name}-debug.apk"
					}
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
						Files.move(file.toPath(), moveTargetPath, StandardCopyOption.REPLACE_EXISTING);
					}
					
					LOGGER.lifecycle "Starting connected tests"
				}
			}
			
			createTask("postPrepareMutationAfterConnectedTest") {
				dependsOn "connectedDebugAndroidTest"
				
				doLast {
					LOGGER.lifecycle "Connected tests finished. Storing expected results."	
					
					copyAndroidTestResults("${extension.outputDir}/app/debug");
				}
			}
			
			createTask("afterMutantTest") {
	            ext {
					mfile = null
					copyApk = false
	            }
	
	            doLast {
	                LOGGER.lifecycle "Connected test against mutant finished."
	
					def mutantDir = "${extension.outputDir}/mutants/${mfile.getName()}/${mfile.getId()}"
					
					if(!copyApk) { 
						LOGGER.lifecycle "Copy test results to ${mutantDir}"
						
						copyAndroidTestResults(mutantDir);
					}
					
					if(copyApk) {
						LOGGER.lifecycle "Copy apk '${project.name}-debug.apk' to ${mutantDir}"
						
						project.copy {
							from "${project.buildDir}/outputs/apk/debug/"
							into mutantDir
							include "${project.name}-debug.apk"
						}
					}
					
	                project.copy {
	                    from "${project.buildDir}/intermediates/classes/debugOrg"
	                    into "${project.buildDir}/intermediates/classes/debug"
	                }
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
	                    into "${project.buildDir}/intermediates/classes/debug/${targetFileInfo.path}"
	
	                    include mfile.getName()
	                    rename { filename ->  
							filename = targetFileInfo.name
	                    }
	                }
	
	                LOGGER.lifecycle "mutateAfterCompile done for mutant ${mfile.getId()}."
	            }
	        }
		}

//		project.gradle.taskGraph.beforeTask { Task task ->
//			LOGGER.quiet "Before task ${task.name}"
//		}
//
//		project.gradle.taskGraph.afterTask { Task task, TaskState taskState ->
//			LOGGER.quiet "After task ${task.name} with state $taskState"
//			
//			if(task instanceof MutantTask) {
//				LOGGER.quiet "After mutant task: ${task.ext.mfile.name}"
//			}
//		}

		project.gradle.taskGraph.whenReady { TaskExecutionGraph graph -> 
			LOGGER.info "Taskgraph ready"
			
			if(!graph.hasTask(project.tasks.postPrepareMutation)) {
				LOGGER.lifecycle "Disable prepare mutation connected tests tasks"
				project.tasks.postPrepareMutationAfterConnectedTest.enabled = false
			}
			
			def mutantTasks = graph.getAllTasks().findAll { Task task ->
				task instanceof MutantTask
			}
		
			if(mutantTasks.isEmpty()) {
				LOGGER.lifecycle "Disable mutation tasks found ${mutantTasks.size()} 'mutant*' tasks";
				project.tasks.mutateAfterCompile.enabled = false
				project.tasks.afterMutantTest.enabled = false
			}
			else {
				LOGGER.lifecycle "Enable mutation tasks found ${mutantTasks.size()} 'mutant*' tasks";
				
				Task mutantTask = mutantTasks.first();
				
				LOGGER.lifecycle "Mutant task ${mutantTask.name}";
				LOGGER.lifecycle "Mutant file ${mutantTask.mfile}";
				LOGGER.lifecycle "Mutant copy apk ${mutantTask.copyApk}";
				
				Task mutateAfterCompileTask = project.tasks.mutateAfterCompile;
				
				if(project.gradle.taskGraph.hasTask(mutateAfterCompileTask)) {
					LOGGER.lifecycle "Set mutation config for 'mutateAfterCompile' task";
					
					mutateAfterCompileTask.mfile = mutantTask.mfile
				}

				Task afterMutantTestTask = project.tasks.afterMutantTest;
				
				if(project.gradle.taskGraph.hasTask(afterMutantTestTask)) {
					
					LOGGER.lifecycle "Set mutation config for 'afterMutantTest' task";
					
					afterMutantTestTask.mfile = mutantTask.mfile;
					afterMutantTestTask.copyApk = mutantTask.copyApk;
				}
				
			}
		}
		
		// Create mutation tasks and hook mutation tasks into android tasks
		project.afterEvaluate {
			generateMutationTasks();
			
			project.tasks.compileDebugSources.finalizedBy "mutateAfterCompile"
			project.tasks.assembleDebug.finalizedBy "afterMutantTest"
			project.tasks.connectedDebugAndroidTest.finalizedBy "afterMutantTest"
		}
	}

}
