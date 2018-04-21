package at.woodstick.pimutdroid.test.assertion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.assertj.core.api.FactoryBasedNavigableListAssert;

import at.woodstick.pimutdroid.result.ClassOverview;

public class ClassOverviewListAssert extends FactoryBasedNavigableListAssert<ClassOverviewListAssert, List<? extends ClassOverview>, ClassOverview, ClassOverviewAssert> {

	public ClassOverviewListAssert(List<? extends ClassOverview> actual) {
		super(actual, ClassOverviewListAssert.class, (ClassOverview m) -> {
			return ClassOverviewAssert.assertThat(m);
		});
	}
	
	public static ClassOverviewListAssert assertThat(Collection<ClassOverview> mutantList) {
		return assertThat(new ArrayList<>(mutantList));
	}
	
	public static ClassOverviewListAssert assertThat(List<ClassOverview> mutantList) {
		return new ClassOverviewListAssert(mutantList);
	}
}
