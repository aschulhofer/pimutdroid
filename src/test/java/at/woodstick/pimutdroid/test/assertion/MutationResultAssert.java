package at.woodstick.pimutdroid.test.assertion;

import java.util.Objects;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

import at.woodstick.pimutdroid.result.MutationResult;

public class MutationResultAssert extends AbstractAssert<MutationResultAssert, MutationResult> {

	public MutationResultAssert(MutationResult actual) {
		super(actual, MutationResultAssert.class);
	}

	public static MutationResultAssert assertThat(MutationResult actual) {
		return new MutationResultAssert(actual);
	}
	
	public MutationResultAssert hasNoDate() {
		isNotNull();
		
		Assertions.assertThat(actual.getDate()).isEmpty();
		
		return this;
	}
	
	public MutationResultAssert hasDate(String date) {
		isNotNull();
		
		if(! Objects.equals(actual.getDate(), date)) {
			failWithMessage("Expected mutant date to be <%s> but was <%s>", date, actual.getDate());
		}
		
		return this;
	}
}
