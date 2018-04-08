package at.woodstick.pimutdroid.internal;

public class ClassOverviewKey {
	
	private final String mutantPackage;
	private final String filename;
	
	public ClassOverviewKey(String mutantPackage, String filename) {
		this.mutantPackage = mutantPackage;
		this.filename = filename;
	}

	public String getMutantPackage() {
		return mutantPackage;
	}

	public String getFilename() {
		return filename;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((filename == null) ? 0 : filename.hashCode());
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
		ClassOverviewKey other = (ClassOverviewKey) obj;
		if (filename == null) {
			if (other.filename != null)
				return false;
		} else if (!filename.equals(other.filename))
			return false;
		if (mutantPackage == null) {
			if (other.mutantPackage != null)
				return false;
		} else if (!mutantPackage.equals(other.mutantPackage))
			return false;
		return true;
	}

	public static ClassOverviewKey of(String mutantPackage, String filename) {
		return new ClassOverviewKey(mutantPackage, filename); 
	}
}
