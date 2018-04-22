package at.woodstick.pimutdroid.test.helper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import at.woodstick.pimutdroid.configuration.InstrumentationTestOptions;
import at.woodstick.pimutdroid.internal.MutantDetails;
import at.woodstick.pimutdroid.result.Mutation;
import at.woodstick.pimutdroid.result.Outcome;
import at.woodstick.pimutdroid.task.MutantDetailResult;
import at.woodstick.pimutdroid.task.MutantGroupKey;

public class TestHelper {

	public static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
	public static final BigDecimal SCORE_ZERO = BigDecimal.ZERO;
	public static final BigDecimal SCORE_ONE_HUNDRED = TestHelper.ONE_HUNDRED;
	
	public static final Path TEST_RESOURCES_BASE_PATH = Paths.get("src/test/resources/at/woodstick/pimutdroid");
	public static final Path TEST_RESOURCES_INTERAL_PACKAGE_PATH = TEST_RESOURCES_BASE_PATH.resolve("internal");
	
	/**
	 * @param mutantClassName class of mutant, e.g.: MyClass$1
	 * @param subId sub id of mutant, e.g.: 0
	 * @return <i>&lt;mutantClassName&gt;</i>_<i>&lt;subId&gt;</i>.muid
	 */
	public static final String getMarkerFileName(String mutantClassName, String subId) {
		return String.format("%s%s%s.%s", mutantClassName, "_", subId, "muid");
	}
	
	/**
	 * @param mutantPackage package of mutant, e.g.: MyClass$1
	 * @param mutantClassName class of mutant, e.g.: at.woodstick.mysample
	 * @return <i>&lt;mutantClassName&gt;</i>.<i>&lt;mutantClassName&gt;</i>.class
	 */
	public static final String getMutantClassFileName(String mutantPackage, String mutantClassName) {
		return String.format("%s.%s.%s", mutantPackage, mutantClassName, "class");
	}
	
	/**
	 * <pre>
	 * Map
	 * 
	 * {"listener": ["at.woodstick.MyListener"], "class": ["at.woodstick.Test", "at.woodstick.Test2"]}
	 * 
	 * maps to:
	 * 
	 * ["-e", "listener", "at.woodstick.MyListener", "-e", "class", "at.woodstick.Test,at.woodstick.Test"]
	 * </pre>
	 * @param testOptions
	 * @return
	 */
	public static final List<String> mapTestOptionsMapToList(Map<String, List<String>> testOptions) {
		List<String> testOptionList = new ArrayList<String>();
		for(Map.Entry<String, List<String>> entry : testOptions.entrySet()) {
			testOptionList.add("-e");
			testOptionList.add(entry.getKey());
			testOptionList.add(String.join(",", entry.getValue()));
		}
		return testOptionList;
	}
	
	public static MutantDetailResultBuilder newMutantDetailResult() {
		return new MutantDetailResultBuilder();
	}
	
	public static InstrumentationTestOptionsBuilder newInstrumentationTestOptions() {
		return new InstrumentationTestOptionsBuilder();
	}
	
	public static Mutation mutation(MutantDetails details) {
		return mutation(details.getMethod(), details.getLineNumber(), details.getMutator(), details.getDescription());
	}
	
	public static Mutation mutation(String method, String lineNumber, TestMutator mutator) {
		return mutation(method, lineNumber, mutator.getMutator(), mutator.getDescription());
	}
	
	public static Mutation mutation(String method, String lineNumber, String mutator, String description) {
		return new Mutation(method, lineNumber, mutator, description);
	}
	
	public static BigDecimal expectedScore(int mutants, int killed) {
		BigDecimal mutantsVal = new BigDecimal(mutants);
		BigDecimal mutantsKilledVal = new BigDecimal(killed);
		
		/*
		 *  Scale for devision is mutation score scale plus shift of decimal point because of multiplication by one hundred (= 2).
		 *  So it results in final scale of mutation score scale
		 *  
		 *  E.g.:
		 *  Expected mutation score scale = 5 -> yyy.xxxxx
		 *  Division scale -> 00.3333333    (Mutation score scale = 7)
		 *  Multiplication -> 33.33333      (Multiplication scale = 2) -> final scale = 5
		 */
		int scale = 4 + 2;
		BigDecimal scoreVal = mutantsKilledVal.divide(mutantsVal, scale, RoundingMode.HALF_UP).multiply(ONE_HUNDRED);
		
		return scoreVal;
	}
	
