package at.woodstick.pimutdroid.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.junit.Test;

public class MutationScoreTest {

	private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
	private static final BigDecimal SCORE_ZERO = BigDecimal.ZERO;
	private static final BigDecimal SCORE_ONE_HUNDRED = ONE_HUNDRED;
	
	// ########################################################################
	
	@Test
	public void of_staticOfScore_sameAsConstructor() {
		int mutants = 5;
		int killed  = 4;
		
		MutationScore ofScore = MutationScore.of(mutants, killed);
		MutationScore constructorScore = new MutationScore(mutants, killed);
		
		assertThat(ofScore).isNotNull();
		assertThat(ofScore).isEqualTo(constructorScore);
		assertThat(ofScore.getScore()).isEqualTo(constructorScore.getScore());
	}
	
	@Test
	public void getter_fieldCorrectInitialized_sameAsGivenValues() {
		int mutants = 5;
		int killed  = 4;
		
		MutationScore ofScore = MutationScore.of(mutants, killed);
		
		assertThat(ofScore.getMutants()).isEqualTo(mutants);
		assertThat(ofScore.getKilled()).isEqualTo(killed);
	}
	
	@Test
	public void equals_differentScoreInstancesWithSameValues_areEqual() {
		int mutants = 5;
		int killed  = 4;
		
		MutationScore firstScore = MutationScore.of(mutants, killed);
		MutationScore secondScore = MutationScore.of(mutants, killed);
		
		assertThat(firstScore.equals(secondScore)).isTrue();
	}
	
	@Test
	public void equals_differentScoreInstancesWithSameValues_haveSameHashCode() {
		int mutants = 5;
		int killed  = 4;
		
		MutationScore firstScore = MutationScore.of(mutants, killed);
		MutationScore secondScore = MutationScore.of(mutants, killed);
		
		assertThat(firstScore.hashCode()).isEqualTo(secondScore.hashCode());
	}
	
	@Test
	public void equals_differentScoreInstancesWithDifferentValues_areNotEqual() {
		MutationScore firstScore = MutationScore.of(5, 4);
		MutationScore secondScore = MutationScore.of(1, 0);
		
		assertThat(firstScore.equals(secondScore)).isFalse();
	}
	
	@Test
	public void equals_differentScoreInstancesWithSameValues_dontHaveSameHashCode() {
		MutationScore firstScore = MutationScore.of(5, 4);
		MutationScore secondScore = MutationScore.of(1, 1);
		
		assertThat(firstScore.hashCode()).isNotEqualTo(secondScore.hashCode());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void constructor_numberOfMutantsNegative_throwIllegalArgumentExcetion() {
		int mutants = -1;
		int killed  = 0;
		
		new MutationScore(mutants, killed);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void constructor_numberOfKilledMutantsNegative_throwIllegalArgumentExcetion() {
		int mutants = 0;
		int killed  = -1;
		
		new MutationScore(mutants, killed);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void constructor_numberOfMutantsAndNumberOfKilledMutantsNegative_throwIllegalArgumentExcetion() {
		int mutants = -1;
		int killed  = -1;
		
		new MutationScore(mutants, killed);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void of_numberOfMutantsNegative_throwIllegalArgumentExcetion() {
		int mutants = -1;
		int killed  = 0;
		
		MutationScore.of(mutants, killed);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void of_numberOfKilledMutantsNegative_throwIllegalArgumentExcetion() {
		int mutants = 0;
		int killed  = -1;
		
		MutationScore.of(mutants, killed);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void of_numberOfMutantsAndNumberOfKilledMutantsNegative_throwIllegalArgumentExcetion() {
		int mutants = -1;
		int killed  = -1;
		
		MutationScore.of(mutants, killed);
	}
	
	@Test
	public void getScore_numberOfMutantsIsZero_scoreIsZero() {
		int mutants = 0;
		int killed  = 0;
		
		MutationScore score = MutationScore.of(mutants, killed);
		
		assertThatScoreIsEqualTo(score, SCORE_ZERO);
	}
	
	@Test
	public void getScore_numberOfKilledMutantsIsZero_scoreIsZero() {
		int mutants = 10;
		int killed  = 0;
		
		MutationScore score = MutationScore.of(mutants, killed);
		
		assertThatScoreIsEqualTo(score, SCORE_ZERO);
	}
	
	@Test
	public void getScore_allMutantsKilled_scoreIsOneHundred() {
		int mutants = 10;
		int killed  = mutants;
		
		MutationScore score = MutationScore.of(mutants, killed);
		
		assertThatScoreIsEqualTo(score, SCORE_ONE_HUNDRED);
	}
	
	@Test
	public void getScore_portionOfMutantsKilled_scoreIsOneThird() {
		int mutants = 90;
		int killed  = 30;
		
		MutationScore score = MutationScore.of(mutants, killed);
		
		assertThatScoreIsEqualTo(score, expectedScore(mutants, killed));
		assertThatScoreIsEqualTo(score, "33.3333");
	}
	
	@Test
	public void getScore_moreMutantsKilled_scoreIsMoreThanHundred() {
		int mutants = 90;
		int killed  = 110;
		
		MutationScore score = MutationScore.of(mutants, killed);
		
		assertThatScoreIsEqualTo(score, expectedScore(mutants, killed));
		assertThatScoreIsEqualTo(score, "122.2222");
	}
	
	// ########################################################################,
	
	private BigDecimal expectedScore(int mutants, int killed) {
		BigDecimal mutantsVal = new BigDecimal(mutants);
		BigDecimal mutantsKilledVal = new BigDecimal(killed);
		
		/*
		 *  Scale for devision is mutation score scale plus shift of decimal point because of multiplication by one hundred (= 2).
		 *  So it results in final scale of mutation score scale
		 *  
		 *  Expected mutation score scale = 5 -> yyy.xxxxx
		 *  Division scale -> 00.3333333    (Mutation score scale = 7)
		 *  Multiplication -> 33.33333      (Multiplication scale = 2) -> final scale = 5
		 */
		int scale = MutationScore.SCALE + 2;
		BigDecimal scoreVal = mutantsKilledVal.divide(mutantsVal, scale, RoundingMode.HALF_UP).multiply(ONE_HUNDRED);
		
		return scoreVal;
	}
	
	// ########################################################################
	
	private void assertThatScoreIsEqualTo(MutationScore actualScore, String expectedScore) {
		assertScoreIsEqual(actualScore.getScore(), new BigDecimal(expectedScore));
	}
	
	private void assertThatScoreIsEqualTo(MutationScore actualScore, BigDecimal expectedScore) {
		assertScoreIsEqual(actualScore.getScore(), expectedScore);
	}
	
	private void assertScoreIsEqual(BigDecimal actualScore, BigDecimal expectedScore) {
		assertThat(actualScore).isEqualByComparingTo(expectedScore);
	}
}
