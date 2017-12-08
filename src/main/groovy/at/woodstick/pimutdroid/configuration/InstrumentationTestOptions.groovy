package at.woodstick.pimutdroid.configuration;

import java.util.Set

import org.gradle.util.ConfigureUtil

public class InstrumentationTestOptions {
	final TargetTests targetTests = new TargetTests();
	
	Set<String> targetMutants;
	
	public InstrumentationTestOptions() {
		
	}

	public void targetTests(Closure configureClosure) {
		ConfigureUtil.configure(configureClosure, targetTests);
	}

	public TargetTests getTargetTests() {
		return targetTests;
	}
	
	public Set<String> getTargetMutants() {
		return targetMutants;
	}

	@Override
	public String toString() {
		return "InstrumentationTestOptions [targetTests=" + targetTests + "]";
	}
}
