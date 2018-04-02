package at.woodstick.pimutdroid.task;

class MutantGroupKey {
	
	private final String mutantPackage;
	private final String mutantClass;
	
	MutantGroupKey(String mutantPackage, String mutantClass) {
		this.mutantPackage = mutantPackage;
		this.mutantClass = mutantClass;
	}

	public String getMutantPackage() {
		return mutantPackage;
	}

	public String getMutantClass() {
		return mutantClass;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mutantClass == null) ? 0 : mutantClass.hashCode());
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
		MutantGroupKey other = (MutantGroupKey) obj;
		if (mutantClass == null) {
			if (other.mutantClass != null)
				return false;
		} else if (!mutantClass.equals(other.mutantClass))
			return false;
		if (mutantPackage == null) {
			if (other.mutantPackage != null)
				return false;
		} else if (!mutantPackage.equals(other.mutantPackage))
			return false;
		return true;
	}
	
	public static MutantGroupKey of(String mutantPackage, String mutantClass) {
		return new MutantGroupKey(mutantPackage, mutantClass); 
	}
}
