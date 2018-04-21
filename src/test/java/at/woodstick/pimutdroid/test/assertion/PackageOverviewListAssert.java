package at.woodstick.pimutdroid.test.assertion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.assertj.core.api.FactoryBasedNavigableListAssert;

import at.woodstick.pimutdroid.result.PackageOverview;

public class PackageOverviewListAssert extends FactoryBasedNavigableListAssert<PackageOverviewListAssert, List<? extends PackageOverview>, PackageOverview, PackageOverviewAssert> {

	public PackageOverviewListAssert(List<? extends PackageOverview> actual) {
		super(actual, PackageOverviewListAssert.class, (PackageOverview m) -> {
			return PackageOverviewAssert.assertThat(m);
		});
	}
	
	public static PackageOverviewListAssert assertThat(Collection<PackageOverview> mutantList) {
		return assertThat(new ArrayList<>(mutantList));
	}
	
	public static PackageOverviewListAssert assertThat(List<PackageOverview> mutantList) {
		return new PackageOverviewListAssert(mutantList);
	}
}
