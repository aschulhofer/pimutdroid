package at.woodstick.pimutdroid;

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.FileTree

class PimutdroidPlugin implements Plugin<Project> {

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
		
		extension = project.extensions.create(PLUGIN_EXTENSION, PimutdroidPluginExtension);
		
		project.afterEvaluate {
			if(extension.outputMutateAll == null) {
				extension.outputMutateAll = false;
			}
		}
		
		createTask("pidroidInfo") {
			doLast {
				println "Hello from pimutdroid!"
				println "Tasks in group: ${PLUGIN_TASK_GROUP}"
				println "Mutants dir: ${extension.mutantsDir}"
				println "Package of mutants: ${extension.packageDir}"
				println "Output mutateAll to console: ${extension.outputMutateAll}"
			}
		}
		
		createTask("mutateAll") {
			doLast {
				final MutantTestHandler handler = new MutantTestHandler(project);
				handler.execute(mutants.files.size(), extension.outputMutateAll);
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

                    println "Mutant $numberMutants" + "\t" + file.parentFile.getName() + "\t" +  file.getName()

                }
            }
        }
		
		createTask("createMutants") {
            dependsOn "pitestDebug"

            doLast {
                println "mutants ready."
            }
        }
		
		createTask("afterMutantTest") {
            ext {
                mutantId = -1
                mutantFile = null
                mutantName = null
            }

            doLast {
                println "Connected test against mutant finished."

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
                println "compileSources done."
            }

            doLast {
                def mutantTargetFileName = classFileNameWithoutRelativePackagePath(mutantFile.getName());
                def mutantTargetRelPath = classFileNameToRelativePackagePath(mutantFile.getName());

                def targetFileInfo = getTargetFileInfoFromMutantClass(mutantFile.getName())

                println "Copy mutant class over debug class"
                println "Mutant file: ${mutantFile.getName()}"

                println "Target file name: ${targetFileInfo.name}"
                println "Target file path: ${targetFileInfo.path}"

                project.copy {
                    from mutantFile.parentFile.absolutePath
                    into "${project.buildDir}/intermediates/classes/debug/${targetFileInfo.path}"

                    include mutantFile.getName()
                    rename(mutantFile.getName(), targetFileInfo.name)
                }

                println "mutateAfterCompile done for mutant $mutantId."
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
