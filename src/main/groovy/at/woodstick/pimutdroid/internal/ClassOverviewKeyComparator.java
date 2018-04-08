package at.woodstick.pimutdroid.internal;

import java.util.Comparator;

public class ClassOverviewKeyComparator implements Comparator<ClassOverviewKey> {

	private Comparator<ClassOverviewKey> comparator;
	
	public ClassOverviewKeyComparator(Comparator<ClassOverviewKey> comparator) {
		this.comparator = comparator;
	}

	@Override
	public int compare(ClassOverviewKey o1, ClassOverviewKey o2) {
		return comparator.compare(o1, o2);
	}
	
	public static final Comparator<ClassOverviewKey> getDefault() {
		Comparator<ClassOverviewKey> defaultComparator = Comparator
				.comparing(ClassOverviewKey::getMutantPackage)
				.thenComparing(ClassOverviewKey::getFilename);
		
		return new ClassOverviewKeyComparator(defaultComparator);
	}
}
