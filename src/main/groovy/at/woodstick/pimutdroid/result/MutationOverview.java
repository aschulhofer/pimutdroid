package at.woodstick.pimutdroid.result;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "overview")
public class MutationOverview {

	private final int numberOfMutantsKilled;
	private final int numberOfMutants;
	private final double mutationScore;

	public MutationOverview(int numberOfMutantsKilled, int numberOfMutants, double mutationScore) {
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
