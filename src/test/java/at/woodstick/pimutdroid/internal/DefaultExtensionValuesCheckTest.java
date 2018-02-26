package at.woodstick.pimutdroid.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.File;
import java.util.Set;

import org.easymock.EasyMockRule;
import org.easymock.Mock;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import com.android.build.gradle.BaseExtension;
import com.android.build.gradle.internal.dsl.DefaultConfig;
import com.android.build.gradle.internal.dsl.ProductFlavor;

import at.woodstick.pimutdroid.PimutdroidPlugin;
import at.woodstick.pimutdroid.PimutdroidPluginExtension;
import at.woodstick.pimutdroid.configuration.BuildConfiguration;
import at.woodstick.pimutdroid.configuration.InstrumentationTestOptions;
import at.woodstick.pimutdroid.configuration.TargetTests;
import info.solidsoft.gradle.pitest.PitestPluginExtension;

public class DefaultExtensionValuesCheckTest {

	@Rule
	public EasyMockRule mockRule = new EasyMockRule(this);
	
	private DefaultExtensionValuesCheck unitUnderTest;
	
	private static Project project;
	
	private PimutdroidPluginExtension extension;
	private NamedDomainObjectContainer<BuildConfiguration> buildConfiguration;
	
	private String projectName;
	private File buildDir;
	
	@Mock
	private BaseExtension androidExtension;
	
	@Mock
	private DefaultConfig androidDefaultConfig;
	
	@Mock
	private PitestPluginExtension pitestExtension;

	private String androidConfigApplicationId;
	private String androidConfigTestApplicationId;
	private String androidConfigApplicationIdSuffix;
	private String androidConfigTestBuildType;
	private NamedDomainObjectContainer<ProductFlavor> productFlavors;
	private String androidConfigTestInstrumentationRunner;

	private File pitestReportDir;
	
	@BeforeClass
	public static void init() {
		project = ProjectBuilder.builder().withName("project-mock").build();
	}
	
	@Before
	public void setUp() {
		buildConfiguration = project.container(BuildConfiguration.class);
		
		extension = new PimutdroidPluginExtension(buildConfiguration);
		
		projectName = project.getName();
		buildDir = project.getBuildDir(); 
		
		unitUnderTest = new DefaultExtensionValuesCheck(project, extension, androidExtension, pitestExtension);
		
		setExtensionDefaultValues();
	}
	
	protected void setExtensionDefaultValues() {
		androidConfigApplicationId = "at.woodstick.junit";
		androidConfigTestApplicationId = null;
		androidConfigApplicationIdSuffix = null;
		androidConfigTestBuildType = "debug";
		productFlavors = project.container(ProductFlavor.class);
		androidConfigTestInstrumentationRunner = null;
		
		pitestReportDir = buildDir.toPath().resolve("reportDir").toFile();
	}
	
	@After
	public void tearDown() {
		projectName = null;
		buildDir = null;
		
		unitUnderTest = null;
		
		clearExtensionDefaultValues();
	}
	
	protected void clearExtensionDefaultValues() {
		androidConfigApplicationId = null;
		androidConfigTestApplicationId = null;
		androidConfigApplicationIdSuffix = null;
		androidConfigTestBuildType = null;
		productFlavors = null;
		androidConfigTestInstrumentationRunner = null;
		
		pitestReportDir = null;
	}
	
	@Test
	public void checkAndSetValues_constructor_notNull() {
		assertThat(unitUnderTest).isNotNull();
	}
	
