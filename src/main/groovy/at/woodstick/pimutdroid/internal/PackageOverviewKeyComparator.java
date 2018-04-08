package at.woodstick.pimutdroid.internal;

import java.util.Comparator;

public class PackageOverviewKeyComparator implements Comparator<PackageOverviewKey> {

	private Comparator<PackageOverviewKey> comparator;
	
	public PackageOverviewKeyComparator(Comparator<PackageOverviewKey> comparator) {
		this.comparator = comparator;
	}

	@Override
	public int compare(PackageOverviewKey o1, PackageOverviewKey o2) {
		return comparator.compare(o1, o2);
	}
	
	public static final Comparator<PackageOverviewKey> getDefault() {
		Comparator<PackageOverviewKey> defaultComparator = Comparator
				.comparing(PackageOverviewKey::getMutantPackage);
		
		return new PackageOverviewKeyComparator(defaultComparator);
	}
}
