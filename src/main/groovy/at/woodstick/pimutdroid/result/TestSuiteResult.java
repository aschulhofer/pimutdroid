package at.woodstick.pimutdroid.result;

import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "testsuite")
public class TestSuiteResult {

	@JacksonXmlProperty(localName = "name", isAttribute = true)
    private String name;
	
    @JacksonXmlProperty(localName = "tests", isAttribute = true)
    private long tests;
	
    @JacksonXmlProperty(localName = "failures", isAttribute = true)
    private long failures;
	
    @JacksonXmlProperty(localName = "errors", isAttribute = true)
    private long errors;
	
	@JacksonXmlProperty(localName = "skipped", isAttribute = true)
	private long skipped;
	
	@JacksonXmlElementWrapper(useWrapping = false)
	@JacksonXmlProperty(localName = "testcase")
	private List<Testcase> testcases;

	public String getName() {
		return name;
	}

	public long getTests() {
		return tests;
	}

	public long getFailures() {
		return failures;
	}

	public long getErrors() {
		return errors;
	}

	public long getSkipped() {
		return skipped;
	}

	public List<Testcase> getTestcases() {
		return testcases;
	}

	@Override
	public String toString() {
		return "TestSuiteResult [name=" + name + ", tests=" + tests + ", failures=" + failures + ", errors=" + errors
				+ ", skipped=" + skipped + ", testcases=" + testcases + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (errors ^ (errors >>> 32));
		result = prime * result + (int) (failures ^ (failures >>> 32));
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + (int) (skipped ^ (skipped >>> 32));
		result = prime * result + ((testcases == null) ? 0 : testcases.hashCode());
		result = prime * result + (int) (tests ^ (tests >>> 32));
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
		TestSuiteResult other = (TestSuiteResult) obj;
		if (errors != other.errors)
			return false;
		if (failures != other.failures)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (skipped != other.skipped)
			return false;
		if (testcases == null) {
			if (other.testcases != null)
				return false;
		} else if (!testcases.equals(other.testcases))
			return false;
		if (tests != other.tests)
			return false;
		return true;
	}
}
