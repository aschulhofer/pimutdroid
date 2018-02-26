package at.woodstick.pimutdroid;

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.tasks.Internal

import at.woodstick.pimutdroid.configuration.BuildConfiguration
import at.woodstick.pimutdroid.configuration.InstrumentationTestOptions
import groovy.transform.CompileStatic

@CompileStatic
class PimutdroidPluginExtension {
	final InstrumentationTestOptions instrumentationTestOptions = new InstrumentationTestOptions();
	
	final NamedDomainObjectContainer<BuildConfiguration> buildConfiguration;
	
	/**
	 * Default root dir for output files
	 */
	String outputDir;
	
	/**
	 * Root dir path of pitest exported mutated class files
	 */
	String mutantClassesDir;
	
	/**
	 * Default application package.
	 * E.g.: "at.woodstick.app"
	 */
	String applicationPackage;
	
	/**
	 * Relative dir path of application package where mutant class files reside in.
	 * E.g.: "at.woodstick.app" -> "at/woodstick/app" 
	 */
	String packageDir;
	
	/**
	 * Root dir of mutation files created by this plugin
	 */
	String mutantResultRootDir;
	
	/**
	 * Root dir of application files that are used for mutation (created by this plugin)
	 */
	String appResultRootDir;
	
	/**
	 * Root dir of application class files, will be replaced with mutated class files 
	 */
	String classFilesDir;
	
	/**
	 * Root dir to backup application class files
	 */
	String classFilesBackupDir;
	
	/**
	 * Output dir of mutation reports (E.g.: xml result file)
	 */
	String mutantReportRootDir;
	
	/**
	 * The application ID
	 */
	String applicationId;
	
	/**
	 * The application ID suffix
	 */
	String applicationIdSuffix;
	
	/**
	 * Test application ID
	 */
	String testApplicationId;
	
	/**
	 * Location of application apk (normally created by android plugin) 
	 */
	String apkAppOutputRootDir;
	
	/**
	 * Location of test application apk (normally created by android plugin) 
	 */
	String apkTestOutputRootDir;

	/**
	 * Gradle property name used by certain tasks to pass mutant ids to tasks (internally used)
	 */
	String muidProperty;
	
	/**
	 * Name of application apk file (must end with .apk extension)
	 */
	String apkName;
	
	/**
	 * Name of application test apk file (must end with .apk extension)
	 */
	String testApkName;

	/**
	 * Specifies the build type that the plugin should use to test the module
	 */
	String testBuildType;
		
	/**
	 * Product flavor to use
	 */
	String productFlavor;
	
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

	public String getApplicationPackage() {
		return applicationPackage;
	}

	public void setApplicationPackage(String applicationPackage) {
		this.applicationPackage = applicationPackage;
	}

	public String getPackageDir() {
		return packageDir;
	}

	public void setPackageDir(String packageDir) {
		this.packageDir = packageDir;
	}

	public String getMutantClassesDir() {
		return mutantClassesDir;
	}

	public void setMutantClassesDir(String mutantClassesDir) {
		this.mutantClassesDir = mutantClassesDir;
	}

	public String getOutputDir() {
		return outputDir;
	}

	public void setOutputDir(String outputDir) {
		this.outputDir = outputDir;
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

	public String getApplicationIdSuffix() {
		return applicationIdSuffix;
	}

	public void setApplicationIdSuffix(String applicationIdSuffix) {
		this.applicationIdSuffix = applicationIdSuffix;
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

	public String getApkName() {
		return apkName;
	}

	public void setApkName(String apkName) {
		this.apkName = apkName;
	}

	public String getTestApkName() {
		return testApkName;
	}

	public void setTestApkName(String testApkName) {
		this.testApkName = testApkName;
	}

	public String getTestBuildType() {
		return testBuildType;
	}

	public void setTestBuildType(String testBuildType) {
		this.testBuildType = testBuildType;
	}

	public String getProductFlavor() {
		return productFlavor;
	}

	public void setProductFlavor(String productFlavor) {
		this.productFlavor = productFlavor;
	}
}
