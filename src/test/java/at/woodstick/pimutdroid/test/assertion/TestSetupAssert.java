package at.woodstick.pimutdroid.test.assertion;

import java.util.Objects;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

import at.woodstick.pimutdroid.result.TestSetup;

public class TestSetupAssert extends AbstractAssert<TestSetupAssert, TestSetup> {

	public TestSetupAssert(TestSetup actual) {
		super(actual, TestSetupAssert.class);
	}

	public static TestSetupAssert assertThat(TestSetup actual) {
		return new TestSetupAssert(actual);
	}
	
	public TestSetupAssert hasRunner(String expectedRunner) {
		isNotNull();
		
		if(! Objects.equals(actual.getRunner(), expectedRunner)) {
			failWithMessage("Expected test runner to be <%s> but was <%s>", expectedRunner, actual.getRunner());
		}
		
		return this;
	}
	
	public TestSetupAssert hasTestClassesInOrder(String...expectedClasses) {
		isNotNull();
		
		Assertions.assertThat(actual.getClasses()).containsExactly(expectedClasses);
		
		return this;
	}
	
	public TestSetupAssert hasNoTestClasses() {
		isNotNull();
		
		Assertions.assertThat(actual.getClasses()).isEmpty();
		
		return this;
	}
	
	public TestSetupAssert hasTestPackagesInOrder(String...expectedPackages) {
		isNotNull();
		
		Assertions.assertThat(actual.getPackages()).containsExactly(expectedPackages);
		
		return this;
	}
	
	public TestSetupAssert hasNoTestPackages() {
		isNotNull();
		
		Assertions.assertThat(actual.getPackages()).isEmpty();
		
		return this;
	}
	
	public TestSetupAssert hasTargetedMutantsInOrder(String...expectedMutants) {
		isNotNull();
		
		Assertions.assertThat(actual.getTargetedMutants()).containsExactly(expectedMutants);
		
		return this;
	}
	
	public TestSetupAssert isDefault() {
		isNotNull();
		
		hasNoTestClasses();
		hasNoTestPackages();
		Assertions.assertThat(actual.getTargetedMutants()).isEmpty();
		Assertions.assertThat(actual.getRunner()).isNull();
		
		return this;
	}
}
