package at.woodstick.pimutdroid.task;

import java.util.Set;

import org.gradle.api.tasks.Input;

public class MutateClassesTask extends PimutBaseTask {

	private Set<String> targetedMutants;

	@Override
	protected void exec() {
		getLogger().lifecycle("Class files mutated.");
		getLogger().lifecycle("Targeted mutants were {}", targetedMutants);
	}

	@Input
	public Set<String> getTargetedMutants() {
		return targetedMutants;
	}

	public void setTargetedMutants(Set<String> targetedMutants) {
		this.targetedMutants = targetedMutants;
	}
}