	public static Set<String> asSet(String...values) {
		return new HashSet<>(Arrays.asList(values));
	}
	
	@SafeVarargs
	public static <T> List<T> asList(T...values) {
		return new ArrayList<T>(Arrays.asList(values));
	}
	
	public static class MutantDetailResultBuilder {
		
		private Outcome outcome;
		private MutantDetails details;
		
		public MutantDetailResultBuilder() {
			
		}
		
		public MutantDetailResultBuilder withDetails(String muid, MutantGroupKey group, String method, String lineNumber, TestMutator mutator) {
			return withDetails(muid, group.getMutantPackage(), group.getMutantClass(), method, mutator.getMutator(), group.getFilename(), lineNumber, mutator.getDescription());
		}
		
		public MutantDetailResultBuilder withDetails(String muid, String clazzPackage, String clazzName, String method, String filename, String lineNumber, TestMutator mutator) {
			return withDetails(muid, clazzPackage, clazzName, method, mutator.getMutator(), filename, lineNumber, mutator.getDescription());
		}
		
		public MutantDetailResultBuilder withDetails(String muid, String clazzPackage, String clazzName, String method, String mutator, String filename, String lineNumber, String description) {
			return withDetails(muid, clazzPackage, clazzName, clazzPackage + "." + clazzName, method, mutator, filename, lineNumber, description);
		}
		
		public MutantDetailResultBuilder withDetails(String muid, String clazzPackage, String clazzName, String clazz, String method, String mutator, String filename, String lineNumber, String description) {
			MutantDetails details = new MutantDetails();
			
			details.setMuid(muid);
			details.setClazzName(clazzName);
			details.setClazzPackage(clazzPackage);
			details.setClazz(clazz);
			details.setMethod(method);
			details.setMutator(mutator);
			details.setFilename(filename);
			details.setLineNumber(lineNumber);
			details.setDescription(description);
			
			 this.details = details;
			 
			 return this;
		}
		
		public MutantDetailResultBuilder withDetails(MutantDetails details) {
			this.details = details;
			return this;
		}
		
		public MutantDetailResultBuilder withOutcome(Outcome outcome) {
			this.outcome = outcome;
			return this;
		}
		
		public MutantDetailResultBuilder lived() {
			return withOutcome(Outcome.LIVED);
		}
		
		public MutantDetailResultBuilder killed() {
			return withOutcome(Outcome.KILLED);
		}
		
		public MutantDetailResultBuilder noResult() {
			return withOutcome(Outcome.NO_RESULT);
		}
		
		public MutantDetailResult get() {
			return MutantDetailResult.of(details, outcome);
		}
	}
	
	public static class InstrumentationTestOptionsBuilder {
		
		private InstrumentationTestOptions testOptions;
		
		public InstrumentationTestOptionsBuilder() {
			testOptions = new InstrumentationTestOptions();
		}
		
		public InstrumentationTestOptionsBuilder withRunner(String runner) {
			testOptions.setRunner(runner);
			return this;
		}
		
		public InstrumentationTestOptionsBuilder withTargetedMutants(String...mutants) {
			testOptions.setTargetMutants(asSet(mutants));
			return this;
		}
		
		public InstrumentationTestOptionsBuilder withTargetedMutants(Set<String> mutants) {
			testOptions.setTargetMutants(mutants);
			return this;
		}
		
		public InstrumentationTestOptionsBuilder withTestPackages(String...packages) {
			return withTestPackages(asSet(packages));
		}
		
		public InstrumentationTestOptionsBuilder withTestPackages(Set<String> packages) {
			testOptions.getTargetTests().setPackages(packages);
			return this;
		}
		
		public InstrumentationTestOptionsBuilder withTestClasses(String...classes) {
			return withTestClasses(asSet(classes));
		}
		
		public InstrumentationTestOptionsBuilder withTestClasses(Set<String> classes) {
			testOptions.getTargetTests().setClasses(classes);
			return this;
		}
		
		public InstrumentationTestOptions get() {
			return testOptions;
		}
	}
	
}
