package at.woodstick.pimutdroid.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import at.woodstick.pimutdroid.configuration.InstrumentationTestOptions;
import at.woodstick.pimutdroid.result.MutantGroup;
import at.woodstick.pimutdroid.result.Mutation;
import at.woodstick.pimutdroid.result.MutationOverview;
import at.woodstick.pimutdroid.result.MutationResult;
import at.woodstick.pimutdroid.task.MutantDetailResult;
import at.woodstick.pimutdroid.task.MutantGroupKey;
import at.woodstick.pimutdroid.test.assertion.ClassOverviewListAssert;
import at.woodstick.pimutdroid.test.assertion.MutantGroupAssert;
import at.woodstick.pimutdroid.test.assertion.MutantGroupListAssert;
import at.woodstick.pimutdroid.test.assertion.MutantListAssert;
import at.woodstick.pimutdroid.test.assertion.MutationOverviewAssert;
import at.woodstick.pimutdroid.test.assertion.MutationResultAssert;
import at.woodstick.pimutdroid.test.assertion.PackageOverviewListAssert;
import at.woodstick.pimutdroid.test.assertion.TestSetupAssert;

public class MutationResultBuilderTest {

	private static final String DEFAULT_TEST_RUNNER = RandomStringUtils.randomAlphabetic(20);
	private static final String RESULT_TIMESTAMP = RandomStringUtils.randomAlphabetic(20);
	
	// ########################################################################
	
