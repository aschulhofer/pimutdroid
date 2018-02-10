package at.woodstick.pimutdroid.task

import java.nio.file.Files
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileTree
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper

import at.woodstick.pimutdroid.internal.MutationFilesProvider
import at.woodstick.pimutdroid.result.MutationResult
import at.woodstick.pimutdroid.result.TestSuiteResult
import groovy.transform.CompileStatic

@CompileStatic
public class AfterMutationTask extends DefaultTask {
	private final static Logger LOGGER = Logging.getLogger(AfterMutationTask);
	
	private MutationFilesProvider mutationFilesProvider;
	
	private String outputDir;
	private String appResultDir;
	private String mutantsResultDir;
	
	@TaskAction
	void exec() {
		LOGGER.lifecycle "Gather results and compare with expected result"
		
		final ObjectMapper mapper = new XmlMapper();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		
		LOGGER.lifecycle "Output dir: $outputDir"
		LOGGER.lifecycle "App result dir: $appResultDir"
		LOGGER.lifecycle "Mutants result dir: $mutantsResultDir"
		
		FileTree appResult = project.fileTree(
			dir: appResultDir,
			include: "**/*.xml"
		)
		
		if(appResult.files.isEmpty()) {
			// TODO: is message still correct?
			LOGGER.lifecycle "App result not present 'run task \"prepareMutation\"'";
			return;
		}
		
		File appResultFile = appResult.files.first()
		
		TestSuiteResult expectedResult = mapper.readValue(Files.newInputStream(appResultFile.toPath()), TestSuiteResult.class);
		LOGGER.lifecycle "Expected result $expectedResult"
		
		// Handle mutants
		FileTree mutantsResults = mutationFilesProvider.getMutantResultTestFiles();
		
		int numMutants = mutantsResults.size()
		LOGGER.lifecycle "Found $numMutants mutant test results" 
		
		if(numMutants == 0) {
			LOGGER.lifecycle "No mutants found to create result for."
			return;
		}
		
		int mutantsKilled = 0;
		
		mutantsResults.eachWithIndex { File file, index ->
			try {
				TestSuiteResult result = mapper.readValue(Files.newInputStream(file.toPath()), TestSuiteResult.class);
				
				LOGGER.debug "Result $index \t $file \t $result"
				
				if(!result.equals(expectedResult)) {
					mutantsKilled++;
					LOGGER.lifecycle "Mutant killed. $index \t $file"
				}
			} catch(IOException e) {
				LOGGER.warn "Error parsing mutant result xml", e
				LOGGER.warn "Mutant counts as killed"
				LOGGER.warn "Mutant file: ($index) $file"
				mutantsKilled++;
			}
		}

		BigDecimal mutationScore = ((mutantsKilled / numMutants)*100);
		
		LOGGER.lifecycle "Mutants killed: $mutantsKilled / $numMutants"
		LOGGER.lifecycle "Mutation score is $mutationScore%"
		
		// Write mutation result xml file
		final String resultTimeStampString = nowAsTimestampString();
		def mutationResultXmlFile = project.file("${outputDir}/mutation-result-${resultTimeStampString}.xml");

		MutationResult mutatuionResult = new MutationResult(mutantsKilled, numMutants, mutationScore.doubleValue());
		mapper.writeValue(Files.newOutputStream(mutationResultXmlFile.toPath()), mutatuionResult);
	}

	private String nowAsTimestampString() {
		return DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
	}
	
	public void setMutationFilesProvider(MutationFilesProvider mutationFilesProvider) {
		this.mutationFilesProvider = mutationFilesProvider;
	}

	public void setOutputDir(String outputDir) {
		this.outputDir = outputDir;
	}

	public void setAppResultDir(String appResultDir) {
		this.appResultDir = appResultDir;
	}

	public void setMutantsResultDir(String mutantsResultDir) {
		this.mutantsResultDir = mutantsResultDir;
	}
}
