package at.woodstick.pimutdroid.test.assertion;

import java.util.Objects;

import org.assertj.core.api.AbstractAssert;

import at.woodstick.pimutdroid.result.Mutant;
import at.woodstick.pimutdroid.result.Mutation;
import at.woodstick.pimutdroid.result.Outcome;

public class MutantAssert extends AbstractAssert<MutantAssert, Mutant> {

	public MutantAssert(Mutant actual) {
		super(actual, MutantAssert.class);
	}

	public static MutantAssert assertThat(Mutant actual) {
		return new MutantAssert(actual);
	}
	
	public MutantAssert hasMutation() {
		isNotNull();
		
		if(actual.getMutation() == null) {
			failWithMessage("Expected mutant to have mutation but was null");
		}
		
		return this;
	}
	
	public MutantAssert hasId(String id) {
		isNotNull();
		
		if(! Objects.equals(actual.getId(), id)) {
			failWithMessage("Expected mutant id to be <%s> but was <%s>", id, actual.getId());
		}
		
		return this;
	}
	
	public MutantAssert hasOutcome(Outcome outcome) {
		isNotNull();
		
		if(! Objects.equals(actual.getOutcome(), outcome)) {
			failWithMessage("Expected mutant outcome to be <%s> but was <%s>", outcome, actual.getOutcome());
		}
		
		return this;
	}
	
	public MutantAssert isAlive() {
		isNotNull();
		
		hasOutcome(Outcome.LIVED);
		
		return this;
	}
	
	public MutantAssert wasKilled() {
		isNotNull();
		
		hasOutcome(Outcome.KILLED);
		
		return this;
	}
	
	public MutantAssert wasNoResult() {
		isNotNull();
		
		hasOutcome(Outcome.NO_RESULT);
		
		return this;
	}
	
	public MutantAssert hasMutation(Mutation expected) {
		isNotNull();
		
		MutationAssert.assertThat(actual.getMutation()).hasMutation(expected);
		
		return this;
	}
	
	public MutantAssert hasMutation(String mutator, String method, String lineNumber, String description) {
		isNotNull();
		
		MutationAssert.assertThat(actual.getMutation()).hasMutation(mutator, method, lineNumber, description);
		
		return this;
	}
}
