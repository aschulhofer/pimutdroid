package at.woodstick.pimutdroid.result;

public class MutationResult {

	private final MutationOverview overview;
	private final TestSetup testSetup;

	public MutationResult(MutationOverview overview, TestSetup testSetup) {
		this.overview = overview;
		this.testSetup = testSetup;
	}

	public MutationOverview getOverview() {
		return overview;
	}

	public TestSetup getTestSetup() {
		return testSetup;
	}
}
