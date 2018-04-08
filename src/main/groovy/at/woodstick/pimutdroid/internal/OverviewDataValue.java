package at.woodstick.pimutdroid.internal;

public class OverviewDataValue {

	private int mutants;
	private int killed;
	
	public OverviewDataValue(int mutants, int killed) {
		this.mutants = mutants;
		this.killed = killed;
	}

	public void addMutants(int mutants) {
		this.mutants += mutants;
	}
	
	public void addKilled(int killed) {
		this.killed += killed;
	}
	
	public int getMutants() {
		return mutants;
	}

	public void setMutants(int mutants) {
		this.mutants = mutants;
	}

	public int getKilled() {
		return killed;
	}

	public void setKilled(int killed) {
		this.killed = killed;
	}

	public static OverviewDataValue empty() {
		return new OverviewDataValue(0, 0);
	}
}
