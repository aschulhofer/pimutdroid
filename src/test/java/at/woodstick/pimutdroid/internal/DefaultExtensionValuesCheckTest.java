package at.woodstick.pimutdroid.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import org.easymock.EasyMock;
import org.easymock.EasyMockRule;
import org.easymock.Mock;
import org.gradle.api.NamedDomainObjectContainer;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.android.build.gradle.BaseExtension;
import com.android.build.gradle.internal.dsl.DefaultConfig;
import com.android.build.gradle.internal.dsl.ProductFlavor;

import at.woodstick.pimutdroid.PimutdroidPluginExtension;
import at.woodstick.pimutdroid.configuration.BuildConfiguration;
import at.woodstick.pimutdroid.configuration.InstrumentationTestOptions;
import at.woodstick.pimutdroid.configuration.TargetTests;
import info.solidsoft.gradle.pitest.PitestPluginExtension;

public class DefaultExtensionValuesCheckTest {

	private static final String DEFAULT_APPLICATION_ID = "at.woodstick.junit";
	private static final String DEFAULT_TEST_APPLICATION_ID = DEFAULT_APPLICATION_ID + ".test";

	private static final String DEFAULT_PROJECT_NAME = "project-mock";

	private static final String DEFAULT_TEST_BUILD_TYPE = "debug";
	
	private static final String DEFAULT_RUNNER = "android.support.test.runner.AndroidJUnitRunner";
	private static final String DEFAULT_PROPERTY_NAME_MUID = "pimut.muid";

	@Rule
	public EasyMockRule mockRule = new EasyMockRule(this);

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();
	
	private DefaultExtensionValuesCheck unitUnderTest;
	
	private PimutdroidPluginExtension extension;
	
	private String projectName;
	private File buildDir;
	
	@Mock
	private BaseExtension androidExtension;
	
	@Mock
	private DefaultConfig androidDefaultConfig;
	
	@Mock
	private PitestPluginExtension pitestExtension;
	
	@Mock
	private NamedDomainObjectContainer<BuildConfiguration> buildConfigContainerMock;
	boolean hasNoBuildConfiguration = true;


	private String androidConfigApplicationId;
	private String androidConfigTestApplicationId;
	private String androidConfigApplicationIdSuffix;
	private String androidConfigTestBuildType;
	
	@Mock
	private NamedDomainObjectContainer<ProductFlavor> androidConfigProductFlavors;
	boolean hasNoProductFlavor = true;
	
	private String androidConfigTestInstrumentationRunner;

	private File pitestReportDir;
	
	private List<Object> additionalMocks;
	
	@Before
	public void setUp() throws IOException {
		additionalMocks = new ArrayList<Object>();
		
		extension = new PimutdroidPluginExtension(buildConfigContainerMock);
		
		projectName = DEFAULT_PROJECT_NAME;
		buildDir = tempFolder.newFolder();
		
		hasNoBuildConfiguration = true;
		hasNoProductFlavor = true;
		
		unitUnderTest = new DefaultExtensionValuesCheck(projectName, buildDir, extension, androidExtension, pitestExtension);
		
		setExtensionDefaultValues();
	}
	
	protected void setExtensionDefaultValues() {
		androidConfigApplicationId = DEFAULT_APPLICATION_ID;
		androidConfigTestApplicationId = null;
		androidConfigApplicationIdSuffix = null;
		androidConfigTestBuildType = DEFAULT_TEST_BUILD_TYPE;
		androidConfigTestInstrumentationRunner = null;
		
		pitestReportDir = buildDir.toPath().resolve("reportDir").toFile();
	}
	
	@After
	public void tearDown() {
		additionalMocks.clear();
		additionalMocks = null;
		
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
		androidConfigTestInstrumentationRunner = null;
		
		pitestReportDir = null;
	}
	
	@Test
	public void checkAndSetValues_constructor_notNull() {
		assertThat(unitUnderTest).isNotNull();
	}
	
