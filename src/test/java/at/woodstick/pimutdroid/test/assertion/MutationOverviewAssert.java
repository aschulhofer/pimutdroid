package at.woodstick.pimutdroid.test.assertion;

import java.math.BigDecimal;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

import at.woodstick.pimutdroid.result.MutationOverview;

public class MutationOverviewAssert extends AbstractAssert<MutationOverviewAssert, MutationOverview> {

	private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
	
	public MutationOverviewAssert(MutationOverview actual) {
		super(actual, MutationOverviewAssert.class);
	}

	public static MutationOverviewAssert assertThat(MutationOverview actual) {
		return new MutationOverviewAssert(actual);
	}
	
	public MutationOverviewAssert hasNoKilledMutants() {
		isNotNull();
		
		hasKilledMutants(0);
		
		return this;
	}
	
	public MutationOverviewAssert hasKilledMutants(int mutantsKilled) {
		isNotNull();
		
		if(actual.getNumberOfMutantsKilled() != mutantsKilled) {
			failWithMessage("Expected overviews number of killed mutants to be <%s> but was <%s>", mutantsKilled, actual.getNumberOfMutantsKilled());
		}
		
		return this;
	}
	
	public MutationOverviewAssert hasTotalMutants(int totalMutants) {
		isNotNull();
		
		if(actual.getNumberOfMutants() != totalMutants) {
			failWithMessage("Expected overviews number of mutants to be <%s> but was <%s>", totalMutants, actual.getNumberOfMutants());
		}
		
		return this;
	}
	
	public MutationOverviewAssert hasScore(BigDecimal score) {
		isNotNull();
		
		Assertions.assertThat(actual.getMutationScore()).isEqualByComparingTo(score);
		
		return this;
	}
	
	public MutationOverviewAssert hasZeroScore() {
		isNotNull();
		
		Assertions.assertThat(actual.getMutationScore()).isZero();
		
		return this;
	}
	
	public MutationOverviewAssert hasFullScore() {
		isNotNull();
		
		hasScore(ONE_HUNDRED);
		
		return this;
	}
	
	public MutationOverviewAssert hasPackagOverviews(int expectedNumber) {
		isNotNull();

		Assertions.assertThat(actual.getPackageOverview()).hasSize(expectedNumber);
		
		return this;
	}
	
	public MutationOverviewAssert hasClassOverviews(int expectedNumber) {
		isNotNull();

		Assertions.assertThat(actual.getClassOverview()).hasSize(expectedNumber);
		
		return this;
	}
	
	public MutationOverviewAssert isDefault() {
		isNotNull();
		
		hasZeroScore();
		hasNoKilledMutants();
		hasTotalMutants(0);
		hasPackagOverviews(0);
		hasClassOverviews(0);
		
		return this;
	}
}
