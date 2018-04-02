package at.woodstick.pimutdroid.result;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class Mutation {

	@JacksonXmlProperty(isAttribute = true)
	private final String method;
	
	@JacksonXmlProperty(isAttribute = true)
	private final String lineNumber;
	
	@JacksonXmlProperty(isAttribute = true)
	private final String mutator;
	
	@JacksonXmlProperty(isAttribute = true)
	private final String description;

	public Mutation(String method, String lineNumber, String mutator, String description) {
		this.method = method;
		this.lineNumber = lineNumber;
		this.mutator = mutator;
		this.description = description;
	}

	public String getMethod() {
		return method;
	}

	public String getLineNumber() {
		return lineNumber;
	}

	public String getMutator() {
		return mutator;
	}

	public String getDescription() {
		return description;
	}
}
