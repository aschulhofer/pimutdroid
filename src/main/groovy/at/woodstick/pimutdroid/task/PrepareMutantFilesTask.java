package at.woodstick.pimutdroid.task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
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
import at.woodstick.pimutdroid.internal.PimutdroidException;
import at.woodstick.pimutdroid.internal.UnitTestResult;
import at.woodstick.pimutdroid.internal.UnitTestResultProvider;
import at.woodstick.pimutdroid.internal.XmlFileMapper;
import at.woodstick.pimutdroid.internal.pitest.PitestUnitTestResultProvider;
import at.woodstick.pimutdroid.result.Outcome;

public class PrepareMutantFilesTask extends PimutBaseTask {
	static final Logger LOGGER = Logging.getLogger(PrepareMutantFilesTask.class);
	
	private Set<String> targetedMutants;
	private Boolean ignoreKilledByUnitTest;
	private File unitTestResultFile;
	
	private MutationFilesProvider mutantFilesProvider;
	private MarkerFileFactory markerFileFactory;
	
	private UnitTestResult noopUnitTestResult = new UnitTestResult() {
		@Override
		public boolean isKilled(String index, String mutator, String method, String mutatedClass, String sourceFile) {
			return false;
		}
		
		@Override
		public boolean hasResults() {
			return false;
		}
		
		@Override
		public Outcome getOutcome(String index, String mutator, String method, String mutatedClass, String sourceFile) {
			return Outcome.LIVED;
		}
	};
	
	@Override
	protected void beforeTaskAction() {
		if(targetedMutants == null) {
			targetedMutants = new HashSet<>();
		}
		
		if(ignoreKilledByUnitTest == null) {
			ignoreKilledByUnitTest = extension.getIgnoreKilledByUnitTest();
		}
		
		mutantFilesProvider = new MutationFilesProvider(getProject(), extension, targetedMutants);
		markerFileFactory = getMarkerFileFactory();
	}
	
	protected Optional<File> getUnitTestResulFile() {
		if(unitTestResultFile == null) {
			
			FileTree resultFiles = getProject().fileTree(extension.getMutantClassesDir(), (config -> {
				config.setIncludes(Arrays.asList("**/mutations.xml"));
			}));
			
			if(!resultFiles.isEmpty()) {
				Set<File> files = resultFiles.getFiles();
				List<File> fileList = new ArrayList<>(files);
				unitTestResultFile = fileList.get(files.size()-1);
			}
		}
		
		LOGGER.debug("Use unit test result file {}", unitTestResultFile);
		
		return Optional.ofNullable(unitTestResultFile);
	}
	
	@Override
	protected void exec() {
		FileTree mutantClassFilesFileTree = mutantFilesProvider.getAllMutantClassFiles();
		
		Set<File> innerClassesDirSet = new HashSet<>();
		
		final XmlFileMapper xmlFileWriter = XmlFileMapper.get();
		
		final MutantDetailsParser mutantDetailsParser = new MutantDetailsParser();
		
		final Optional<File> unitTestResultFile = getUnitTestResulFile();
		
		UnitTestResult unitTestResult = noopUnitTestResult;
		
		if(ignoreKilledByUnitTest) {
			if(!unitTestResultFile.isPresent()) {
				LOGGER.error("No unit test result file found");
				throw new GradleException("No unit test result file found, ignoreKilledByUnitTest is set to true");
			}
			
			final UnitTestResultProvider unitTestResultProvider = new PitestUnitTestResultProvider(unitTestResultFile.get(), xmlFileWriter);
			
			try {
				unitTestResult = unitTestResultProvider.getResult();
			} catch(PimutdroidException e) {
				LOGGER.warn("Error retrieving unit test result");
				throw new GradleException("Error retrieving unit test result, ignoreKilledByUnitTest is set to true");
			}
			
			if(unitTestResult.hasResults()) {
				LOGGER.debug("Unit test result file '{}' contains no results", unitTestResultFile.get());
			}
		}
		
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
				
				String indexes = mutantDetails.getIndexes();
				String firstIndex = indexes.split(",")[0];
				
				boolean isKilled = unitTestResult.isKilled(firstIndex, mutantDetails.getMutator(), mutantDetails.getMethod(), mutantDetails.getClazz(), mutantDetails.getFilename());

				LOGGER.debug("Mutant '{}' killed by unit test: {}", mutantDetails.getMuid(), isKilled);
				
				if(ignoreKilledByUnitTest && isKilled) {
					LOGGER.info("Mutant killed by unit test: {}", mutantDetails.getMuid(), isKilled);
				}
				
				mutantDetails.setKilledByUnitTest(isKilled);
				
				xmlFileWriter.writeTo(muidFile, mutantDetails);

				LOGGER.debug("markerfile {} - {}", markerFile.getFileName(), file.getAbsolutePath());
				
			} catch (IOException e) {
				throw new GradleException("Unable to create marker file", e);
			}
		}
		
		try {
			moveInnerMutantClassDirs(innerClassesDirSet);
		} catch (IOException e) {
			LOGGER.error("Unable to prepare inner classes", e);
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
