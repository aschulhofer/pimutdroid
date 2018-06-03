package at.woodstick.pimutdroid.internal.pitest;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "mutation")
public class MutationResult {

	@JacksonXmlProperty(isAttribute = true)
	private String detected;
	
	@JacksonXmlProperty(isAttribute = true)
	private String status;
	
	private String sourceFile;
	private String mutatedClass;
	private String index;
	private String mutator;
	
	@JacksonXmlProperty(localName = "mutatedMethod")
	private String method;
	
	public MutationResult() {
	}

	public String getDetected() {
		return detected;
	}
	
	public void setDetected(String detected) {
		this.detected = detected;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getSourceFile() {
		return sourceFile;
	}
	
	public void setSourceFile(String sourceFile) {
		this.sourceFile = sourceFile;
	}
	
	public String getMutatedClass() {
		return mutatedClass;
	}
	
	public void setMutatedClass(String mutatedClass) {
		this.mutatedClass = mutatedClass;
	}
	
	public String getIndex() {
		return index;
	}
	
	public void setIndex(String index) {
		this.index = index;
	}

	public String getMutator() {
		return mutator;
	}

	public void setMutator(String mutator) {
		this.mutator = mutator;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	@Override
	public String toString() {
		return "MutationResult [detected=" + detected + ", status=" + status + ", sourceFile=" + sourceFile
				+ ", mutatedClass=" + mutatedClass + ", index=" + index + ", mutator=" + mutator + ", method=" + method
				+ "]";
	}
}
