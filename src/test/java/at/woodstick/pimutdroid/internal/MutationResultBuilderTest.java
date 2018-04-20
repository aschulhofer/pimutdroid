package at.woodstick.pimutdroid.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import at.woodstick.pimutdroid.configuration.InstrumentationTestOptions;
import at.woodstick.pimutdroid.result.Mutant;
import at.woodstick.pimutdroid.result.MutantGroup;
import at.woodstick.pimutdroid.result.Mutation;
import at.woodstick.pimutdroid.result.MutationOverview;
import at.woodstick.pimutdroid.result.MutationResult;
import at.woodstick.pimutdroid.result.TestSetup;
import at.woodstick.pimutdroid.task.MutantDetailResult;
import at.woodstick.pimutdroid.task.MutantGroupKey;
import at.woodstick.pimutdroid.test.assertion.MutantAssert;
import at.woodstick.pimutdroid.test.assertion.MutantGroupAssert;
import at.woodstick.pimutdroid.test.assertion.MutantGroupListAssert;
import at.woodstick.pimutdroid.test.assertion.MutantListAssert;

public class MutationResultBuilderTest {

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
		
		assertThat(mutationResult).isNotNull();
		
		assertThat(mutationResult.getDate()).isEmpty();
		
		MutationOverview overview = mutationResult.getOverview();
		assertThat(overview).isNotNull();
		assertThat(overview.getMutationScore()).isZero();
		assertThat(overview.getNumberOfMutants()).isZero();
		assertThat(overview.getNumberOfMutantsKilled()).isZero();
		assertThat(overview.getPackageOverview()).isEmpty();
		assertThat(overview.getClassOverview()).isEmpty();
		
		TestSetup testSetup = mutationResult.getTestSetup();
		assertThat(testSetup).isNotNull();
		assertThat(testSetup.getClasses()).isEmpty();
		assertThat(testSetup.getPackages()).isEmpty();
		assertThat(testSetup.getRunner()).isNull();
		assertThat(testSetup.getTargetedMutants()).isEmpty();
		
		Collection<MutantGroup> mutantCollection = mutationResult.getMutants();
		assertThat(mutantCollection).isEmpty();
	}
	
	@Test
	public void build_TODO_TODO() {
		
		int totalMutants = 1;
		int killedMutants = 0;
		Map<MutantGroupKey, List<MutantDetailResult>> mutantGroupMap = new HashMap<>();
		
		String mutantPackage = "package.of.mutant";
		String mutantClass = "ClassOfMutant";
		String filename = "ClassOfMutant.java";
		String mutator = "VOID_METHOD";
		String muid = mutantClass + "_1.muid";
		String method = "RemovedMethod";
		String description = "Remove void method call";
		String lineNumber = "12";
		
		MutantGroupKey groupKey = MutantGroupKey.of(mutantPackage, mutantClass, filename);
		
		MutantDetails details = mutantDetails(mutantPackage, mutantClass, filename);
		details.setMuid(muid);
		details.setMutator(mutator);
		details.setMethod(method);
		details.setDescription(description);
		details.setLineNumber(lineNumber);
		
		MutantDetailResult resultDetails = MutantDetailResult.lived(details);
		List<MutantDetailResult> detailList = detailList(resultDetails);
		
		mutantGroupMap.put(groupKey, detailList);
		
		MutationResultBuilder unitUnderTest = MutationResultBuilder.builder()
													.withMutantResults(mutantGroupMap)
													.withTotalMutants(totalMutants)
													.withKilledMutants(killedMutants);
		MutationResult mutationResult = unitUnderTest.build();
		
		
		assertThat(mutationResult).isNotNull();
		assertThat(mutationResult.getDate()).isEmpty();
		
		MutationOverview overview = mutationResult.getOverview();
		assertThat(overview).isNotNull();
		assertThat(overview.getMutationScore()).isZero();
		assertThat(overview.getNumberOfMutants()).isOne();
		assertThat(overview.getNumberOfMutantsKilled()).isZero();
		assertThat(overview.getPackageOverview()).hasSize(1);
		assertThat(overview.getClassOverview()).hasSize(1);
		
		TestSetup testSetup = mutationResult.getTestSetup();
		assertThat(testSetup).isNotNull();
		assertThat(testSetup.getClasses()).isEmpty();
		assertThat(testSetup.getPackages()).isEmpty();
		assertThat(testSetup.getRunner()).isNull();
		assertThat(testSetup.getTargetedMutants()).isEmpty();
		
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
		return new HashSet<>(Arrays.asList(mutants));
	}
	
	private InstrumentationTestOptions getEmptyTestOptions() {
		return new InstrumentationTestOptions();
	}
	
	private MutationResultBuilder newUnitUnderTest(InstrumentationTestOptions testOptions, Set<String> targetedMutants, Map<MutantGroupKey, List<MutantDetailResult>> mutantGroupMap,
			int totalMutants, int totalKilled) {
		return newUnitUnderTest(testOptions, targetedMutants, RESULT_TIMESTAMP, mutantGroupMap, totalMutants, totalKilled);
	}
	
	private MutationResultBuilder newUnitUnderTest(InstrumentationTestOptions testOptions, Set<String> targetedMutants,
			String resultTimeStampString, Map<MutantGroupKey, List<MutantDetailResult>> mutantGroupMap,
			int totalMutants, int totalKilled) {
		return new MutationResultBuilder(testOptions, targetedMutants, resultTimeStampString, mutantGroupMap, totalMutants, totalKilled);
	}
}
