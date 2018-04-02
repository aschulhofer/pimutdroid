package at.woodstick.pimutdroid.result;

import java.util.Set;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class TestSetup {
	
	@JacksonXmlElementWrapper(localName="packages")
	@JacksonXmlProperty(localName = "package")
	private final Set<String> packages;
	
	@JacksonXmlElementWrapper(localName="classes")
	@JacksonXmlProperty(localName = "class")
	private final Set<String> classes;
	
	@JacksonXmlElementWrapper(localName="targetedMutants")
	@JacksonXmlProperty(localName = "target")
	private final Set<String> targetedMutants;
	
	private final String runner;
	
	public TestSetup(Set<String> packages, Set<String> classes, Set<String> targetedMutants, String runner) {
		this.packages = packages;
		this.classes = classes;
		this.targetedMutants = targetedMutants;
		this.runner = runner;
	}
	
	public Set<String> getPackages() {
		return packages;
	}
	
	public Set<String> getClasses() {
		return classes;
	}
	
	public Set<String> getTargetedMutants() {
		return targetedMutants;
	}
	
	public String getRunner() {
		return runner;
	}
}
