package at.woodstick.pimutdroid;

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.FileTree
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import info.solidsoft.gradle.pitest.PitestPlugin
import info.solidsoft.gradle.pitest.PitestTask

class PimutdroidPlugin implements Plugin<Project> {

	private final static Logger LOGGER = Logging.getLogger(PimutdroidPlugin);
	
	static final String PLUGIN_EXTENSION  = "pimut";
	static final String PLUGIN_TASK_GROUP = "Mutation";
	
	private Project project;
	private PimutdroidPluginExtension extension;
	
	private FileTree mutants;

	private Task createTask(String name, Closure closure) {
		return project.task([group: PLUGIN_TASK_GROUP], name, closure);
	}
	
	private Task createTask(Map<String, ?> args, String name, Closure closure) {
		args["group"] = PLUGIN_TASK_GROUP;
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
			
			
		}
		
		createTask("pimutInfo") {
			doLast {
				LOGGER.quiet "Hello from pimutdroid!"
				LOGGER.quiet "Tasks in group: ${PLUGIN_TASK_GROUP}"
				LOGGER.quiet "Mutants dir: ${extension.mutantsDir}"
				LOGGER.quiet "Package of mutants: ${extension.packageDir}"
				LOGGER.quiet "Output mutateAll to console: ${extension.outputMutateAll}"
			}
		}
		
		createTask("mutateAll") {
			doLast {
				final MutantTestHandler handler = new MutantTestHandler(project);
				
				def numMutants = mutants.files.size();
				
				LOGGER.info "Start mutation of all mutants ($numMutants, $extension.outputMutateAll)";
				
				handler.execute(numMutants, extension.outputMutateAll);
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
		
		createTask("createMutants") {
            dependsOn "pitestDebug"

            doLast {
                LOGGER.info "mutants ready."
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

                FileTree testResult = project.fileTree(project.android.testOptions.resultsDir)

                project.copy {
                    from testResult.files
                    into "${project.buildDir}/mutantion/result/$mutantName/$mutantId"
                    include "**/*.xml"
                }

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
		
		createTask("preMutation") {
			doFirst {

				// Backup compiled debug class files
				copy {
					from "${project.buildDir}/intermediates/classes/debug"
					into "${project.buildDir}/intermediates/classes/debugOrg"
				}

				// Backup original debug apk
				copy {
					from "${project.buildDir}/outputs/apk/${project.name}-debug.apk"
					into "${project.buildDir}/outputs/apk/"

					include "${project.name}-debug.apk"

					rename("${project.name}-debug.apk", "${project.name}-debug.org.apk")
				}
			}
		}
		
		project.afterEvaluate {
			project.tasks.compileDebugSources.finalizedBy project.tasks.mutateAfterCompile
			project.tasks.connectedDebugAndroidTest.finalizedBy project.tasks.afterMutantTest

			mutants = getMutants();
			
			mutants.eachWithIndex { File file, index ->

				def mutationTask = createTask("mutant$index") {
					ext {
						idx = index
					}

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
	}

}
