package at.woodstick.pimutdroid.test.assertion;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

import at.woodstick.pimutdroid.result.Mutant;
import at.woodstick.pimutdroid.result.MutantGroup;

public class MutantGroupAssert extends AbstractAssert<MutantGroupAssert, MutantGroup> {

	public MutantGroupAssert(MutantGroup actual) {
		super(actual, MutantGroupAssert.class);
	}

	public static MutantGroupAssert assertThat(MutantGroup actual) {
		return new MutantGroupAssert(actual);
	}

	protected List<Mutant> mutantList() {
		return new ArrayList<>(actual.getMutantList());
	}
	
	public MutantAssert withMutantAt(int expectedIndex) {
		isNotNull();
		
		return MutantListAssert.assertThat(mutantList()).element(expectedIndex);
	}
	
	public MutantGroupAssert hasMutants(int expectedNumber) {
		isNotNull();

		Assertions.assertThat(actual.getMutantList()).hasSize(expectedNumber);
		
		return this;
	}
	
	public MutantGroupAssert hasMutants() {
		isNotNull();

		Assertions.assertThat(actual.getMutantList()).isNotEmpty();
		
		return this;
	}
	
	public MutantGroupAssert hasNoMutants() {
		isNotNull();

		Assertions.assertThat(actual.getMutantList()).isEmpty();
		
		return this;
	}
	
	public MutantGroupAssert hasMutantPackage(String mutantPackage) {
		isNotNull();
		
		if(! Objects.equals(actual.getMutantPackage(), mutantPackage)) {
			failWithMessage("Expected mutant package to be <%s> but was <%s>", mutantPackage, actual.getMutantPackage());
		}
		
		return this;
	}
	
	public MutantGroupAssert hasMutantClass(String mutantClass) {
		isNotNull();
		
		if(! Objects.equals(actual.getMutantClass(), mutantClass)) {
			failWithMessage("Expected mutant class to be <%s> but was <%s>", mutantClass, actual.getMutantClass());
		}
		
		return this;
	}
	
	public MutantGroupAssert hasKilledMutants(int mutantsKilled) {
		isNotNull();
		
		if(actual.getKilled() != mutantsKilled) {
			failWithMessage("Expected mutants killed to be <%s> but were <%s>", mutantsKilled, actual.getKilled());
		}
		
		return this;
	}
	
	public MutantGroupAssert hasFile(String file) {
		isNotNull();
		
		if(! Objects.equals(actual.getFile(), file)) {
			failWithMessage("Expected mutant file to be <%s> but was <%s>", file, actual.getFile());
		}
		
		return this;
	}
	
	public MutantGroupAssert hasScore(BigDecimal score) {
		isNotNull();
		
		Assertions.assertThat(actual.getScore()).isEqualByComparingTo(score);
		
		return this;
	}
}