	@Test
	public void constructor_noError() {
		MutationResultBuilder unitUnderTest = MutationResultBuilder.builder();
		assertThat(unitUnderTest).isNotNull();
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void build_withNegativeNumberOfMutants_throwsIllegalArgument() {
		MutationResultBuilder unitUnderTest = MutationResultBuilder.builder().withTotalMutants(-1);
		unitUnderTest.build();
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void build_withNegativeNumberOfMutantKilled_throwsIllegalArgument() {
		MutationResultBuilder unitUnderTest = MutationResultBuilder.builder().withKilledMutants(-1);
		unitUnderTest.build();
	}
	
	@Test
	public void build_builderWithDefaultValues_noErrorAndValuesSetToDefaults() {
		MutationResultBuilder unitUnderTest = MutationResultBuilder.builder();
		MutationResult mutationResult = unitUnderTest.build();
		
		MutationResultAssert.assertThat(mutationResult).hasNoDate();
		MutationOverviewAssert.assertThat(mutationResult.getOverview()).isDefault();
		TestSetupAssert.assertThat(mutationResult.getTestSetup()).isDefault();
		
		Collection<MutantGroup> mutantCollection = mutationResult.getMutants();
		assertThat(mutantCollection).isEmpty();
	}
	
	@Test
	public void build_instrumentationTestsWithTestPackages_testSetupListContainsThem() {
		
		InstrumentationTestOptions testOptions = new InstrumentationTestOptions();
		testOptions.setRunner(DEFAULT_TEST_RUNNER);
		testOptions.setTargetMutants(getTargetedMutants("package.of.mutant.*"));
		testOptions.getTargetTests().setPackages(getSet("package.of.mutant", "package.of.global"));
		
		
		// Build unit under test
		MutationResultBuilder unitUnderTest = MutationResultBuilder.builder()
													.withTestOptions(testOptions)
													.withTargetedMutants(testOptions.getTargetMutants())
													;
		// Execute method to test
		MutationResult mutationResult = unitUnderTest.build();
		
		MutationResultAssert.assertThat(mutationResult).hasNoDate();
		
		TestSetupAssert.assertThat(mutationResult.getTestSetup())
			.hasRunner(DEFAULT_TEST_RUNNER)
			.hasTargetedMutantsInOrder("package.of.mutant.*")
			.hasTestPackagesInOrder("package.of.global", "package.of.mutant")
			.hasNoTestClasses();
	}
	
	@Test
	public void build_instrumentationTestsWithTestClasses_testSetupListContainsThem() {
		
		InstrumentationTestOptions testOptions = new InstrumentationTestOptions();
		testOptions.setRunner(DEFAULT_TEST_RUNNER);
		testOptions.setTargetMutants(getTargetedMutants("package.of.mutant.*"));
		testOptions.getTargetTests().setClasses(getSet("package.of.mutant", "package.of.global"));
		
		
		// Build unit under test
		MutationResultBuilder unitUnderTest = MutationResultBuilder.builder()
													.withTestOptions(testOptions)
													.withTargetedMutants(testOptions.getTargetMutants())
													;
		// Execute method to test
		MutationResult mutationResult = unitUnderTest.build();
		
		MutationResultAssert.assertThat(mutationResult).hasNoDate();
		
		TestSetupAssert.assertThat(mutationResult.getTestSetup())
			.hasRunner(DEFAULT_TEST_RUNNER)
			.hasTargetedMutantsInOrder("package.of.mutant.*")
			.hasTestClassesInOrder("package.of.global", "package.of.mutant")
			.hasNoTestPackages();
	}
	
	@Test
	public void build_TODO_TODO() {
		
		Map<MutantGroupKey, List<MutantDetailResult>> mutantGroupMap = new HashMap<>();

		int totalMutants = 1;
		int killedMutants = 0;
		
		String mutantPackage = "package.of.mutant";
		String mutantClass = "ClassOfMutant";
		String filename = "ClassOfMutant.java";
		String mutator = "VOID_METHOD";
		String muid = mutantClass + "_1.muid";
		String method = "RemovedMethod";
		String description = "Remove void method call";
		String lineNumber = "12";
		
		MutantGroupKey groupKey = MutantGroupKey.of(mutantPackage, mutantClass, filename);
		
		MutantDetails details = mutantDetails(muid, mutantPackage, mutantClass, method, mutator, filename, lineNumber, description);
		MutantDetailResult resultDetails = MutantDetailResult.lived(details);
		
		List<MutantDetailResult> detailList = detailList(resultDetails);
		
		mutantGroupMap.put(groupKey, detailList);
		
		
		InstrumentationTestOptions testOptions = new InstrumentationTestOptions();
		testOptions.setRunner(DEFAULT_TEST_RUNNER);
		testOptions.setTargetMutants(getTargetedMutants("package.of.mutant.global.*"));
		testOptions.getTargetTests().setPackages(getSet("package.of.mutant"));
		
		
		// Build unit under test
		MutationResultBuilder unitUnderTest = MutationResultBuilder.builder()
													.withTestOptions(testOptions)
													.withTargetedMutants(getTargetedMutants("package.of.mutant.*", "package.of.other.*"))
													.withResultTimestamp(RESULT_TIMESTAMP)
													.withMutantResults(mutantGroupMap)
													.withTotalMutants(totalMutants)
													.withKilledMutants(killedMutants);
		// Execute method to test
		MutationResult mutationResult = unitUnderTest.build();
		
		MutationResultAssert.assertThat(mutationResult).hasDate(RESULT_TIMESTAMP);
		
		MutationOverview overview = mutationResult.getOverview();
		
		MutationOverviewAssert.assertThat(overview)
			.hasZeroScore().hasPackagOverviews(1).hasClassOverviews(1).hasKilledMutants(0).hasTotalMutants(1);
		
		PackageOverviewListAssert.assertThat(overview.getPackageOverview()).element(0)
			.hasKilledMutants(0).hasMutants(1).hasName("package.of.mutant").hasZeroScore();
		
		ClassOverviewListAssert.assertThat(overview.getClassOverview()).element(0)
			.hasKilledMutants(0).hasMutants(1).hasPackage("package.of.mutant").hasName("ClassOfMutant.java").hasZeroScore();
		
		TestSetupAssert.assertThat(mutationResult.getTestSetup())
			.hasRunner(DEFAULT_TEST_RUNNER)
			.hasTargetedMutantsInOrder("package.of.mutant.*", "package.of.other.*")
			.hasTestPackagesInOrder("package.of.mutant")
			.hasNoTestClasses();
		
		Collection<MutantGroup> mutantCollection = mutationResult.getMutants();
		
		MutantGroupAssert groupAssert = MutantGroupListAssert.assertThat(mutantCollection).element(0)
			.hasMutantPackage("package.of.mutant")
			.hasMutantClass("ClassOfMutant")
			.hasFile(filename)
			.hasKilledMutants(0)
			.hasMutants(1);
		
		groupAssert.withMutantAt(0).isNotNull().hasId(muid).isAlive().hasMutation(new Mutation(method, lineNumber, mutator, description));
	}
	
	// ########################################################################
	
	protected List<MutantDetailResult> detailList(MutantDetailResult...results) {
		return Arrays.asList(results);
	}
	
	// ########################################################################
	
	protected MutantListAssert assertMutantListOfGroup(Collection<MutantGroup> mutantGroupCollection, int groupIndex) {
		MutantGroup mutantGroup = (new ArrayList<>(mutantGroupCollection)).get(groupIndex);
		return MutantListAssert.assertThat(mutantGroup.getMutantList());
	}
	
	// ########################################################################
	
	protected MutantDetails mutantDetails() {
		return new MutantDetails();
	}
	
	protected MutantDetails mutantDetails(String clazzPackage, String clazzName, String filename) {
		return mutantDetails(null, clazzPackage, clazzName, null, null, null, filename, null, null);
	}
	
	protected MutantDetails mutantDetails(String muid, String clazzPackage, String clazzName, String method, String mutator, String filename, String lineNumber, String description) {
		return mutantDetails(muid, clazzPackage, clazzName, clazzPackage + "." + clazzName, method, mutator, filename, lineNumber, description);
	}
	
	protected MutantDetails mutantDetails(String muid, String clazzPackage, String clazzName, String clazz, String method, String mutator, String filename, String lineNumber, String description) {
		MutantDetails details = mutantDetails();
		
		details.setMuid(muid);
		details.setClazzName(clazzName);
		details.setClazzPackage(clazzPackage);
		details.setClazz(clazz);
		details.setMethod(method);
		details.setMutator(mutator);
		details.setFilename(filename);
		details.setLineNumber(lineNumber);
		details.setDescription(description);
		
		return details;
	}
	
	// ########################################################################
	
	private Set<String> getTargetedMutants(String...mutants) {
		return getSet(mutants);
	}
	
	private Set<String> getSet(String...values) {
		return new HashSet<>(Arrays.asList(values));
	}
	
	private InstrumentationTestOptions getEmptyTestOptions() {
		return new InstrumentationTestOptions();
	}
	
	private MutationResultBuilder newUnitUnderTest(InstrumentationTestOptions testOptions, Set<String> targetedMutants, Map<MutantGroupKey, List<MutantDetailResult>> mutantGroupMap,
			int totalMutants, int totalKilled) {
		return newUnitUnderTest(testOptions, targetedMutants, DEFAULT_TEST_RUNNER, mutantGroupMap, totalMutants, totalKilled);
	}
	
	private MutationResultBuilder newUnitUnderTest(InstrumentationTestOptions testOptions, Set<String> targetedMutants,
			String resultTimeStampString, Map<MutantGroupKey, List<MutantDetailResult>> mutantGroupMap,
			int totalMutants, int totalKilled) {
		return new MutationResultBuilder(testOptions, targetedMutants, resultTimeStampString, mutantGroupMap, totalMutants, totalKilled);
	}
}
