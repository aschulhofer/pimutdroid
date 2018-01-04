package at.woodstick.pimutdroid.task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileTree;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskAction;

import at.woodstick.pimutdroid.internal.MarkerFileProvider;
import at.woodstick.pimutdroid.internal.MutantMarkerFile;
import at.woodstick.pimutdroid.internal.MutationFilesProvider;

public class PrepareMutantFilesTask extends DefaultTask {
	static final Logger LOGGER = Logging.getLogger(PrepareMutantFilesTask.class);
	
	private MutationFilesProvider mutantFilesProvider;
	private MarkerFileProvider markerFileProvider;
	
	@TaskAction
	void exec() throws IOException {
		FileTree mutantClassFilesFileTree = mutantFilesProvider.getAllMutantClassFiles();
		
		Set<File> innerClassesDirSet = new HashSet<>();
		
		// Create marker files for mutant class files and store root dirs of inner class mutants
		for(File file : mutantClassFilesFileTree) {
			final String fileName = file.getName();
			
			if(fileName.contains("$")) {
				File innerClassMutantRootDir = file.getParentFile().getParentFile().getParentFile();
				innerClassesDirSet.add(innerClassMutantRootDir);
				
				LOGGER.quiet("Marker file for inner class mutant {} in mutant rootDir {}", fileName, innerClassMutantRootDir);
			}
			
			final MutantMarkerFile markerFile = markerFileProvider.fromClassFile(file);
			File muidFile = markerFile.getFile();
			muidFile.createNewFile();
			
			LOGGER.quiet("markerfile {} - {}", markerFile.getFileName(), file.getAbsolutePath());
		}
		
		
		// Move inner class mutant dir to containing class dir (remove existing target dir)
		for(File innerClassDir : innerClassesDirSet) {
			String innerClassName = innerClassDir.getName();
			String targetDirName = innerClassName.split("\\$")[0];
			
			File packageDir = innerClassDir.getParentFile();
			
			Path moveTargetPath = packageDir.toPath().resolve(targetDirName).resolve(innerClassName);
			
			if(Files.exists(moveTargetPath)) {
				Files.walk(moveTargetPath).map(Path::toFile).sorted(Comparator.reverseOrder()).forEach(File::delete);
			}
			
			if(!targetDirName.equals(packageDir.getName())) {
				LOGGER.quiet("Mutant rootDir move {} to dir {}", innerClassDir, targetDirName);
				Files.move(innerClassDir.toPath(), moveTargetPath, StandardCopyOption.REPLACE_EXISTING);
			}
		}
	}

	public void setMutantFilesProvider(MutationFilesProvider mutantFilesProvider) {
		this.mutantFilesProvider = mutantFilesProvider;
	}

	public void setMarkerFileProvider(MarkerFileProvider markerFileProvider) {
		this.markerFileProvider = markerFileProvider;
	}
}
