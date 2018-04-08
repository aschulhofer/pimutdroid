package at.woodstick.pimutdroid.internal;

public class PackageOverviewKey {
	
	private final String mutantPackage;
	
	public PackageOverviewKey(String mutantPackage) {
		this.mutantPackage = mutantPackage;
	}

	public String getMutantPackage() {
		return mutantPackage;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mutantPackage == null) ? 0 : mutantPackage.hashCode());
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
		PackageOverviewKey other = (PackageOverviewKey) obj;
		if (mutantPackage == null) {
			if (other.mutantPackage != null)
				return false;
		} else if (!mutantPackage.equals(other.mutantPackage))
			return false;
		return true;
	}

	public static PackageOverviewKey of(String mutantPackage) {
		return new PackageOverviewKey(mutantPackage); 
	}
}
