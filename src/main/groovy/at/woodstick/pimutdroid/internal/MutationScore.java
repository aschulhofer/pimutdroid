package at.woodstick.pimutdroid.internal;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MutationScore {

	private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
	
	/**
	 * Number of decimal places of mutation score
	 */
	protected static final int SCALE = 4;
	
	/**
	 *  Scale for devision is mutation score scale plus shift of decimal point because of multiplication by one hundred (= 2).
	 *  So it results in final scale of mutation score scale
	 *  
	 *  E.g.:
	 *  Expected mutation score scale = 5 -> yyy.xxxxx
	 *  Division scale -> 00.3333333    (Mutation score scale = 7)
	 *  Multiplication -> 33.33333      (Multiplication scale = 2) -> final scale = 5
	 */
	protected static final int DIVISION_SCALE = SCALE + 2; 
	
	private final int mutants;
	private final int killed;
	
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
			return BigDecimal.ZERO;
		}
		
		BigDecimal mutantsVal = new BigDecimal(mutants);
		BigDecimal mutantsKilledVal = new BigDecimal(killed);
		
		BigDecimal scoreVal = mutantsKilledVal.divide(mutantsVal, DIVISION_SCALE, RoundingMode.HALF_UP).multiply(ONE_HUNDRED);
		
		return scoreVal;
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
		if (this == obj)
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
