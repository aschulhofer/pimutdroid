package at.woodstick.pimutdroid;

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer

import at.woodstick.pimutdroid.configuration.BuildConfiguration
import at.woodstick.pimutdroid.configuration.InstrumentationTestOptions
import groovy.transform.CompileStatic

@CompileStatic
class PimutdroidPluginExtension {
	final InstrumentationTestOptions instrumentationTestOptions = new InstrumentationTestOptions();
	
	final NamedDomainObjectContainer<BuildConfiguration> buildConfiguration;
	
	String packageDir;
	String mutantsDir;
	
	String outputDir;
	
	String testReportDir;
	String testResultDir;
	
	String mutantResultRootDir;
	String appResultRootDir;
	String classFilesDir;
	String classFilesBackupDir;
	
	String mutantReportRootDir;
	
	String applicationId;
	String testApplicationId;
	
	String muidProperty;
	
	String apkAppOutputRootDir;
	String apkTestOutputRootDir;
	
	public PimutdroidPluginExtension(NamedDomainObjectContainer<BuildConfiguration> buildConfiguration) {
		this.buildConfiguration = buildConfiguration;
	}
	
//  In gradle 4.3.1 project.objects
//	@Inject
//	PimutdroidPluginExtension(ObjectFactory objectFactory) {
//		instrumentationTestOptions = objectFactory.newInstance(InstrumentationTestOptions.class);
//	}
	
	public void instrumentationTestOptions(Action<? extends InstrumentationTestOptions> action) {
		action.execute(instrumentationTestOptions);
	}
	
	public void buildConfiguration(Closure<?> configureClosure) {
		buildConfiguration.configure(configureClosure);
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

	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	public String getTestApplicationId() {
		return testApplicationId;
	}

	public void setTestApplicationId(String testApplicationId) {
		this.testApplicationId = testApplicationId;
	}

	public String getMuidProperty() {
		return muidProperty;
	}

	public void setMuidProperty(String muidProperty) {
		this.muidProperty = muidProperty;
	}

	public String getApkAppOutputRootDir() {
		return apkAppOutputRootDir;
	}

	public void setApkAppOutputRootDir(String apkAppOutputRootDir) {
		this.apkAppOutputRootDir = apkAppOutputRootDir;
	}

	public String getApkTestOutputRootDir() {
		return apkTestOutputRootDir;
	}

	public void setApkTestOutputRootDir(String apkTestOutputRootDir) {
		this.apkTestOutputRootDir = apkTestOutputRootDir;
	}

	public String getClassFilesBackupDir() {
		return classFilesBackupDir;
	}

	public void setClassFilesBackupDir(String classFilesBackupDir) {
		this.classFilesBackupDir = classFilesBackupDir;
	}

	public String getMutantReportRootDir() {
		return mutantReportRootDir;
	}

	public void setMutantReportRootDir(String mutantReportRootDir) {
		this.mutantReportRootDir = mutantReportRootDir;
	}
}