	@Test
	public void checkAndSetValues_extensionNoValuesSupplied_hasDefaultValues() {
		
		String applicationId = DEFAULT_APPLICATION_ID;
		String testBuildType = DEFAULT_TEST_BUILD_TYPE;
		
		String applicationPackage = applicationId;
		String targetMutantsBlob = applicationPackage + ".*";
		
		String appApkName  = DEFAULT_PROJECT_NAME + "-" + testBuildType + ".apk";
		String testApkName = DEFAULT_PROJECT_NAME + "-" + testBuildType + "-" + "androidTest" + ".apk";
		
		run_checkAndSetValues();
		
		InstrumentationTestOptions instrumentationTestOptions = extension.getInstrumentationTestOptions();
		assertThat(instrumentationTestOptions).isNotNull();
		
		assertIntrumentationTestRunner(DEFAULT_RUNNER);
		
		Set<String> targetMutants = instrumentationTestOptions.getTargetMutants();
		assertThat(targetMutants).isNotNull().isNotEmpty().hasSize(1);
		assertThat(targetMutants).contains(targetMutantsBlob);
		
		TargetTests targetTests = instrumentationTestOptions.getTargetTests();
		assertThat(targetTests).isNotNull();
		assertThat(targetTests.getClasses()).isNull();
		assertThat(targetTests.getPackages()).isNull();
		
		assertThat(extension.getMuidProperty()).isEqualTo(DEFAULT_PROPERTY_NAME_MUID);
		
		assertThat(extension.getApplicationId()).isEqualTo(DEFAULT_APPLICATION_ID);
		assertThat(extension.getTestApplicationId()).isEqualTo(DEFAULT_TEST_APPLICATION_ID);
		assertThat(extension.getApplicationIdSuffix()).isNull();
		
		assertThat(extension.getApplicationPackage()).isEqualTo(applicationPackage);
		assertThat(extension.getPackageDir()).isEqualTo(applicationPackage.replaceAll("\\.", "/"));
		
		assertThat(extension.getApkName()).isEqualTo(appApkName);
		assertThat(extension.getTestApkName()).isEqualTo(testApkName);
		assertThat(extension.getApkAppOutputRootDir()).isEqualTo(buildDir + "/outputs/apk/" + testBuildType);
		assertThat(extension.getApkTestOutputRootDir()).isEqualTo(buildDir + "/outputs/apk/androidTest/" + testBuildType);
		
		assertThat(extension.getOutputDir()).isEqualTo(buildDir + "/mutation");
		
		assertThat(extension.getMutantResultRootDir()).isEqualTo(extension.getOutputDir() + "/mutants");
		assertThat(extension.getMutantReportRootDir()).isEqualTo(extension.getOutputDir() + "/result");
		assertThat(extension.getMutantClassesDir()).isEqualTo(pitestReportDir + "/" + testBuildType);
		
		assertThat(extension.getAppResultRootDir()).isEqualTo(extension.getOutputDir() + "/app/" + testBuildType);
		assertThat(extension.getClassFilesBackupDir()).isEqualTo(extension.getAppResultRootDir() + "/backup/classes");
		assertThat(extension.getClassFilesDir()).isEqualTo(buildDir + "/intermediates/classes/" + testBuildType);
	}
	
	@Test(expected = NullPointerException.class)
	public void checkAndSetValues_noApplicationId_applicationIdsCorrect() {
		androidConfigApplicationId = null;
		
		run_checkAndSetValues();
	}
	
	@Test
	public void checkAndSetValues_extensionApplicationIdNull_applicationIdsCorrect() {
		androidConfigApplicationId = DEFAULT_APPLICATION_ID;
		
		run_checkAndSetValues();
		
		assertThat(extension.getApplicationId()).isEqualTo(DEFAULT_APPLICATION_ID);
		assertThat(extension.getTestApplicationId()).isEqualTo(DEFAULT_TEST_APPLICATION_ID);
		assertThat(extension.getApplicationIdSuffix()).isNull();
	}
	
	@Test
	public void checkAndSetValues_extensionWithApplicationId_applicationIdsCorrect() {
		final String applicationId = "at.woodstick.junit.android";
		
		androidConfigApplicationId = applicationId;
		
		run_checkAndSetValues();
		
		assertThat(extension.getApplicationId()).isEqualTo(applicationId);
		assertThat(extension.getTestApplicationId()).isEqualTo(applicationId + ".test");
		assertThat(extension.getApplicationIdSuffix()).isNull();
	}
	
	@Test
	public void checkAndSetValues_extensionWithTestApplicationId_applicationIdsCorrect() {
		final String testApplicationId = "at.woodstick.junit.test.android";
		
		androidConfigTestApplicationId = testApplicationId;
		
		run_checkAndSetValues();
		
		assertThat(extension.getApplicationId()).isEqualTo(DEFAULT_APPLICATION_ID);
		assertThat(extension.getTestApplicationId()).isEqualTo(testApplicationId);
		assertThat(extension.getApplicationIdSuffix()).isNull();
	}
	
	@Test
	public void checkAndSetValues_extensionWithApplicationIdSuffix_applicationIdsCorrect() {
		final String applicationIdSuffix = "at.woodstick.junit.android";
		
		androidConfigApplicationIdSuffix = applicationIdSuffix;
		
		run_checkAndSetValues();
		
		assertThat(extension.getApplicationId()).isEqualTo(DEFAULT_APPLICATION_ID);
		assertThat(extension.getTestApplicationId()).isEqualTo(DEFAULT_APPLICATION_ID + ".test");
		assertThat(extension.getApplicationIdSuffix()).isEqualTo(applicationIdSuffix);
	}
	
