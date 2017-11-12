package at.woodstick.pimutdroid.result;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText

@JacksonXmlRootElement(localName = "failure")
public class TestcaseFailure {
	
	@JacksonXmlText
	private String failure;

	public TestcaseFailure() {
	}

	public TestcaseFailure(String failure) {
		this.failure = failure;
	}
	
	public void setFailure(String failure) {
		this.failure = failure;
	}

	public String getFailure() {
		return failure;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((failure == null) ? 0 : failure.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this.is(obj))
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TestcaseFailure other = (TestcaseFailure) obj;
		if (failure == null) {
			if (other.failure != null)
				return false;
		} else if (!failure.equals(other.failure))
			return false;
		return true;
	}
}
