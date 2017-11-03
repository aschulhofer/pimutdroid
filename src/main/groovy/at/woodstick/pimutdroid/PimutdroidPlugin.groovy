package at.woodstick.pimutdroid;

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.api.file.FileTree
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.execution.TaskGraphExecuter

import info.solidsoft.gradle.pitest.PitestPlugin
import info.solidsoft.gradle.pitest.PitestTask

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
	
	private Task createTask(Map<String, ?> args, String name, boolean useDefaultGroup = true, Closure closure) {
		if(useDefaultGroup) {
			args["group"] = PLUGIN_TASK_GROUP;
		}
		return project.task(args, name, closure);
	}
	
	private FileTree getMutants() {
        FileTree mutantsTask = project.fileTree(
            dir: project[PLUGIN_EXTENSION].mutantsDir,
            include: "**/mutants/**/*.class"
        )
		
        return mutantsTask;
    }
	
	def classFileNameToRelativePackagePath(def className) {
		def pathSegs = className.split("\\.")

		// remove class segment and file name
		pathSegs = (pathSegs - pathSegs[-1] - pathSegs[-2])

		return pathSegs.join("/")
	}

	def classFileNameWithoutRelativePackagePath(def className) {
		def pathSegs = className.split("\\.")
		return pathSegs[-2] + "." + pathSegs[-1]
	}

	def getTargetFileInfoFromMutantClass(def className) {
		def pathSegs = className.split("\\.")

		def fileName = pathSegs[-2] + "." + pathSegs[-1];

		// remove class segment and file name
		pathSegs = (pathSegs - pathSegs[-1] - pathSegs[-2])

		def filePath = pathSegs.join("/")

		return [name: fileName, path: filePath]
	}
	
	private void generateMutationTasks() {
		mutants = getMutants();
		
		mutants.eachWithIndex { File file, index ->

			if(extension.outputMutantCreation) {
				LOGGER.lifecycle "Create mutation task $index for mutant file $file"
			}
			
			def mutationTask = createTask([group: PLUGIN_TASK_SINGLE_MUTANT_GROUP], false, "mutant$index") {
				project.tasks.compileDebugSources.finalizedBy project.tasks.mutateAfterCompile
				project.tasks.connectedDebugAndroidTest.finalizedBy project.tasks.afterMutantTest
				
				doLast {
					project.tasks.mutateAfterCompile.mutantFile = file
					project.tasks.mutateAfterCompile.mutantId = index

					project.tasks.afterMutantTest.mutantName = file.getName()
					project.tasks.afterMutantTest.mutantFile = file
					project.tasks.afterMutantTest.mutantId = index
				}

				finalizedBy "connectedDebugAndroidTest"
			}
		}
	}
	
	def setValueIfNull(String extensionProperty, def value) {
		def propertyValue = extension[extensionProperty];
		if(propertyValue == null) {
			extension[extensionProperty] = value;
		}
	}
	
	def copyAndroidTestResults(final String targetDir) {
		FileTree testResult = project.fileTree(project.android.testOptions.resultsDir)
		
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
		extension.pitest = project.extensions["pitest"];
		
		project.afterEvaluate {
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
				extension.outputDir = "${project.buildDir}/mutantion/result";
			}
		}
		
		createTask("pimutInfo") {
			doLast {
				LOGGER.quiet "Hello from pimutdroid!"
				LOGGER.quiet "Tasks in group: ${PLUGIN_TASK_GROUP}"
				LOGGER.quiet "Mutants dir: ${extension.mutantsDir}"
				LOGGER.quiet "Package of mutants: ${extension.packageDir}"
				LOGGER.quiet "Output mutateAll to console: ${extension.outputMutateAll}"
				LOGGER.quiet "Output mutation task creation to console: ${extension.outputMutantCreation}"
				LOGGER.quiet "Run mutateAll for max first mutants: ${extension.maxFirstMutants}"
			}
		}
		
		createTask("mutateAll") {
			doLast {
				final MutantTestHandler handler = new MutantTestHandler(project);
				
				def numMutants = mutants.files.size();
				
				LOGGER.info "Start mutation of all mutants ($numMutants, ${extension.maxFirstMutants}, ${extension.outputMutateAll})";
				
				handler.execute(numMutants, extension.maxFirstMutants, extension.outputMutateAll);
			}
		}
		
		createTask("afterMutation") {
			doLast {
				final AfterMutationHandler handler = new AfterMutationHandler();
				handler.execute();
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
			doFirst {

				// Backup compiled debug class files
				project.copy {
					from "${project.buildDir}/intermediates/classes/debug"
					into "${project.buildDir}/intermediates/classes/debugOrg"
				}

				// Backup original debug apk
				project.copy {
					from "${project.buildDir}/outputs/apk/${project.name}-debug.apk"
					into "${project.buildDir}/outputs/apk/"

					include "${project.name}-debug.apk"

					rename("${project.name}-debug.apk", "${project.name}-debug.org.apk")
				}
			}
		}
		
		createTask("createMutants") {
			dependsOn "pitestDebug"
			
			doLast {
				LOGGER.info "mutants ready."
			}
		}
		
		createTask("generateMutants") {
            dependsOn "pitestDebug"

            doLast {
                LOGGER.info "mutants ready."
            }
        }
		
		createTask("unitTestMutants") {
			dependsOn "pitestDebug"

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
						
			project.tasks.preMutation.dependsOn "assembleDebug"
			project.tasks.preMutation.dependsOn "assembleAndroidTest"
			project.tasks.generateMutants.dependsOn "preMutation"
			
			doLast {
				LOGGER.lifecycle "Preparations for mutation finished."
			}
		}
		
		createTask("postPrepareMutation") {
			dependsOn "prepareMutation"
			finalizedBy "postPrepareMutationAfterConnectedTest"

			doLast {
				LOGGER.lifecycle "Starting connected tests"
			}
		}
		
		createTask("postPrepareMutationAfterConnectedTest") {
			dependsOn "connectedDebugAndroidTest"
			
			doLast {
				LOGGER.lifecycle "Connected tests finished. Storing expected results."	
				
				copyAndroidTestResults("${extension.outputDir}/debug");
			}
		}
		
		createTask("afterMutantTest") {
            ext {
                mutantId = -1
                mutantFile = null
                mutantName = null
            }

            doLast {
                LOGGER.lifecycle "Connected test against mutant finished."

				copyAndroidTestResults("${extension.outputDir}/$mutantName/$mutantId");
				
                project.copy {
                    from "${project.buildDir}/intermediates/classes/debugOrg"
                    into "${project.buildDir}/intermediates/classes/debug"
                }
            }

        }
		
		createTask("mutateAfterCompile") {
            ext {
                mutantId = -1
                mutantFile = null
            }

            doFirst {
                LOGGER.debug "compileSources done."
            }

            doLast {
                def mutantTargetFileName = classFileNameWithoutRelativePackagePath(mutantFile.getName());
                def mutantTargetRelPath = classFileNameToRelativePackagePath(mutantFile.getName());

                def targetFileInfo = getTargetFileInfoFromMutantClass(mutantFile.getName())

                LOGGER.debug "Copy mutant class over debug class"
                LOGGER.debug "Mutant file: ${mutantFile.getName()}"

                LOGGER.debug "Target file name: ${targetFileInfo.name}"
                LOGGER.debug "Target file path: ${targetFileInfo.path}"

                project.copy {
                    from mutantFile.parentFile.absolutePath
                    into "${project.buildDir}/intermediates/classes/debug/${targetFileInfo.path}"

                    include mutantFile.getName()
                    rename(mutantFile.getName(), targetFileInfo.name)
                }

                LOGGER.lifecycle "mutateAfterCompile done for mutant $mutantId."
            }
        }

		
		project.gradle.taskGraph.whenReady { TaskExecutionGraph graph -> 
			LOGGER.info "Taskgraph ready"
			
			if(!graph.hasTask(project.tasks.postPrepareMutation)) {
				LOGGER.lifecycle "Disable prepare mutation connected tests tasks"
				project.tasks.postPrepareMutationAfterConnectedTest.enabled = false
			}
			
			def mutantTasks = graph.getAllTasks().findAll {
				it.name.startsWith("mutant")
			}
		
			if(mutantTasks.isEmpty()) {
				LOGGER.lifecycle "Disable mutation tasks"
				project.tasks.mutateAfterCompile.enabled = false
				project.tasks.afterMutantTest.enabled = false
			}
		}
		
		// Create mutation tasks
		project.afterEvaluate {
			generateMutationTasks();
		}
	}

}
