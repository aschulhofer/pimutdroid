package at.woodstick.pimutdroid.configuration;

import java.util.HashSet;
import java.util.Set;

public class BuildConfiguration {

	private final String name;
	private Set<String> targetMutants = new HashSet<>();
	private Integer maxMutationsPerClass;
	private Set<String> mutators;

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

	public Integer getMaxMutationsPerClass() {
		return maxMutationsPerClass;
	}

	public void setMaxMutationsPerClass(Integer maxMutationsPerClass) {
		this.maxMutationsPerClass = maxMutationsPerClass;
	}

	public Set<String> getMutators() {
		return mutators;
	}

	public void setMutators(Set<String> mutators) {
		this.mutators = mutators;
	}
}
