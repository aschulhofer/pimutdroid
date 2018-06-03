package at.woodstick.pimutdroid.internal;

import at.woodstick.pimutdroid.result.Outcome;

public interface UnitTestResult {
	boolean hasResults();
	Outcome getOutcome(final String index, final String mutator, final String method, final String mutatedClass, final String sourceFile);
	boolean isKilled(final String index, final String mutator, final String method, final String mutatedClass, final String sourceFile);
}