	@Test
	public void checkAndSetValues_extensionWithApplicationIds_applicationIdsCorrect() {
		final String applicationId = "at.woodstick.junit.android";
		final String testApplicationId = "at.woodstick.junit.test.android";
		final String applicationIdSuffix = "at.woodstick.junit.android";
		
		androidConfigApplicationId = applicationId;
		androidConfigTestApplicationId = testApplicationId;
		androidConfigApplicationIdSuffix = applicationIdSuffix;
		
		run_checkAndSetValues();
		
		assertThat(extension.getApplicationId()).isEqualTo(applicationId);
		assertThat(extension.getTestApplicationId()).isEqualTo(testApplicationId);
		assertThat(extension.getApplicationIdSuffix()).isEqualTo(applicationIdSuffix);
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
	
	@Test
	public void checkAndSetValues_extensionWithFlavor_correctValues() {
		@SuppressWarnings("unchecked")
		SortedMap<String, ProductFlavor> flavorMap = (SortedMap<String, ProductFlavor>)EasyMock.mock(SortedMap.class);

		final String flavorName = "free";
		
		expect( androidConfigProductFlavors.getAsMap() ).andStubReturn(flavorMap);
		expect( flavorMap.firstKey() ).andStubReturn(flavorName);
		
		hasNoProductFlavor = false;
		additionalMocks.add(flavorMap);
		
		String appApkName  = projectName + "-" + flavorName + "-" + androidConfigTestBuildType + ".apk";
		String testApkName = projectName + "-" + flavorName + "-" + androidConfigTestBuildType + "-" + "androidTest" + ".apk";
		
		run_checkAndSetValues();
		
		assertThat(extension.getApkName()).isEqualTo(appApkName);
		assertThat(extension.getTestApkName()).isEqualTo(testApkName);
	}
	
	@Test
	public void checkAndSetValues_extensionWithFlavorAndBuildType_correctValues() {
		@SuppressWarnings("unchecked")
		SortedMap<String, ProductFlavor> flavorMap = (SortedMap<String, ProductFlavor>)EasyMock.mock(SortedMap.class);

		final String testBuildType = "release";
		final String flavorName = "free";
		
		expect( androidConfigProductFlavors.getAsMap() ).andStubReturn(flavorMap);
		expect( flavorMap.firstKey() ).andStubReturn(flavorName);
		
		hasNoProductFlavor = false;
		additionalMocks.add(flavorMap);
		
		androidConfigTestBuildType = testBuildType;
		
		String appApkName  = projectName + "-" + flavorName + "-" + testBuildType + ".apk";
		String testApkName = projectName + "-" + flavorName + "-" + testBuildType + "-" + "androidTest" + ".apk";
		
		run_checkAndSetValues();
		
		assertThat(extension.getApkName()).isEqualTo(appApkName);
		assertThat(extension.getTestApkName()).isEqualTo(testApkName);
	}
	
	// ########################################################################
	
	private void assertIntrumentationTestRunner(final String testInstrumentationRunner) {
		InstrumentationTestOptions instrumentationTestOptions = extension.getInstrumentationTestOptions();
		assertThat(instrumentationTestOptions).isNotNull();
		assertThat(instrumentationTestOptions.getRunner()).isEqualTo(testInstrumentationRunner);
	}
	
	// ########################################################################
	
	private void run_checkAndSetValues() {
		expectBuildConfiguration();
		expectProductFlavorsConfiguration();
		expectAndroidConfigValues();
		expectPitestConfigValues();
		
		List<Object> defaultMocks = java.util.Arrays.asList( androidExtension, androidDefaultConfig, pitestExtension, buildConfigContainerMock, androidConfigProductFlavors );
		getAdditionalMocks().addAll(defaultMocks);
		Object[] mocks = getAdditionalMocks().toArray();
		
		replay( mocks );
		
		unitUnderTest.checkAndSetValues();
		
		verify( mocks );
	}
	
	private List<Object> getAdditionalMocks() {
		return additionalMocks;
	}
	
	// ########################################################################
	
	private void expectBuildConfiguration() {
		expect( buildConfigContainerMock.isEmpty() ).andReturn(hasNoBuildConfiguration).anyTimes();
	}
	
	private void expectProductFlavorsConfiguration() {
		expect( androidConfigProductFlavors.isEmpty() ).andReturn(hasNoProductFlavor).anyTimes();
	}

	// ########################################################################
	
	private void expectAndroidConfigValues() {
		expect( androidExtension.getDefaultConfig() ).andReturn(androidDefaultConfig).anyTimes();
		
		expect( androidDefaultConfig.getApplicationId() ).andReturn(androidConfigApplicationId).anyTimes();
		
		expect( androidDefaultConfig.getTestApplicationId() ).andReturn(androidConfigTestApplicationId).anyTimes();
		
		expect( androidDefaultConfig.getApplicationIdSuffix() ).andReturn(androidConfigApplicationIdSuffix).anyTimes();
		
		expect( androidExtension.getTestBuildType() ).andReturn(androidConfigTestBuildType).anyTimes();
		
		expect( androidExtension.getProductFlavors() ).andReturn(androidConfigProductFlavors).anyTimes();
		
		expect( androidDefaultConfig.getTestInstrumentationRunner() ).andReturn(androidConfigTestInstrumentationRunner).anyTimes();
	}
	
	private void expectPitestConfigValues() {
		expect( pitestExtension.getReportDir() ).andReturn(pitestReportDir).anyTimes();
	}
}
