package at.woodstick.pimutdroid.internal;

import static at.woodstick.pimutdroid.test.helper.TestHelper.asList;
import static at.woodstick.pimutdroid.test.helper.TestHelper.asSet;
import static at.woodstick.pimutdroid.test.helper.TestHelper.expectedScore;
import static at.woodstick.pimutdroid.test.helper.TestHelper.mutation;
import static at.woodstick.pimutdroid.test.helper.TestHelper.newInstrumentationTestOptions;
import static at.woodstick.pimutdroid.test.helper.TestHelper.newMutantDetailResult;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import at.woodstick.pimutdroid.configuration.InstrumentationTestOptions;
import at.woodstick.pimutdroid.result.ClassOverview;
import at.woodstick.pimutdroid.result.MutantGroup;
import at.woodstick.pimutdroid.result.MutationOverview;
import at.woodstick.pimutdroid.result.MutationResult;
import at.woodstick.pimutdroid.result.PackageOverview;
import at.woodstick.pimutdroid.task.MutantDetailResult;
import at.woodstick.pimutdroid.task.MutantGroupKey;
import at.woodstick.pimutdroid.test.assertion.ClassOverviewListAssert;
import at.woodstick.pimutdroid.test.assertion.MutantGroupAssert;
import at.woodstick.pimutdroid.test.assertion.MutantGroupListAssert;
import at.woodstick.pimutdroid.test.assertion.MutationOverviewAssert;
import at.woodstick.pimutdroid.test.assertion.MutationResultAssert;
import at.woodstick.pimutdroid.test.assertion.PackageOverviewListAssert;
import at.woodstick.pimutdroid.test.assertion.TestSetupAssert;
import at.woodstick.pimutdroid.test.helper.TestMutator;

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
		
		InstrumentationTestOptions testOptions = newInstrumentationTestOptions()
				.withRunner(DEFAULT_TEST_RUNNER)
				.withTargetedMutants("package.of.mutant.*")
				.withTestPackages("package.of.mutant", "package.of.global")
				.get();
		
		MutationResultBuilder unitUnderTest = MutationResultBuilder.builder()
													.withTestOptions(testOptions)
													.withTargetedMutants(testOptions.getTargetMutants())
													;
		
		MutationResult mutationResult = unitUnderTest.build();
		
		TestSetupAssert.assertThat(mutationResult.getTestSetup())
			.hasRunner(DEFAULT_TEST_RUNNER)
			.hasTargetedMutantsInOrder("package.of.mutant.*")
			.hasTestPackagesInOrder("package.of.global", "package.of.mutant")
			.hasNoTestClasses();
	}
	
	@Test
	public void build_instrumentationTestsWithTestClasses_testSetupListContainsThem() {
		
		InstrumentationTestOptions testOptions = newInstrumentationTestOptions()
				.withRunner(DEFAULT_TEST_RUNNER)
				.withTargetedMutants("package.of.mutant.*")
				.withTestClasses("package.of.mutant", "package.of.global")
				.get();
		
		
		MutationResultBuilder unitUnderTest = MutationResultBuilder.builder()
													.withTestOptions(testOptions)
													.withTargetedMutants(testOptions.getTargetMutants())
													;
		
		MutationResult mutationResult = unitUnderTest.build();
		
		TestSetupAssert.assertThat(mutationResult.getTestSetup())
			.hasRunner(DEFAULT_TEST_RUNNER)
			.hasTargetedMutantsInOrder("package.of.mutant.*")
			.hasTestClassesInOrder("package.of.global", "package.of.mutant")
			.hasNoTestPackages();
	}
	
	@Test
	public void build_oneMutantNotKilled_resultDataCorrect() {
		
		Map<MutantGroupKey, List<MutantDetailResult>> mutantGroupMap = new HashMap<>();

		int totalMutants = 1;
		int killedMutants = 0;

		int totalMutantPackages = totalMutants;
		int totalMutantClasses  = totalMutants;
		
		String mutantPackage = "package.of.mutant";
		String mutantClass = "ClassOfMutant";
		String filename = "ClassOfMutant.java";
		
		String muid = mutantClass + "_1.muid";
		String method = "RemovedMethod";
		String lineNumber = "12";
		TestMutator mutator = TestMutator.EQUAL_IF;
		
		MutantGroupKey groupKey = MutantGroupKey.of(mutantPackage, mutantClass, filename);
		
		MutantDetailResult resultDetails = newMutantDetailResult()
				.withDetails(muid, mutantPackage, mutantClass, method, filename, lineNumber, mutator)
				.lived()
				.get();
		
		List<MutantDetailResult> detailList = asList(resultDetails);
		
		mutantGroupMap.put(groupKey, detailList);
		
		
		InstrumentationTestOptions testOptions = newInstrumentationTestOptions()
				.withRunner(DEFAULT_TEST_RUNNER)
				.withTargetedMutants("package.of.mutant.global.*")
				.withTestPackages("package.of.mutant")
				.get();
		
		
		// Build unit under test
		MutationResultBuilder unitUnderTest = MutationResultBuilder.builder()
													.withTestOptions(testOptions)
													.withTargetedMutants(asSet("package.of.mutant.*", "package.of.other.*"))
													.withResultTimestamp(RESULT_TIMESTAMP)
													.withMutantResults(mutantGroupMap)
													.withTotalMutants(totalMutants)
													.withKilledMutants(killedMutants);
		// Execute method to test
		MutationResult mutationResult = unitUnderTest.build();
		
		MutationResultAssert.assertThat(mutationResult).hasDate(RESULT_TIMESTAMP);
		
		MutationOverview overview = mutationResult.getOverview();
		
		MutationOverviewAssert.assertThat(overview)
			.hasZeroScore()
			.hasPackagOverviews(totalMutantPackages)
			.hasClassOverviews(totalMutantClasses)
			.hasKilledMutants(killedMutants)
			.hasTotalMutants(totalMutants)
			;
		
		PackageOverviewListAssert.assertThat(overview.getPackageOverview()).element(0)
			.hasMutants(1).hasKilledMutants(0).hasName("package.of.mutant").hasZeroScore();
		
		ClassOverviewListAssert.assertThat(overview.getClassOverview()).element(0)
			.hasMutants(1).hasKilledMutants(0).hasPackage("package.of.mutant").hasName("ClassOfMutant.java").hasZeroScore();
		
		TestSetupAssert.assertThat(mutationResult.getTestSetup())
			.hasRunner(DEFAULT_TEST_RUNNER)
			.hasTargetedMutantsInOrder("package.of.mutant.*", "package.of.other.*")
			.hasTestPackagesInOrder("package.of.mutant")
			.hasNoTestClasses();
		
		MutantGroupAssert groupAssert = MutantGroupListAssert.assertThat(mutationResult.getMutants()).element(0)
			.hasMutantPackage("package.of.mutant")
			.hasMutantClass("ClassOfMutant")
			.hasFile(filename)
			.hasMutants(1)
			.hasKilledMutants(0)
			;
		
		groupAssert.withMutantAt(0).isNotNull().hasId(muid).isAlive().hasMutation(mutation(method, lineNumber, mutator));
	}
	
	@Test
	public void build_twoMutantGroupsInOnePackagesWithOneClassesAndInnerClass_resultDataCorrect() {
		
		int totalMutants = 2;
		int totalKilled = 2;
		
		Map<MutantGroupKey, List<MutantDetailResult>> mutantGroupMap = new HashMap<>();
		
		// Add first mutant group
		MutantGroupKey firstGroupKey  = MutantGroupKey.of("at.package.of.mutant.first", "ClassOfMutant", "ClassOfMutant.java");
		
		MutantDetailResult firstGroupFirstMutant = newMutantDetailResult()
				.killed()
				.withDetails("ClassOfMutant_1.muid", firstGroupKey, "mutantMethod", "12", TestMutator.EQUAL_ELSE)
				.get();
		
		mutantGroupMap.put(firstGroupKey, asList(firstGroupFirstMutant));
		
		// Add second mutant group
		MutantGroupKey secondGroupKey = MutantGroupKey.of("at.package.of.mutant.first", "ClassOfMutant$1", "ClassOfMutant.java");
		
		MutantDetailResult secondGroupFirstMutant = newMutantDetailResult()
				.killed()
				.withDetails("ClassOfMutant$1_1.muid", secondGroupKey, "mutantInnerClassMethod", "122", TestMutator.EQUAL_IF)
				.get();
		
		mutantGroupMap.put(secondGroupKey, asList(secondGroupFirstMutant));
		
		// Build unit under test
		MutationResultBuilder unitUnderTest = MutationResultBuilder.builder()
													.withMutantResults(mutantGroupMap)
													.withTotalMutants(totalMutants)
													.withKilledMutants(totalKilled);
		
		// Execute method to test
		MutationResult mutationResult = unitUnderTest.build();
		
		// Assert mutation overview
		MutationOverview overview = mutationResult.getOverview();
		
		MutationOverviewAssert.assertThat(overview)
			.hasScore(expectedScore(totalMutants, totalKilled))
			.hasPackagOverviews(1)
			.hasClassOverviews(1)
			.hasKilledMutants(totalKilled)
			.hasTotalMutants(totalMutants)
			;
		
		// Assert package overview
		Collection<PackageOverview> packageOverview = overview.getPackageOverview();
		
		assertThat(packageOverview).isNotEmpty().hasSize(1);
		assertThat(packageOverview).extracting(PackageOverview::getName).containsExactly("at.package.of.mutant.first");
		
		PackageOverviewListAssert.assertThat(packageOverview).element(0)
			.hasMutants(2).hasKilledMutants(2).hasName("at.package.of.mutant.first").hasFullScore();
		
		// Assert class overview
		Collection<ClassOverview> classOverview = overview.getClassOverview();
		
		assertThat(classOverview).isNotEmpty().hasSize(1);
		assertThat(classOverview).extracting(ClassOverview::getName).containsExactly("ClassOfMutant.java");
		
		ClassOverviewListAssert.assertThat(classOverview).element(0)
			.hasName("ClassOfMutant.java").hasPackage("at.package.of.mutant.first").hasMutants(2).hasKilledMutants(2).hasFullScore();
		
		// Assert mutant groups
		Collection<MutantGroup> mutantGroupList = mutationResult.getMutants();
		
		assertThat(mutantGroupList).isNotEmpty().hasSize(2);
		
		// Assert first mutant group and mutations
		MutantGroupAssert firstGroupAssert = MutantGroupListAssert.assertThat(mutantGroupList).element(0)
			.hasMutantPackage(firstGroupKey.getMutantPackage())
			.hasMutantClass(firstGroupKey.getMutantClass())
			.hasFile(firstGroupKey.getFilename())
			.hasMutants(1)
			.hasKilledMutants(1)
			.hasFullScore()
			;
		
		firstGroupAssert.withMutantAt(0).isNotNull().hasId(firstGroupFirstMutant.getDetails().getMuid()).wasKilled().hasMutation(mutation(firstGroupFirstMutant.getDetails()));
		
		// Assert second mutant group and mutations
		MutantGroupAssert secondGroupAssert = MutantGroupListAssert.assertThat(mutantGroupList).element(1)
			.hasMutantPackage(secondGroupKey.getMutantPackage())
			.hasMutantClass(secondGroupKey.getMutantClass())
			.hasFile(secondGroupKey.getFilename())
			.hasMutants(1)
			.hasKilledMutants(1)
			.hasFullScore()
			;
		
		secondGroupAssert.withMutantAt(0).isNotNull().hasId(secondGroupFirstMutant.getDetails().getMuid()).wasKilled().hasMutation(mutation(secondGroupFirstMutant.getDetails()));
	}
	
	@Test
	public void build_fourMutantGroupsInThreePackagesWithFourClasses_resultDataCorrect() {
		
		int totalMutants = 0;
		int totalKilled = 0;
		
		int expectedNumberPackages = 3;
		int expectedNumberClasses  = 4;
		
		Map<MutantGroupKey, List<MutantDetailResult>> mutantGroupMap = new HashMap<>();

		// Add first mutant group
		MutantGroupKey firstGroupKey  = MutantGroupKey.of("at.package.of.mutant.first", "ClassOfMutant", "ClassOfMutant.java");
		
		MutantDetailResult firstGroupFirstMutant = newMutantDetailResult()
				.lived()
				.withDetails("ClassOfMutant_1.muid", firstGroupKey, "mutantMethod", "12", TestMutator.EQUAL_ELSE)
				.get();
		
		MutantDetailResult firstGroupSecondMutant = newMutantDetailResult()
				.killed()
				.withDetails("ClassOfMutant_2.muid", firstGroupKey, "anotherMutantMethod", "24", TestMutator.VOID_METHOD)
				.get();
		
		totalMutants += 2;
		totalKilled++;
		
		mutantGroupMap.put(firstGroupKey, asList(firstGroupFirstMutant, firstGroupSecondMutant));
		
		// Add second mutant group
		MutantGroupKey secondGroupKey = MutantGroupKey.of("at.package.of.mutant.first.second", "SubClassOfMutant", "SubClassOfMutant.java");
		
		MutantDetailResult secondGroupFirstMutant = newMutantDetailResult()
				.killed()
				.withDetails("SubClassOfMutant_1.muid", secondGroupKey, "mutantSubMethod", "102", TestMutator.EQUAL_IF)
				.get();
		
		totalMutants++;
		totalKilled++;
		
		mutantGroupMap.put(secondGroupKey, asList(secondGroupFirstMutant));
		
		// Add third mutant group
		MutantGroupKey thirdGroupKey  = MutantGroupKey.of("at.package.of.mutant.second", "ClassOfAMutant", "ClassOfAMutant.java");
		
		MutantDetailResult thirdGroupFirstMutant = newMutantDetailResult()
				.lived()
				.withDetails("ClassOfAMutant_1.muid", thirdGroupKey, "mutantMethod", "12", TestMutator.EQUAL_IF)
				.get();
		
		MutantDetailResult thirdGroupSecondMutant = newMutantDetailResult()
				.killed()
				.withDetails("ClassOfAMutant_3.muid", thirdGroupKey, "mutantMethod", "12", TestMutator.VOID_METHOD)
				.get();
		
		MutantDetailResult thirdGroupThirdMutant = newMutantDetailResult()
				.noResult()
				.withDetails("ClassOfAMutant_2.muid", thirdGroupKey, "mutantMethod", "12", TestMutator.EQUAL_ELSE)
				.get();
		
		totalMutants += 3;
		totalKilled++;
		
		mutantGroupMap.put(thirdGroupKey, asList(thirdGroupFirstMutant, thirdGroupSecondMutant, thirdGroupThirdMutant));
		
		// Add fourth mutant group
		MutantGroupKey fourthGroupKey  = MutantGroupKey.of("at.package.of.mutant.first", "AbstractClassOfMutant", "AbstractClassOfMutant.java");
		
		MutantDetailResult fourthGroupFirstMutant = newMutantDetailResult()
				.lived()
				.withDetails("AbstractClassOfMutant_1.muid", fourthGroupKey, "exec", "12", TestMutator.EQUAL_ELSE)
				.get();
		
		MutantDetailResult fourthGroupSecondMutant = newMutantDetailResult()
				.lived()
				.withDetails("AbstractClassOfMutant_2.muid", fourthGroupKey, "exec", "12", TestMutator.VOID_METHOD)
				.get();
		
		totalMutants += 2;
		
		mutantGroupMap.put(fourthGroupKey, asList(fourthGroupFirstMutant, fourthGroupSecondMutant));
		
		InstrumentationTestOptions testOptions = newInstrumentationTestOptions()
				.withRunner(DEFAULT_TEST_RUNNER)
				.withTargetedMutants("at.package.of.mutant.*")
				.withTestPackages("at.package.of.mutant.test")
				.get();

		
		// Build unit under test
		MutationResultBuilder unitUnderTest = MutationResultBuilder.builder()
													.withTestOptions(testOptions)
													.withTargetedMutants(asSet("at.package.of.mutant.second.*", "at.package.of.mutant.first.*"))
													.withResultTimestamp(RESULT_TIMESTAMP)
													.withMutantResults(mutantGroupMap)
													.withTotalMutants(totalMutants)
													.withKilledMutants(totalKilled);
		
		// Execute method to test
		MutationResult mutationResult = unitUnderTest.build();
		
		MutationResultAssert.assertThat(mutationResult).hasDate(RESULT_TIMESTAMP);
		
		// Assert mutation overview
		MutationOverview overview = mutationResult.getOverview();
		
		MutationOverviewAssert.assertThat(overview)
			.hasScore(expectedScore(totalMutants, totalKilled))
			.hasPackagOverviews(expectedNumberPackages)
			.hasClassOverviews(expectedNumberClasses)
			.hasKilledMutants(totalKilled)
			.hasTotalMutants(totalMutants)
			;
		
		// Assert package overview
		Collection<PackageOverview> packageOverview = overview.getPackageOverview();
		
		assertThat(packageOverview).isNotEmpty().hasSize(3);
		assertThat(packageOverview).extracting(PackageOverview::getName)
			.containsExactly("at.package.of.mutant.first", "at.package.of.mutant.first.second", "at.package.of.mutant.second");
		
		PackageOverviewListAssert.assertThat(packageOverview).element(0)
			.hasMutants(4).hasKilledMutants(1).hasName("at.package.of.mutant.first").hasScore(expectedScore(4, 1));
		
		PackageOverviewListAssert.assertThat(packageOverview).element(1)
			.hasMutants(1).hasKilledMutants(1).hasName("at.package.of.mutant.first.second").hasFullScore();
		
		PackageOverviewListAssert.assertThat(packageOverview).element(2)
			.hasMutants(3).hasKilledMutants(1).hasName("at.package.of.mutant.second").hasScore(expectedScore(3, 1));
		
		
		// Assert class overview
		Collection<ClassOverview> classOverview = overview.getClassOverview();
		
		assertThat(classOverview).isNotEmpty().hasSize(4);
		assertThat(classOverview).extracting(ClassOverview::getName)
			.containsExactly("AbstractClassOfMutant.java", "ClassOfMutant.java", "SubClassOfMutant.java", "ClassOfAMutant.java");
		
		ClassOverviewListAssert.assertThat(classOverview).element(0)
			.hasName("AbstractClassOfMutant.java").hasPackage("at.package.of.mutant.first").hasMutants(2).hasKilledMutants(0).hasZeroScore();
		
		ClassOverviewListAssert.assertThat(classOverview).element(1)
			.hasName("ClassOfMutant.java").hasPackage("at.package.of.mutant.first").hasMutants(2).hasKilledMutants(1).hasScore(expectedScore(2, 1));
		
		ClassOverviewListAssert.assertThat(classOverview).element(2)
			.hasName("SubClassOfMutant.java").hasPackage("at.package.of.mutant.first.second").hasMutants(1).hasKilledMutants(1).hasFullScore();
		
		ClassOverviewListAssert.assertThat(classOverview).element(3)
			.hasName("ClassOfAMutant.java").hasPackage("at.package.of.mutant.second").hasMutants(3).hasKilledMutants(1).hasScore(expectedScore(3, 1));
		
		TestSetupAssert.assertThat(mutationResult.getTestSetup())
			.hasRunner(DEFAULT_TEST_RUNNER)
			.hasTargetedMutantsInOrder("at.package.of.mutant.first.*", "at.package.of.mutant.second.*")
			.hasTestPackagesInOrder("at.package.of.mutant.test")
			.hasNoTestClasses();
		
		// Assert mutant groups
		Collection<MutantGroup> mutantGroupList = mutationResult.getMutants();
		
		assertThat(mutantGroupList).isNotEmpty().hasSize(4);
		
		// Assert first mutant group and mutations
		MutantGroupAssert firstGroupAssert = MutantGroupListAssert.assertThat(mutantGroupList).element(0)
			.hasMutantPackage(fourthGroupKey.getMutantPackage())
			.hasMutantClass(fourthGroupKey.getMutantClass())
			.hasFile(fourthGroupKey.getFilename())
			.hasMutants(2)
			.hasKilledMutants(0)
			.hasZeroScore()
			;
		
		firstGroupAssert.withMutantAt(0).isNotNull().hasId(fourthGroupFirstMutant.getDetails().getMuid()).isAlive().hasMutation(mutation(fourthGroupFirstMutant.getDetails()));
		firstGroupAssert.withMutantAt(1).isNotNull().hasId(fourthGroupSecondMutant.getDetails().getMuid()).isAlive().hasMutation(mutation(fourthGroupSecondMutant.getDetails()));
		
		// Assert second mutant group and mutations
		MutantGroupAssert secondGroupAssert = MutantGroupListAssert.assertThat(mutantGroupList).element(1)
			.hasMutantPackage(firstGroupKey.getMutantPackage())
			.hasMutantClass(firstGroupKey.getMutantClass())
			.hasFile(firstGroupKey.getFilename())
			.hasMutants(2)
			.hasKilledMutants(1)
			.hasScore(expectedScore(2, 1))
			;
		
		secondGroupAssert.withMutantAt(0).isNotNull().hasId(firstGroupFirstMutant.getDetails().getMuid()).isAlive().hasMutation(mutation(firstGroupFirstMutant.getDetails()));
		secondGroupAssert.withMutantAt(1).isNotNull().hasId(firstGroupSecondMutant.getDetails().getMuid()).wasKilled().hasMutation(mutation(firstGroupSecondMutant.getDetails()));
		
		// Assert third mutant group and mutations
		MutantGroupAssert thirdGroupAssert = MutantGroupListAssert.assertThat(mutantGroupList).element(2)
			.hasMutantPackage(secondGroupKey.getMutantPackage())
			.hasMutantClass(secondGroupKey.getMutantClass())
			.hasFile(secondGroupKey.getFilename())
			.hasMutants(1)
			.hasKilledMutants(1)
			.hasFullScore()
			;
		
		thirdGroupAssert.withMutantAt(0).isNotNull().hasId(secondGroupFirstMutant.getDetails().getMuid()).wasKilled().hasMutation(mutation(secondGroupFirstMutant.getDetails()));
		
		// Assert fourth mutant group and mutations
		MutantGroupAssert fourthGroupAssert = MutantGroupListAssert.assertThat(mutantGroupList).element(3)
			.hasMutantPackage(thirdGroupKey.getMutantPackage())
			.hasMutantClass(thirdGroupKey.getMutantClass())
			.hasFile(thirdGroupKey.getFilename())
			.hasMutants(3)
			.hasKilledMutants(1)
			.hasScore(expectedScore(3, 1))
			;
		
		fourthGroupAssert.withMutantAt(0).isNotNull().hasId(thirdGroupFirstMutant.getDetails().getMuid()).isAlive().hasMutation(mutation(thirdGroupFirstMutant.getDetails()));
		fourthGroupAssert.withMutantAt(1).isNotNull().hasId(thirdGroupSecondMutant.getDetails().getMuid()).wasKilled().hasMutation(mutation(thirdGroupSecondMutant.getDetails()));
		fourthGroupAssert.withMutantAt(2).isNotNull().hasId(thirdGroupThirdMutant.getDetails().getMuid()).wasNoResult().hasMutation(mutation(thirdGroupThirdMutant.getDetails()));
	}
}
