package at.woodstick.pimutdroid.task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import org.gradle.api.GradleException;
import org.gradle.api.file.FileTree;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import at.woodstick.pimutdroid.internal.MarkerFileFactory;
import at.woodstick.pimutdroid.internal.MutantDetails;
import at.woodstick.pimutdroid.internal.MutantDetailsParser;
import at.woodstick.pimutdroid.internal.MutantMarkerFile;
import at.woodstick.pimutdroid.internal.MutationFilesProvider;
import at.woodstick.pimutdroid.internal.XmlFileMapper;

public class PrepareMutantFilesTask extends PimutBaseTask {
	static final Logger LOGGER = Logging.getLogger(PrepareMutantFilesTask.class);
	
	private Set<String> targetedMutants;
	
	private MutationFilesProvider mutantFilesProvider;
	private MarkerFileFactory markerFileFactory;
	
	@Override
	protected void beforeTaskAction() {
		if(targetedMutants == null) {
			targetedMutants = new HashSet<>();
		}
		
		mutantFilesProvider = new MutationFilesProvider(getProject(), extension, targetedMutants);
		markerFileFactory = getMarkerFileFactory();
	}
	
	@Override
	protected void exec() {
		FileTree mutantClassFilesFileTree = mutantFilesProvider.getAllMutantClassFiles();
		
		Set<File> innerClassesDirSet = new HashSet<>();
		
		final XmlFileMapper xmlFileWriter = XmlFileMapper.get();
		
		final MutantDetailsParser mutantDetailsParser = new MutantDetailsParser();
		
		// Create marker files for mutant class files and store root dirs of inner class mutants
		for(File file : mutantClassFilesFileTree) {
			final String fileName = file.getName();
			
			if(fileName.contains("$")) {
				File innerClassMutantRootDir = file.getParentFile().getParentFile().getParentFile();
				innerClassesDirSet.add(innerClassMutantRootDir);
				
				LOGGER.debug("Marker file for inner class mutant {} in mutant rootDir {}", fileName, innerClassMutantRootDir);
			}
			
			final MutantMarkerFile markerFile = markerFileFactory.fromClassFile(file);
			File muidFile = markerFile.getFile();
			File mutantDetailsFile = muidFile.getParentFile().toPath().resolve("details.txt").toFile();
			
			try {
				MutantDetails mutantDetails = mutantDetailsParser.parseFromFile(muidFile.getName(), mutantDetailsFile);
				xmlFileWriter.writeTo(muidFile, mutantDetails);
			
				LOGGER.debug("markerfile {} - {}", markerFile.getFileName(), file.getAbsolutePath());
			} catch (IOException e) {
				throw new GradleException("Unable to create marker file", e);
			}
		}
		
		try {
			moveInnerMutantClassDirs(innerClassesDirSet);
		} catch (IOException e) {
			throw new GradleException("Unable handle inner classes", e);
		}
	}
	
	protected void moveInnerMutantClassDirs(final Set<File> innerClassesDirSet) throws IOException {
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
				LOGGER.debug("Mutant rootDir move {} to dir {}", innerClassDir, targetDirName);
				Files.move(innerClassDir.toPath(), moveTargetPath, StandardCopyOption.REPLACE_EXISTING);
			}
		}
	}

	public Set<String> getTargetedMutants() {
		return targetedMutants;
	}

	public void setTargetedMutants(Set<String> targetedMutants) {
		this.targetedMutants = targetedMutants;
	}
}
