package at.woodstick.pimutdroid.test.assertion;

import java.math.BigDecimal;
import java.util.Objects;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

import at.woodstick.pimutdroid.result.PackageOverview;

public class PackageOverviewAssert extends AbstractAssert<PackageOverviewAssert, PackageOverview> {

	private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
	
	public PackageOverviewAssert(PackageOverview actual) {
		super(actual, PackageOverviewAssert.class);
	}

	public static PackageOverviewAssert assertThat(PackageOverview actual) {
		return new PackageOverviewAssert(actual);
	}
	
	public PackageOverviewAssert hasName(String name) {
		isNotNull();
		
		if(! Objects.equals(actual.getName(), name)) {
			failWithMessage("Expected package name to be <%s> but was <%s>", name, actual.getName());
		}
		
		return this;
	}
	
	public PackageOverviewAssert hasMutants(int mutants) {
		isNotNull();
		
		if(actual.getMutants() != mutants) {
			failWithMessage("Expected packages number of mutants to be <%s> but was <%s>", mutants, actual.getMutants());
		}
		
		return this;
	}
	
	public PackageOverviewAssert hasKilledMutants(int mutants) {
		isNotNull();
		
		if(actual.getKilled() != mutants) {
			failWithMessage("Expected packages number killed of mutants to be <%s> but was <%s>", mutants, actual.getKilled());
		}
		
		return this;
	}
	
	public PackageOverviewAssert hasScore(BigDecimal score) {
		isNotNull();
		
		Assertions.assertThat(actual.getScore()).isEqualByComparingTo(score);
		
		return this;
	}
	
	public PackageOverviewAssert hasZeroScore() {
		isNotNull();
		
		Assertions.assertThat(actual.getScore()).isZero();
		
		return this;
	}
	
	public PackageOverviewAssert hasFullScore() {
		isNotNull();
		
		hasScore(ONE_HUNDRED);
		
		return this;
	}
}
