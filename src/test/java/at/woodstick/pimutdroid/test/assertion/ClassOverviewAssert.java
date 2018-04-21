package at.woodstick.pimutdroid.test.assertion;

import java.math.BigDecimal;
import java.util.Objects;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

import at.woodstick.pimutdroid.result.ClassOverview;

public class ClassOverviewAssert extends AbstractAssert<ClassOverviewAssert, ClassOverview> {

	private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
	
	public ClassOverviewAssert(ClassOverview actual) {
		super(actual, ClassOverviewAssert.class);
	}

	public static ClassOverviewAssert assertThat(ClassOverview actual) {
		return new ClassOverviewAssert(actual);
	}
	
	public ClassOverviewAssert hasPackage(String packageName) {
		isNotNull();
		
		if(! Objects.equals(actual.getPackageName(), packageName)) {
			failWithMessage("Expected class overview package name to be <%s> but was <%s>", packageName, actual.getPackageName());
		}
		
		return this;
	}
	
	public ClassOverviewAssert hasName(String name) {
		isNotNull();
		
		if(! Objects.equals(actual.getName(), name)) {
			failWithMessage("Expected class overview name to be <%s> but was <%s>", name, actual.getName());
		}
		
		return this;
	}
	
	public ClassOverviewAssert hasMutants(int mutants) {
		isNotNull();
		
		if(actual.getMutants() != mutants) {
			failWithMessage("Expected class overview number of mutants to be <%s> but was <%s>", mutants, actual.getMutants());
		}
		
		return this;
	}
	
	public ClassOverviewAssert hasKilledMutants(int mutants) {
		isNotNull();
		
		if(actual.getKilled() != mutants) {
			failWithMessage("Expected class overview number killed of mutants to be <%s> but was <%s>", mutants, actual.getKilled());
		}
		
		return this;
	}
	
	public ClassOverviewAssert hasScore(BigDecimal score) {
		isNotNull();
		
		Assertions.assertThat(actual.getScore()).isEqualByComparingTo(score);
		
		return this;
	}
	
	public ClassOverviewAssert hasZeroScore() {
		isNotNull();
		
		Assertions.assertThat(actual.getScore()).isZero();
		
		return this;
	}
	
	public ClassOverviewAssert hasFullScore() {
		isNotNull();
		
		hasScore(ONE_HUNDRED);
		
		return this;
	}
}
