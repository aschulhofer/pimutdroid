package at.woodstick.pimutdroid.test.assertion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.assertj.core.api.FactoryBasedNavigableListAssert;

import at.woodstick.pimutdroid.result.Mutant;

public class MutantListAssert extends FactoryBasedNavigableListAssert<MutantListAssert, List<? extends Mutant>, Mutant, MutantAssert> {

	public MutantListAssert(List<? extends Mutant> actual) {
		super(actual, MutantListAssert.class, (Mutant m) -> {
			return MutantAssert.assertThat(m);
		});
	}
	
	public static MutantListAssert assertThat(Collection<Mutant> mutantList) {
		return assertThat(new ArrayList<>(mutantList));
	}
	
	public static MutantListAssert assertThat(List<Mutant> mutantList) {
		return new MutantListAssert(mutantList);
	}
}
