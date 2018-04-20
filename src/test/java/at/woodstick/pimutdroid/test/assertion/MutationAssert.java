package at.woodstick.pimutdroid.test.assertion;

import java.util.Objects;

import org.assertj.core.api.AbstractAssert;

import at.woodstick.pimutdroid.result.Mutation;

public class MutationAssert extends AbstractAssert<MutationAssert, Mutation> {

	public MutationAssert(Mutation actual) {
		super(actual, MutationAssert.class);
	}

	public static MutationAssert assertThat(Mutation actual) {
		return new MutationAssert(actual);
	}

	public MutationAssert hasMutator(String mutator) {
		isNotNull();

		if (!Objects.equals(actual.getMutator(), mutator)) {
			failWithMessage("Expected mutations mutator to be <%s> but was <%s>", mutator, actual.getMutator());
		}

		return this;
	}

	public MutationAssert hasMethod(String method) {
		isNotNull();

		if (!Objects.equals(actual.getMethod(), method)) {
			failWithMessage("Expected mutations method to be <%s> but was <%s>", method, actual.getMethod());
		}

		return this;
	}

	public MutationAssert hasLineNumber(String lineNumber) {
		isNotNull();

		if (!Objects.equals(actual.getLineNumber(), lineNumber)) {
			failWithMessage("Expected mutations lineNumber to be <%s> but was <%s>", lineNumber, actual.getLineNumber());
		}

		return this;
	}

	public MutationAssert hasDescription(String description) {
		isNotNull();

		if (!Objects.equals(actual.getDescription(), description)) {
			failWithMessage("Expected mutations description to be <%s> but was <%s>", description, actual.getDescription());
		}

		return this;
	}

	public MutationAssert hasMutation(Mutation expected) {
		hasMutator(expected.getMutator());
		hasMethod(expected.getMethod());
		hasLineNumber(expected.getLineNumber());
		hasDescription(expected.getDescription());

		return this;
	}

	public MutationAssert hasMutation(String mutator, String method, String lineNumber, String description) {
		hasMutator(mutator);
		hasMethod(method);
		hasLineNumber(lineNumber);
		hasDescription(description);

		return this;
	}
}
