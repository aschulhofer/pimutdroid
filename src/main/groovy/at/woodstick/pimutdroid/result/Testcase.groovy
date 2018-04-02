package at.woodstick.pimutdroid.result

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

@JacksonXmlRootElement(localName = "testcase")
class Testcase {

	@JacksonXmlProperty(localName = "name", isAttribute = true)
    private String name;
	
    @JacksonXmlProperty(localName = "classname", isAttribute = true)
    private String className;
	
	@JacksonXmlProperty(localName = "failure")
	private TestcaseFailure failure;
//	private String failure;
	
	@JacksonXmlProperty(localName = "time", isAttribute = true)
	private String time;
	
	// Set on field because skipped tag is empty tag hence not null if skipped
	@JacksonXmlProperty(localName = "skipped")
	private SkippedMarker skipped = SkippedMarker.notSkipped();

	public String getName() {
		return name;
	}

	public String getClassName() {
		return className;
	}

//	public TestcaseFailure getFailure() {
//		return failure;
//	}

	/**
	 * Setter used to support deserialization of an &lt;skipped /&gt; empty tag to a none null value. If tag 
	 * not present setter is not called. Set to none skipped marker instance on field initialization.
	 * 
	 * @param skippedMarker
	 */
	public void setSkipped(SkippedMarker skippedMarker) {
		if(skippedMarker == null) {
			skippedMarker = SkippedMarker.skipped();
		}
		
		skipped = skippedMarker;
	}
	
	public SkippedMarker getSkipped() {
		return skipped;
	}
	
	public boolean isSkipped() {
		return skipped.isSkipped();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((className == null) ? 0 : className.hashCode());
		result = prime * result + ((failure == null) ? 0 : failure.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((skipped == null) ? 0 : skipped.hashCode());
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
		Testcase other = (Testcase) obj;
		if (className == null) {
			if (other.className != null)
				return false;
		} else if (!className.equals(other.className))
			return false;
		if (failure == null) {
			if (other.failure != null)
				return false;
		} else if (!failure.equals(other.failure))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (skipped == null) {
			if (other.skipped != null)
				return false;
		} else if (!skipped.equals(other.skipped))
			return false;
		return true;
	}
}
