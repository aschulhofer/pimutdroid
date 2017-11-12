package at.woodstick.pimutdroid.result;

public class MutationResult {

	private final int numberOfMutantsKilled;
	private final int numberOfMutants;
	private final double mutationScore;

	public MutationResult(int numberOfMutantsKilled, int numberOfMutants, double mutationScore) {
		this.numberOfMutantsKilled = numberOfMutantsKilled;
		this.numberOfMutants = numberOfMutants;
		this.mutationScore = mutationScore;
	}

	public int getNumberOfMutantsKilled() {
		return numberOfMutantsKilled;
	}

	public int getNumberOfMutants() {
		return numberOfMutants;
	}

	public double getMutationScore() {
		return mutationScore;
	}
}
