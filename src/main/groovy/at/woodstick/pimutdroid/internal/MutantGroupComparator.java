package at.woodstick.pimutdroid.internal;

import java.util.Comparator;

import at.woodstick.pimutdroid.result.MutantGroup;

public class MutantGroupComparator implements Comparator<MutantGroup> {

	private Comparator<MutantGroup> comparator;
	
	public MutantGroupComparator(Comparator<MutantGroup> comparator) {
		this.comparator = comparator;
	}

	@Override
	public int compare(MutantGroup o1, MutantGroup o2) {
		return comparator.compare(o1, o2);
	}
	
	public static final Comparator<MutantGroup> getDefault() {
		Comparator<MutantGroup> defaultComparator = Comparator
				.comparing(MutantGroup::getMutantPackage)
				.thenComparing(MutantGroup::getFile)
				.thenComparing(MutantGroup::getMutantClass)
				.thenComparing(MutantGroup::getMutants)
				.thenComparing(MutantGroup::getKilled);
		
		return new MutantGroupComparator(defaultComparator);
	}
}
