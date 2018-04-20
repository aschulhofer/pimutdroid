package at.woodstick.pimutdroid.test.assertion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.assertj.core.api.FactoryBasedNavigableListAssert;

import at.woodstick.pimutdroid.result.MutantGroup;

public class MutantGroupListAssert extends FactoryBasedNavigableListAssert<MutantGroupListAssert, List<? extends MutantGroup>, MutantGroup, MutantGroupAssert> {

	public MutantGroupListAssert(List<? extends MutantGroup> actual) {
		super(actual, MutantGroupListAssert.class, (MutantGroup m) -> {
			return MutantGroupAssert.assertThat(m);
		});
	}
	
	public static MutantGroupListAssert assertThat(Collection<MutantGroup> mutantList) {
		return assertThat(new ArrayList<>(mutantList));
	}
	
	public static MutantGroupListAssert assertThat(List<MutantGroup> mutantList) {
		return new MutantGroupListAssert(mutantList);
	}
}
