package at.woodstick.pimutdroid.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
	private File reportsDir;
	
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
	private String androidConfigTestBuildType;
	
	@Mock
	private NamedDomainObjectContainer<ProductFlavor> androidConfigProductFlavors;
	boolean hasNoProductFlavor = true;
	
	private String androidConfigTestInstrumentationRunner;

	private File pitestReportDir;
	
	private List<Object> additionalMocks;
	
	private Collection<AndroidVariant> androidVariants;
	
	@Mock
	private AndroidVariant androidVariantMock;
	
	@Before
	public void setUp() throws IOException {
		additionalMocks = new ArrayList<Object>();
		
		extension = new PimutdroidPluginExtension(buildConfigContainerMock);
		
		projectName = DEFAULT_PROJECT_NAME;
		buildDir = tempFolder.newFolder();
		reportsDir = buildDir.toPath().resolve("reports").toFile();
		
		hasNoBuildConfiguration = true;
		hasNoProductFlavor = true;
		
		androidVariants = Arrays.asList(androidVariantMock);
		
		unitUnderTest = new DefaultExtensionValuesCheck(projectName, buildDir, reportsDir, extension, androidExtension, pitestExtension, androidVariants);
		
		setExtensionDefaultValues();
	}
	
	protected void setExtensionDefaultValues() {
		androidConfigApplicationId = DEFAULT_APPLICATION_ID;
		androidConfigTestApplicationId = null;
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
		
		expectAndroidVariant(androidConfigTestBuildType, androidConfigApplicationId);
		
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
		
		assertThat(extension.getApplicationPackage()).isEqualTo(applicationPackage);
		
		assertThat(extension.getApkName()).isEqualTo(appApkName);
		assertThat(extension.getTestApkName()).isEqualTo(testApkName);
		assertThat(extension.getApkAppOutputRootDir()).isEqualTo(buildDir + "/outputs/apk/" + testBuildType);
		assertThat(extension.getApkTestOutputRootDir()).isEqualTo(buildDir + "/outputs/apk/androidTest/" + testBuildType);
		
		assertThat(extension.getOutputDir()).isEqualTo(buildDir + "/mutation");
		
		assertThat(extension.getMutantResultRootDir()).isEqualTo(extension.getOutputDir() + "/mutants");
		assertThat(extension.getMutantReportRootDir()).isEqualTo(reportsDir + "/mutation");
		assertThat(extension.getMutantClassesDir()).isEqualTo(pitestReportDir + "/" + testBuildType);
		
		assertThat(extension.getAppResultRootDir()).isEqualTo(extension.getOutputDir() + "/app/" + testBuildType);
		assertThat(extension.getClassFilesBackupDir()).isEqualTo(extension.getAppResultRootDir() + "/backup/classes");
		assertThat(extension.getClassFilesDir()).isEqualTo(buildDir + "/intermediates/classes/" + testBuildType);
		
		assertThat(extension.getExpectedTestResultFilename()).isEqualTo(projectName.toLowerCase() + "-expected-test-result.xml");
		assertThat(extension.getMutantTestResultFilename()).isEqualTo(projectName.toLowerCase() + "-mutant-test-result.xml");
		assertThat(extension.getIgnoreKilledByUnitTest()).isFalse();
		assertThat(extension.getForcePitestVersion()).isTrue();
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void checkAndSetValues_noApplicationId_applicationIdsCorrect() {
		androidConfigApplicationId = null;
		
		expectAndroidVariant(androidConfigTestBuildType, androidConfigApplicationId);
		
		run_checkAndSetValues();
	}
	
	@Test
	public void checkAndSetValues_defaultApplicationIdAndVariant_defaultApplicationIdAsApplicationPackage() {
		androidConfigApplicationId = DEFAULT_APPLICATION_ID;
		final String variantApplicationId = "at.woodstick.junit.variant.id";
		final String variantTestApplicationId = variantApplicationId + ".test";
		
		expectAndroidVariant(androidConfigTestBuildType, variantApplicationId);
		
		run_checkAndSetValues();
		
		assertThat(extension.getApplicationId()).isEqualTo(variantApplicationId);
		assertThat(extension.getTestApplicationId()).isEqualTo(variantTestApplicationId);
		assertThat(extension.getApplicationPackage()).isEqualTo(androidConfigApplicationId);
	}
	
	@Test
	public void checkAndSetValues_noDefaultApplicationIdAndVariant_variantApplicationIdAsApplicationPackage() {
		androidConfigApplicationId = null;
		final String variantApplicationId = "at.woodstick.junit.variant.id";
		final String variantTestApplicationId = variantApplicationId + ".test";
		
		expectAndroidVariant(androidConfigTestBuildType, variantApplicationId);
		
		run_checkAndSetValues();
		
		assertThat(extension.getApplicationId()).isEqualTo(variantApplicationId);
		assertThat(extension.getTestApplicationId()).isEqualTo(variantTestApplicationId);
		assertThat(extension.getApplicationPackage()).isEqualTo(variantApplicationId);
	}
	
	@Test
	public void checkAndSetValues_extensionApplicationIdNull_applicationIdsCorrect() {
		androidConfigApplicationId = DEFAULT_APPLICATION_ID;
		
		expectAndroidVariant(androidConfigTestBuildType, androidConfigApplicationId);
		
		run_checkAndSetValues();
		
		assertThat(extension.getApplicationId()).isEqualTo(DEFAULT_APPLICATION_ID);
		assertThat(extension.getTestApplicationId()).isEqualTo(DEFAULT_TEST_APPLICATION_ID);
	}
	
	@Test
	public void checkAndSetValues_extensionWithApplicationId_applicationIdsCorrect() {
		final String applicationId = "at.woodstick.junit.android";
		
		androidConfigApplicationId = applicationId;
		
		expectAndroidVariant(androidConfigTestBuildType, androidConfigApplicationId);
		
		run_checkAndSetValues();
		
		assertThat(extension.getApplicationId()).isEqualTo(applicationId);
		assertThat(extension.getTestApplicationId()).isEqualTo(applicationId + ".test");
	}
	
	@Test
	public void checkAndSetValues_extensionWithTestApplicationId_applicationIdsCorrect() {
		final String testApplicationId = "at.woodstick.junit.test.android";
		
		androidConfigTestApplicationId = testApplicationId;
		
		expectAndroidVariant(androidConfigTestBuildType, androidConfigApplicationId);
		
		run_checkAndSetValues();
		
		assertThat(extension.getApplicationId()).isEqualTo(DEFAULT_APPLICATION_ID);
		assertThat(extension.getTestApplicationId()).isEqualTo(testApplicationId);
	}
	
	@Test
	public void checkAndSetValues_extensionWithApplicationIds_applicationIdsCorrect() {
		final String applicationId = "at.woodstick.junit.android";
		final String testApplicationId = "at.woodstick.junit.test.android";
		
		androidConfigApplicationId = applicationId;
		androidConfigTestApplicationId = testApplicationId;
		
		expectAndroidVariant(androidConfigTestBuildType, androidConfigApplicationId);
		
		run_checkAndSetValues();
		
		assertThat(extension.getApplicationId()).isEqualTo(applicationId);
		assertThat(extension.getTestApplicationId()).isEqualTo(testApplicationId);
	}
	
	@Test
	public void checkAndSetValues_extensionRunnerSetInAndroidExtension_notSetInPluginExtension_runnerIsSetToAndroidConfig() {
		
		String testInstrumentationRunner = "org.catrobat.paintroid.test.utils.CustomAndroidJUnitRunner";
		
		androidConfigTestInstrumentationRunner = testInstrumentationRunner;

		expectAndroidVariant(androidConfigTestBuildType, androidConfigApplicationId);
		
		run_checkAndSetValues();
		
		assertIntrumentationTestRunner(testInstrumentationRunner);
	}
	
	@Test
	public void checkAndSetValues_extensionRunnerSupplied_notSetInAndroidExtension_runnerIsSetToPluginExtension() {
		
		String testInstrumentationRunner = "org.catrobat.paintroid.test.utils.CustomAndroidJUnitRunner";
		
		extension.getInstrumentationTestOptions().setRunner(testInstrumentationRunner);
		
		expectAndroidVariant(androidConfigTestBuildType, androidConfigApplicationId);
		
		run_checkAndSetValues();
		
		assertIntrumentationTestRunner(testInstrumentationRunner);
	}
	
	@Test
	public void checkAndSetValues_extensionRunnerSupplied_alsoSetInAndroidExtension_runnerIsSetToPluginExtension() {
		
		String testInstrumentationRunner = "org.catrobat.paintroid.test.utils.CustomAndroidJUnitRunner";

		androidConfigTestInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner";

		expectAndroidVariant(androidConfigTestBuildType, androidConfigApplicationId);
		
		extension.getInstrumentationTestOptions().setRunner(testInstrumentationRunner);
		
		run_checkAndSetValues();
		
		assertIntrumentationTestRunner(testInstrumentationRunner);
	}
	
	@Test
	public void checkAndSetValues_extensionWithFlavor_correctValues() {
		@SuppressWarnings("unchecked")
		SortedMap<String, ProductFlavor> flavorMap = (SortedMap<String, ProductFlavor>)EasyMock.mock(SortedMap.class);

		final String flavorName = "free";
		
		expectAndroidVariant(flavorName, androidConfigTestBuildType, androidConfigApplicationId);
		
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
		
		expectAndroidVariant(flavorName, testBuildType, androidConfigApplicationId);
		
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
	
	@Test
	public void checkAndSetValues_extensionSetIgnoreKilledByUniTestToTrue_isTrue() {
		
		expectAndroidVariant(androidConfigTestBuildType, androidConfigApplicationId);
		
		extension.setIgnoreKilledByUnitTest(true);
		
		run_checkAndSetValues();
		
		assertThat(extension.getIgnoreKilledByUnitTest()).isTrue();
	}
	
	@Test
	public void checkAndSetValues_extensionSetIgnoreKilledByUniTestToFalse_isFalse() {
		
		expectAndroidVariant(androidConfigTestBuildType, androidConfigApplicationId);
		
		extension.setIgnoreKilledByUnitTest(false);
		
		run_checkAndSetValues();
		
		assertThat(extension.getIgnoreKilledByUnitTest()).isFalse();
	}
	
	@Test
	public void checkAndSetValues_extensionSetForcePitestVersionToTrue_isTrue() {
		
		expectAndroidVariant(androidConfigTestBuildType, androidConfigApplicationId);
		
		extension.setForcePitestVersion(true);
		
		run_checkAndSetValues();
		
		assertThat(extension.getForcePitestVersion()).isTrue();
	}
	
	@Test
	public void checkAndSetValues_extensionSetForcePitestVersionToFalse_isFalse() {
		
		expectAndroidVariant(androidConfigTestBuildType, androidConfigApplicationId);
		
		extension.setForcePitestVersion(false);
		
		run_checkAndSetValues();
		
		assertThat(extension.getForcePitestVersion()).isFalse();
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
		
		List<Object> defaultMocks = java.util.Arrays.asList( androidVariantMock, androidExtension, androidDefaultConfig, pitestExtension, buildConfigContainerMock, androidConfigProductFlavors );
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
	
	private void expectAndroidVariant(String buildType, String applicationId) {
		expect( androidVariantMock.getBuildTypeName() ).andStubReturn(buildType);
		expect( androidVariantMock.getDirName() ).andStubReturn(buildType);
		expect( androidVariantMock.getApplicationId() ).andStubReturn(applicationId);
	}
	
	private void expectAndroidVariant(String flavorName, String buildType, String applicationId) {
		expect( androidVariantMock.getFlavorName() ).andStubReturn(flavorName);
		expect( androidVariantMock.getBuildTypeName() ).andStubReturn(buildType);
		expect( androidVariantMock.getDirName() ).andStubReturn(flavorName + "/" + buildType);
		expect( androidVariantMock.getApplicationId() ).andStubReturn(applicationId);
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
		
		expect( androidExtension.getTestBuildType() ).andReturn(androidConfigTestBuildType).anyTimes();
		
		expect( androidExtension.getProductFlavors() ).andReturn(androidConfigProductFlavors).anyTimes();
		
		expect( androidDefaultConfig.getTestInstrumentationRunner() ).andReturn(androidConfigTestInstrumentationRunner).anyTimes();
	}
	
	private void expectPitestConfigValues() {
		expect( pitestExtension.getReportDir() ).andReturn(pitestReportDir).anyTimes();
	}
}
