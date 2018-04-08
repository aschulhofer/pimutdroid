package at.woodstick.pimutdroid.task;

import at.woodstick.pimutdroid.internal.MutantDetails;
import at.woodstick.pimutdroid.result.Outcome;


public class MutantDetailResult {
	private final MutantDetails details;
	private final Outcome outcome;
	
	public MutantDetailResult(MutantDetails details, Outcome outcome) {
		this.details = details;
		this.outcome = outcome;
	}
	
	public MutantDetails getDetails() {
		return details;
	}
	
	public Outcome getOutcome() {
		return outcome;
	}
	
	public static final MutantDetailResult of(MutantDetails details, Outcome outcome) {
		return new MutantDetailResult(details, outcome);
	}
	
	public static final MutantDetailResult killed(MutantDetails details) {
		return MutantDetailResult.of(details, Outcome.KILLED);
	}
	
	public static final MutantDetailResult lived(MutantDetails details) {
		return MutantDetailResult.of(details, Outcome.LIVED);
	}
	
	public static final MutantDetailResult noResult(MutantDetails details) {
		return MutantDetailResult.of(details, Outcome.NO_RESULT);
	}
}
