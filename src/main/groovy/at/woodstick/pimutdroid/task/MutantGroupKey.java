package at.woodstick.pimutdroid.task;

class MutantGroupKey {
	
	private final String mutantPackage;
	private final String mutantClass;
	private final String filename;
	
	MutantGroupKey(String mutantPackage, String mutantClass, String filename) {
		this.mutantPackage = mutantPackage;
		this.mutantClass = mutantClass;
		this.filename = filename;
	}

	public String getMutantPackage() {
		return mutantPackage;
	}

	public String getMutantClass() {
		return mutantClass;
	}

	public String getFilename() {
		return filename;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((filename == null) ? 0 : filename.hashCode());
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
		if (mutantPackage == null) {
			if (other.mutantPackage != null)
				return false;
		} else if (!mutantPackage.equals(other.mutantPackage))
			return false;
		if (filename == null) {
			if (other.filename != null)
				return false;
		} else if (!filename.equals(other.filename))
			return false;
		if (mutantClass == null) {
			if (other.mutantClass != null)
				return false;
		} else if (!mutantClass.equals(other.mutantClass))
			return false;
		return true;
	}

	public static MutantGroupKey of(String mutantPackage, String mutantClass, String filename) {
		return new MutantGroupKey(mutantPackage, mutantClass, filename); 
	}
}
