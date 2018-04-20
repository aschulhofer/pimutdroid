package at.woodstick.pimutdroid.internal;

import java.math.RoundingMode

public class MutationScore {

	protected static final int SCALE = 4;
	
	private int mutants;
	private int killed;
	
	public MutationScore(int mutants, int killed) {
		if(mutants < 0) {
			throw new IllegalArgumentException("Number of mutants must not be negative");
		}
		
		if(killed < 0) {
			throw new IllegalArgumentException("Number of killed mutants must not be negative");
		}
		
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
		if(mutants == 0) {
			return 0;
		}
		
		return round( (killed / mutants) * 100 );
	}
	
	protected BigDecimal round(BigDecimal score) {
		return score.setScale(SCALE, RoundingMode.HALF_UP);
	}
	
	public static MutationScore of(int mutants, int killed) {
		return new MutationScore(mutants, killed);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + killed;
		result = prime * result + mutants;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this.is(obj))
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MutationScore other = (MutationScore) obj;
		if (killed != other.killed)
			return false;
		if (mutants != other.mutants)
			return false;
		return true;
	}
}
