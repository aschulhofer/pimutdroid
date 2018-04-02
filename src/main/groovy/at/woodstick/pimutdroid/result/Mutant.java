package at.woodstick.pimutdroid.result;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class Mutant {

	@JacksonXmlProperty(isAttribute = true)
	private final String id;
	
	@JacksonXmlProperty(isAttribute = true)
	private final Outcome outcome;
	
	@JacksonXmlProperty(isAttribute = true)
	private final String file;
	
	private final Mutation mutation;

	public Mutant(String id, Outcome outcome, String file, Mutation mutation) {
		this.id = id;
		this.outcome = outcome;
		this.file = file;
		this.mutation = mutation;
	}

	public String getId() {
		return id;
	}

	public Outcome getOutcome() {
		return outcome;
	}

	public String getFile() {
		return file;
	}

	public Mutation getMutation() {
		return mutation;
	}
}
