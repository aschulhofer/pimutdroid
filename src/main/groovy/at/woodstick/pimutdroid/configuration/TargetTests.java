package at.woodstick.pimutdroid.configuration;

import java.util.Set;

public class TargetTests {
	private Set<String> packages;
	private Set<String> classes;
	
	public Set<String> getPackages() {
		return packages;
	}

	public void setPackages(Set<String> packages) {
		this.packages = packages;
	}

	public Set<String> getClasses() {
		return classes;
	}

	public void setClasses(Set<String> classes) {
		this.classes = classes;
	}

	@Override
	public String toString() {
		return "TargetTests [packages=" + packages + ", classes=" + classes + "]";
	}
}
