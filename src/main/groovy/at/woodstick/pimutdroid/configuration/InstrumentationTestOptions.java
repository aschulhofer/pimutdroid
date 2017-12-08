package at.woodstick.pimutdroid.configuration;

import java.util.Set;
import groovy.lang.Closure;
import org.gradle.util.ConfigureUtil;

public class InstrumentationTestOptions {
	private final TargetTests targetTests = new TargetTests();
	
	private Set<String> targetMutants;
	
	public InstrumentationTestOptions() {
		
	}

	public void targetTests(Closure<?> configureClosure) {
		ConfigureUtil.configure(configureClosure, targetTests);
	}

	public Set<String> getTargetMutants() {
		return targetMutants;
	}

	public void setTargetMutants(Set<String> targetMutants) {
		this.targetMutants = targetMutants;
	}

	public TargetTests getTargetTests() {
		return targetTests;
	}

	@Override
	public String toString() {
		return "InstrumentationTestOptions [targetTests=" + targetTests + "]";
	}
}
