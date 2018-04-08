package at.woodstick.pimutdroid.internal;

public class MutationScore {

	private int mutants;
	private int killed;
	
	public MutationScore(int mutants, int killed) {
		this.mutants = mutants;
		this.killed = killed;
	}
	
	public int getMutants() {
		return mutants;
	}

	public int getKilled() {
		return killed;
	}

	public BigDecimal getScore() {
		return ( (killed / mutants) * 100 );
	}
	
	public static MutationScore of(int mutants, int killed) {
		return new MutationScore(mutants, killed);
	}
}