	@Test
	public void checkAndSetValues_extensionNoValuesSupplied_hasDefaultValues() {
		
		String applicationId = "at.woodstick.junit";
		String testApplicationId = applicationId + ".test";
		String testBuildType = "debug";
		
		String applicationPackage = applicationId;
		String targetMutantsBlob = applicationPackage + ".*";
		
		String appApkName  = projectName + "-" + testBuildType + ".apk";
		String testApkName = projectName + "-" + testBuildType + "-" + "androidTest" + ".apk";
		
		run_checkAndSetValues();
		
		InstrumentationTestOptions instrumentationTestOptions = extension.getInstrumentationTestOptions();
		assertThat(instrumentationTestOptions).isNotNull();
		
		assertIntrumentationTestRunner(PimutdroidPlugin.RUNNER);
		
		Set<String> targetMutants = instrumentationTestOptions.getTargetMutants();
		assertThat(targetMutants).isNotNull().isNotEmpty().hasSize(1);
		assertThat(targetMutants).contains(targetMutantsBlob);
		
		TargetTests targetTests = instrumentationTestOptions.getTargetTests();
		assertThat(targetTests).isNotNull();
		assertThat(targetTests.getClasses()).isNull();
		assertThat(targetTests.getPackages()).isNull();
		
		assertThat(extension.getMuidProperty()).isEqualTo(PimutdroidPlugin.PROPERTY_NAME_MUID);
		
		assertThat(extension.getApplicationId()).isEqualTo(applicationId);
		assertThat(extension.getTestApplicationId()).isEqualTo(testApplicationId);
		assertThat(extension.getApplicationIdSuffix()).isNull();
		assertThat(extension.getApplicationPackage()).isEqualTo(applicationPackage);
		assertThat(extension.getPackageDir()).isEqualTo(applicationPackage.replaceAll("\\.", "/"));
		
		assertThat(extension.getApkName()).isEqualTo(appApkName);
		assertThat(extension.getTestApkName()).isEqualTo(testApkName);
		assertThat(extension.getApkAppOutputRootDir()).isEqualTo(project.getBuildDir() + "/outputs/apk/" + testBuildType);
		assertThat(extension.getApkTestOutputRootDir()).isEqualTo(project.getBuildDir() + "/outputs/apk/androidTest/" + testBuildType);
		
		assertThat(extension.getOutputDir()).isEqualTo(project.getBuildDir() + "/mutation");
		
		assertThat(extension.getMutantResultRootDir()).isEqualTo(extension.getOutputDir() + "/mutants");
		assertThat(extension.getMutantReportRootDir()).isEqualTo(extension.getOutputDir() + "/result");
		assertThat(extension.getMutantClassesDir()).isEqualTo(pitestReportDir + "/" + testBuildType);
		
		assertThat(extension.getAppResultRootDir()).isEqualTo(extension.getOutputDir() + "/app/" + testBuildType);
		assertThat(extension.getClassFilesBackupDir()).isEqualTo(extension.getAppResultRootDir() + "/backup/classes");
		assertThat(extension.getClassFilesDir()).isEqualTo(project.getBuildDir() + "/intermediates/classes/" + testBuildType);
	}
	
	@Test
	public void checkAndSetValues_extensionRunnerSetInAndroidExtension_notSetInPluginExtension_runnerIsSetToAndroidConfig() {
		
		String testInstrumentationRunner = "org.catrobat.paintroid.test.utils.CustomAndroidJUnitRunner";
		
		androidConfigTestInstrumentationRunner = testInstrumentationRunner;

		run_checkAndSetValues();
		
		assertIntrumentationTestRunner(testInstrumentationRunner);
	}
	
	@Test
	public void checkAndSetValues_extensionRunnerSupplied_notSetInAndroidExtension_runnerIsSetToPluginExtension() {
		
		String testInstrumentationRunner = "org.catrobat.paintroid.test.utils.CustomAndroidJUnitRunner";
		
		extension.getInstrumentationTestOptions().setRunner(testInstrumentationRunner);
		
		run_checkAndSetValues();
		
		assertIntrumentationTestRunner(testInstrumentationRunner);
	}
	
	@Test
	public void checkAndSetValues_extensionRunnerSupplied_alsoSetInAndroidExtension_runnerIsSetToPluginExtension() {
		
		String testInstrumentationRunner = "org.catrobat.paintroid.test.utils.CustomAndroidJUnitRunner";
		
		androidConfigTestInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner";
		
		extension.getInstrumentationTestOptions().setRunner(testInstrumentationRunner);
		
		run_checkAndSetValues();
		
		assertIntrumentationTestRunner(testInstrumentationRunner);
	}
	
	// ########################################################################
	
	private void assertIntrumentationTestRunner(final String testInstrumentationRunner) {
		InstrumentationTestOptions instrumentationTestOptions = extension.getInstrumentationTestOptions();
		assertThat(instrumentationTestOptions).isNotNull();
		assertThat(instrumentationTestOptions.getRunner()).isEqualTo(testInstrumentationRunner);
	}
	
	// ########################################################################
	
	private void run_checkAndSetValues() {
		expectAndroidConfigValues();
		expectPitestConfigValues();
		
		replay( androidExtension, androidDefaultConfig, pitestExtension );
		
		unitUnderTest.checkAndSetValues();
		
		verify( androidExtension, androidDefaultConfig, pitestExtension );
	}
	
	// ########################################################################
	
	private void expectAndroidConfigValues() {
		expect( androidExtension.getDefaultConfig() ).andReturn(androidDefaultConfig).anyTimes();
		
		expect( androidDefaultConfig.getApplicationId() ).andReturn(androidConfigApplicationId).anyTimes();
		
		expect( androidDefaultConfig.getTestApplicationId() ).andReturn(androidConfigTestApplicationId).anyTimes();
		
		expect( androidDefaultConfig.getApplicationIdSuffix() ).andReturn(androidConfigApplicationIdSuffix).anyTimes();
		
		expect( androidExtension.getTestBuildType() ).andReturn(androidConfigTestBuildType).anyTimes();
		
		expect( androidExtension.getProductFlavors() ).andReturn(productFlavors).anyTimes();
		
		expect( androidDefaultConfig.getTestInstrumentationRunner() ).andReturn(androidConfigTestInstrumentationRunner).anyTimes();
	}
	
	private void expectPitestConfigValues() {
		expect( pitestExtension.getReportDir() ).andReturn(pitestReportDir).anyTimes();
	}
}
