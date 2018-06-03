package at.woodstick.pimutdroid.internal.pitest;

import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "mutations")
public class MutationsResultSet {

	@JacksonXmlElementWrapper(useWrapping = false)
	@JacksonXmlProperty(localName = "mutation")
	private List<MutationResult> result;

	public MutationsResultSet() {
	}

	public MutationsResultSet(List<MutationResult> result) {
		this.result = result;
	}

	public boolean isEmpty() {
		return (result == null || result.isEmpty());
	}
	
	public List<MutationResult> getResult() {
		return result;
	}

	public void setResult(List<MutationResult> result) {
		this.result = result;
	}
}
