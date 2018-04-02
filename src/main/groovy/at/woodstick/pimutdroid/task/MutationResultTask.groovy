package at.woodstick.pimutdroid.task

import java.nio.file.Files
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import org.gradle.api.GradleException
import org.gradle.api.file.FileTree
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import com.ctc.wstx.exc.WstxEOFException
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser

import at.woodstick.pimutdroid.configuration.InstrumentationTestOptions
import at.woodstick.pimutdroid.internal.MutantDetails
import at.woodstick.pimutdroid.internal.MutationFilesProvider
import at.woodstick.pimutdroid.result.MutationOverview
import at.woodstick.pimutdroid.result.MutationResult
import at.woodstick.pimutdroid.result.TestSetup
import at.woodstick.pimutdroid.result.TestSuiteResult
import groovy.transform.CompileStatic

@CompileStatic
public class MutationResultTask extends PimutBaseTask {
	private final static Logger LOGGER = Logging.getLogger(MutationResultTask);
	
	private String expectedResultTestFilename;
	private String mutantResultTestFilename;
	private String resultOutputDir;
	private String appResultDir;
	private String mutantsResultDir;
	private Set<String> targetedMutants;
	
	private MutationFilesProvider mutationFilesProvider;
	
	@Override
	protected void beforeTaskAction() {
		if(expectedResultTestFilename == null) {
			expectedResultTestFilename = extension.getExpectedTestResultFilename();
		}
		
		if(mutantResultTestFilename == null) {
			mutantResultTestFilename = extension.getMutantTestResultFilename();
		}
		
		if(resultOutputDir == null) {
			outputDir = extension.getMutantReportRootDir();
		}
		
		if(appResultDir == null) {
			appResultDir = extension.getAppResultRootDir();
		}
		
		if(mutantsResultDir == null) {
			mutantsResultDir = extension.getMutantResultRootDir();
		}
		
		if(targetedMutants == null) {
			targetedMutants = new HashSet<>();
		}
		
		mutationFilesProvider = new MutationFilesProvider(project, extension, targetedMutants, mutantResultTestFilename);
	}
	
	@Override
	protected void exec() {
		LOGGER.lifecycle("Gather results and compare with expected result")
		
		final ObjectMapper mapper = new XmlMapper();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		
		LOGGER.debug("Output dir: $resultOutputDir")
		LOGGER.debug("App result dir: $appResultDir")
		LOGGER.debug("Mutants result dir: $mutantsResultDir")
		LOGGER.debug("Targeted Mutants: $targetedMutants")

		File appResultFile = project.file("${appResultDir}/${expectedResultTestFilename}")

		if(!appResultFile.exists() || !appResultFile.isFile()) {
			LOGGER.error("Expected app result file not present.")
			LOGGER.error("Expected file is '${appResultFile}'")
			throw new GradleException("Expected app result file not present. ('${appResultFile}')");
		}
				
		TestSuiteResult expectedResult = mapper.readValue(Files.newInputStream(appResultFile.toPath()), TestSuiteResult.class);
		LOGGER.debug("Expected result $expectedResult")
		
		// Handle mutants
		FileTree mutantsResultFiles = mutationFilesProvider.getMutantMarkerFiles(mutantsResultDir);
		
		int numMutants = mutantsResultFiles.size()
		LOGGER.debug("Found $numMutants mutant test results")
		
		if(numMutants == 0) {
			throw new GradleException("No mutant results found to create mutation result from. Configured name (${mutantResultTestFilename})")
		}
		
		int mutantsKilled = 0;
		
		mutantsResultFiles.eachWithIndex { File markerfile, index ->
		
			MutantDetails mutantDetails = mapper.readValue(Files.newInputStream(markerfile.toPath()), MutantDetails.class);
			
			File file = markerfile.getParentFile().toPath().resolve(mutantResultTestFilename).toFile();
			
			LOGGER.lifecycle("Check mutant with id {} for result xml in directory '{}'", mutantDetails.getMuid(), markerfile.getParentFile());
			
			if(!file.exists()) {
				LOGGER.lifecycle("Mutant not killed.\t$index\t$file - does not exist")
				return;
			}
			
			// Empty files count as stillborn mutants (tests could not be run because app crashed on startup)
			if(file.length() == 0) {
				LOGGER.lifecycle("Mutant killed.\t$index\t$file - was empty, mutant counts as killed")
				mutantsKilled++;
				return;
			}
			
			TestSuiteResult result = null;
				
			try {
				result = mapper.readValue(Files.newInputStream(file.toPath()), TestSuiteResult.class);
			} catch(IOException e) {
				LOGGER.lifecycle("Mutant killed.\t$index\t$file - error parsing mutant result xml, mutant counts as killed")
				LOGGER.warn("Error parsing mutant result xml", e)
				mutantsKilled++;
				return;
			}
			
			LOGGER.debug("Result $index\t$file\t$result")
			
			if(!result.equals(expectedResult)) {
				mutantsKilled++;
				LOGGER.lifecycle("Mutant killed.\t$index\t$file")
			} else {
				LOGGER.lifecycle("Mutant not killed.\t$index\t$file")
			}
		}

		BigDecimal mutationScore = ((mutantsKilled / numMutants)*100);
		
		LOGGER.lifecycle("Mutants killed: $mutantsKilled / $numMutants")
		LOGGER.lifecycle("Mutation score is $mutationScore%")
		
		// Write mutation result xml file
		final String resultTimeStampString = nowAsTimestampString();
		File mutationResultXmlFile = project.file("${resultOutputDir}/mutation-result-${resultTimeStampString}.xml");

		Files.createDirectories(mutationResultXmlFile.getParentFile().toPath());
		
		TestSetup testSetup = getTestSetup();

		MutationOverview overview = new MutationOverview(mutantsKilled, numMutants, mutationScore.doubleValue());
		
		MutationResult mutatuionResult = new MutationResult(overview, testSetup);
		
		mapper.writeValue(Files.newOutputStream(mutationResultXmlFile.toPath()), mutatuionResult);
	}

	private TestSetup getTestSetup() {
		InstrumentationTestOptions testOptions = extension.getInstrumentationTestOptions();
		Set<String> packages = testOptions.getTargetTests().getPackages();
		Set<String> classes = testOptions.getTargetTests().getClasses();
		
		if(packages == null) {
			packages = Collections.emptySet();
		}
		
		if(classes == null) {
			classes = Collections.emptySet();
		}
		
		TestSetup testSetup = new TestSetup(packages, classes, testOptions.targetMutants, testOptions.getRunner());
		return testSetup;
	}
	
	private String nowAsTimestampString() {
		return DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
	}

	public String getOutputDir() {
		return resultOutputDir;
	}

	public void setOutputDir(String outputDir) {
		this.resultOutputDir = outputDir;
	}

	public String getAppResultDir() {
		return appResultDir;
	}

	public void setAppResultDir(String appResultDir) {
		this.appResultDir = appResultDir;
	}

	public String getMutantsResultDir() {
		return mutantsResultDir;
	}

	public void setMutantsResultDir(String mutantsResultDir) {
		this.mutantsResultDir = mutantsResultDir;
	}

	public Set<String> getTargetedMutants() {
		return targetedMutants;
	}

	public void setTargetedMutants(Set<String> targetedMutants) {
		this.targetedMutants = targetedMutants;
	}
}
