package at.woodstick.pimutdroid.configuration;

import java.util.Set;

public class TargetTests {
	Set<String> packages;
	Set<String> classes;
	
	public Set<String> getPackages() {
		return packages;
	}

	public Set<String> getClasses() {
		return classes;
	}

	@Override
	public String toString() {
		return "TargetTests [packages=" + packages + ", classes=" + classes + "]";
	}
}
