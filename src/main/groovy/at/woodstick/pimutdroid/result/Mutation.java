package at.woodstick.pimutdroid.result;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonPropertyOrder({"method", "line", "description", "mutator"})
public class Mutation {

	@JacksonXmlProperty(isAttribute = true)
	private final String method;
	
	@JacksonXmlProperty(localName = "line", isAttribute = true)
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((lineNumber == null) ? 0 : lineNumber.hashCode());
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		result = prime * result + ((mutator == null) ? 0 : mutator.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Mutation other = (Mutation) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (lineNumber == null) {
			if (other.lineNumber != null)
				return false;
		} else if (!lineNumber.equals(other.lineNumber))
			return false;
		if (method == null) {
			if (other.method != null)
				return false;
		} else if (!method.equals(other.method))
			return false;
		if (mutator == null) {
			if (other.mutator != null)
				return false;
		} else if (!mutator.equals(other.mutator))
			return false;
		return true;
	}
}
