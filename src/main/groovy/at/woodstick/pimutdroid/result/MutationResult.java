package at.woodstick.pimutdroid.result;

import java.util.Collection;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "mutationResult")
public class MutationResult {

	@JacksonXmlProperty(isAttribute = true)
	private final String date;
	
	private final MutationOverview overview;
	
	private final TestSetup testSetup;
	
	@JacksonXmlElementWrapper(localName = "mutants")
	@JacksonXmlProperty(localName = "mutantGroup")
	private final Collection<MutantGroup> mutants;

	public MutationResult(String date, MutationOverview overview, TestSetup testSetup, Collection<MutantGroup> mutants) {
		this.date = date;
		this.overview = overview;
		this.testSetup = testSetup;
		this.mutants = mutants;
	}

	public String getDate() {
		return date;
	}

	public MutationOverview getOverview() {
		return overview;
	}

	public TestSetup getTestSetup() {
		return testSetup;
	}

	public Collection<MutantGroup> getMutants() {
		return mutants;
	}
}
