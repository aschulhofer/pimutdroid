package at.woodstick.pimutdroid.task;

import java.util.Set;

public class MutateClassesTask extends PimutBaseTask {

	private Set<String> targetedMutants;
	private Integer maxMutationsPerClass;
	private Set<String> mutators;

	@Override
	protected void exec() {
		getLogger().info("Class files mutated.");
		getLogger().info("Targeted mutants were {}", targetedMutants);
		getLogger().info("Max mutants per class were {}", maxMutationsPerClass);
		getLogger().info("Mutators were {}", mutators);
	}

	public Set<String> getTargetedMutants() {
		return targetedMutants;
	}

	public void setTargetedMutants(Set<String> targetedMutants) {
		this.targetedMutants = targetedMutants;
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
