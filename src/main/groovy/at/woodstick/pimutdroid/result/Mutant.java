package at.woodstick.pimutdroid.result;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonPropertyOrder(alphabetic = true)
public class Mutant {

	@JacksonXmlProperty(isAttribute = true)
	private final String id;
	
	@JacksonXmlProperty(isAttribute = true)
	private final Outcome outcome;
	
	private final Mutation mutation;

	public Mutant(String id, Outcome outcome, Mutation mutation) {
		this.id = id;
		this.outcome = outcome;
		this.mutation = mutation;
	}

	public String getId() {
		return id;
	}

	public Outcome getOutcome() {
		return outcome;
	}

	public Mutation getMutation() {
		return mutation;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((mutation == null) ? 0 : mutation.hashCode());
		result = prime * result + ((outcome == null) ? 0 : outcome.hashCode());
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
		Mutant other = (Mutant) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (mutation == null) {
			if (other.mutation != null)
				return false;
		} else if (!mutation.equals(other.mutation))
			return false;
		if (outcome != other.outcome)
			return false;
		return true;
	}
}
