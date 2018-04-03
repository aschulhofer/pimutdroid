package at.woodstick.pimutdroid.task

import java.nio.file.Files
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Comparator

import org.gradle.api.GradleException
import org.gradle.api.file.FileTree
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper

import at.woodstick.pimutdroid.configuration.InstrumentationTestOptions
import at.woodstick.pimutdroid.internal.MutantDetails
import at.woodstick.pimutdroid.internal.MutantGroupComparator
import at.woodstick.pimutdroid.internal.MutationFilesProvider
import at.woodstick.pimutdroid.result.Mutant
import at.woodstick.pimutdroid.result.MutantGroup
import at.woodstick.pimutdroid.result.Mutation
import at.woodstick.pimutdroid.result.MutationOverview
import at.woodstick.pimutdroid.result.MutationResult
import at.woodstick.pimutdroid.result.Outcome
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
		LOGGER.debug("Gather results and compare with expected result")
		
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
		FileTree mutantMarkerFiles = mutationFilesProvider.getMutantMarkerFiles(mutantsResultDir);
		
		int numMutants = mutantMarkerFiles.size()
		LOGGER.debug("Found $numMutants mutant marker files")
		
		if(numMutants == 0) {
			throw new GradleException("No mutant found to create mutation result for. Configured name (${mutantResultTestFilename})")
		}
		
		int mutantsKilled = 0;
		
		Map<MutantGroupKey, List<MutantDetailResult>> mutantGroupMap = new HashMap<>();
		
		mutantMarkerFiles.eachWithIndex { File markerfile, index ->
		
			MutantDetails mutantDetails = mapper.readValue(Files.newInputStream(markerfile.toPath()), MutantDetails.class);
			
			MutantGroupKey mutantKey = MutantGroupKey.of(mutantDetails.getClazzPackage(), mutantDetails.getClazzName(), mutantDetails.getFilename());

			List<MutantDetailResult> mutantGroupList = null;
			if(mutantGroupMap.containsKey(mutantKey)) {
				mutantGroupList = mutantGroupMap.get(mutantKey);
			} else {
				mutantGroupList = new ArrayList<>();
				mutantGroupMap.put(mutantKey, mutantGroupList);
			}
			
			File resultFile = markerfile.getParentFile().toPath().resolve(mutantResultTestFilename).toFile();
			
			LOGGER.lifecycle("Check mutant with id {} for result xml in directory '{}'", mutantDetails.getMuid(), markerfile.getParentFile());
			
			if(!resultFile.exists()) {
				LOGGER.lifecycle("Mutant not killed.\t$index\t$resultFile - does not exist")
				mutantGroupList.add(MutantDetailResult.noResult(mutantDetails));
				return;
			}
			
			// Empty files count as stillborn mutants (tests could not be run because app crashed on startup)
			if(resultFile.length() == 0) {
				LOGGER.lifecycle("Mutant killed.\t$index\t$resultFile - was empty, mutant counts as killed")
				mutantGroupList.add(MutantDetailResult.killed(mutantDetails));
				mutantsKilled++;
				return;
			}
			
			TestSuiteResult result = null;
				
			try {
				result = mapper.readValue(Files.newInputStream(resultFile.toPath()), TestSuiteResult.class);
			} catch(IOException e) {
				LOGGER.lifecycle("Mutant killed.\t$index\t$resultFile - error parsing mutant result xml, mutant counts as killed")
				LOGGER.warn("Error parsing mutant result xml", e)
				mutantGroupList.add(MutantDetailResult.killed(mutantDetails));
				mutantsKilled++;
				return;
			}
			
			LOGGER.debug("Result $index\t$resultFile\t$result")
			
			if(!result.equals(expectedResult)) {
				LOGGER.lifecycle("Mutant killed.\t$index\t$resultFile")
				mutantGroupList.add(MutantDetailResult.killed(mutantDetails));
				mutantsKilled++;
			} else {
				LOGGER.lifecycle("Mutant not killed.\t$index\t$resultFile")
				mutantGroupList.add(MutantDetailResult.lived(mutantDetails));
			}
		}

		BigDecimal mutationScore = ((mutantsKilled / numMutants)*100);
		
		LOGGER.lifecycle("Mutants killed: $mutantsKilled / $numMutants")
		LOGGER.lifecycle("Mutation score is $mutationScore%")
		
		// Write mutation result xml file
		final String resultTimeStampString = nowAsTimestampString();
		File mutationResultXmlFile = project.file("${resultOutputDir}/mutation-result-${resultTimeStampString}.xml");

		Files.createDirectories(mutationResultXmlFile.getParentFile().toPath());
		
		MutationOverview overview = new MutationOverview(mutantsKilled, numMutants, mutationScore.doubleValue());
		TestSetup testSetup = getTestSetup();
		Collection<MutantGroup> mutantGroupList = getMutantGroupList(mutantGroupMap);
				
		MutationResult mutatuionResult = new MutationResult(resultTimeStampString, overview, testSetup, mutantGroupList);
		
		mapper.writeValue(Files.newOutputStream(mutationResultXmlFile.toPath()), mutatuionResult);
	}

	private Collection<MutantGroup> getMutantGroupList(final Map<MutantGroupKey, List<MutantDetailResult>> mutantGroupMap) {
		TreeSet<MutantGroup> mutantGroupList = new TreeSet<>(MutantGroupComparator.getDefault());
		
		mutantGroupMap.each { MutantGroupKey key, List<MutantDetailResult> detailList ->
			
			List<Mutant> mutantList = new ArrayList<>();
			int killed = 0;
			detailList.each { MutantDetailResult resultDetails ->
				
				MutantDetails details = resultDetails.getDetails();
				Mutation mutation = new Mutation(details.getMethod(), details.getLineNumber(), details.getMutator(), details.getDescription());
				Mutant mutant = new Mutant(details.getMuid(), resultDetails.getOutcome(), mutation)
				mutantList.add(mutant);
				
				if(resultDetails.getOutcome() == Outcome.KILLED) {
					killed++;
				}
			}
			
			MutantGroup group = new MutantGroup(
				key.getMutantPackage(),
				key.getMutantClass(),
				detailList.size(),
				killed,
				key.getFilename(),
				mutantList
			);
			
			mutantGroupList.add(group);
		}
		
		return mutantGroupList;
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
