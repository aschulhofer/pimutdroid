package at.woodstick.pimutdroid.configuration;

import java.util.HashSet;
import java.util.Set;

public class BuildConfiguration {

	private final String name;
	private Set<String> targetMutants = new HashSet<>();

	public BuildConfiguration(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public Set<String> getTargetMutants() {
		return targetMutants;
	}

	public void setTargetMutants(Set<String> targetMutants) {
		this.targetMutants = targetMutants;
	}
}
