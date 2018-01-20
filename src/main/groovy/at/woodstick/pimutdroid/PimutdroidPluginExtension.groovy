package at.woodstick.pimutdroid;

import javax.inject.Inject

import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory

import at.woodstick.pimutdroid.configuration.InstrumentationTestOptions
import groovy.transform.CompileStatic
import info.solidsoft.gradle.pitest.PitestPluginExtension;

@CompileStatic
class PimutdroidPluginExtension {
	PitestPluginExtension pitest;

	final InstrumentationTestOptions instrumentationTestOptions = new InstrumentationTestOptions();
	
	String packageDir;
	String mutantsDir;
	Boolean outputMutantCreation;
	
	String outputDir;
	
	String testReportDir;
	String testResultDir;
	
	String mutantResultRootDir;
	String appResultRootDir;
	String classFilesDir;
	
	public PimutdroidPluginExtension() {
		
	}
	
// In gradle 4.3.1 project.objects
//	@Inject
//	PimutdroidPluginExtension(ObjectFactory objectFactory) {
//		instrumentationTestOptions = objectFactory.newInstance(InstrumentationTestOptions.class);
//	}
	
	public void instrumentationTestOptions(Action<? extends InstrumentationTestOptions> action) {
		action.execute(instrumentationTestOptions);
	}

	public PitestPluginExtension getPitest() {
		return pitest;
	}

	public void setPitest(PitestPluginExtension pitest) {
		this.pitest = pitest;
	}

	public String getPackageDir() {
		return packageDir;
	}

	public void setPackageDir(String packageDir) {
		this.packageDir = packageDir;
	}

	public String getMutantsDir() {
		return mutantsDir;
	}

	public void setMutantsDir(String mutantsDir) {
		this.mutantsDir = mutantsDir;
	}

	public Boolean getOutputMutantCreation() {
		return outputMutantCreation;
	}

	public void setOutputMutantCreation(Boolean outputMutantCreation) {
		this.outputMutantCreation = outputMutantCreation;
	}

	public String getOutputDir() {
		return outputDir;
	}

	public void setOutputDir(String outputDir) {
		this.outputDir = outputDir;
	}

	public String getTestReportDir() {
		return testReportDir;
	}

	public void setTestReportDir(String testReportDir) {
		this.testReportDir = testReportDir;
	}

	public String getTestResultDir() {
		return testResultDir;
	}

	public void setTestResultDir(String testResultDir) {
		this.testResultDir = testResultDir;
	}

	public String getMutantResultRootDir() {
		return mutantResultRootDir;
	}

	public void setMutantResultRootDir(String mutantResultRootDir) {
		this.mutantResultRootDir = mutantResultRootDir;
	}

	public String getAppResultRootDir() {
		return appResultRootDir;
	}

	public void setAppResultRootDir(String appResultRootDir) {
		this.appResultRootDir = appResultRootDir;
	}

	public String getClassFilesDir() {
		return classFilesDir;
	}

	public void setClassFilesDir(String classFilesDir) {
		this.classFilesDir = classFilesDir;
	}

	public InstrumentationTestOptions getInstrumentationTestOptions() {
		return instrumentationTestOptions;
	}
}
