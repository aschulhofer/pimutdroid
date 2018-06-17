package at.woodstick.pimutdroid.configuration;

import java.util.Set;

public class DevicesOptions {
	
	private Set<String> serialNumbers;
	private Boolean parallelExecution;
	private Boolean ignoreAndroidSerial;
	
	public DevicesOptions() {
		
	}

	public Set<String> getSerialNumbers() {
		return serialNumbers;
	}

	public void setSerialNumbers(Set<String> serialNumbers) {
		this.serialNumbers = serialNumbers;
	}

	public Boolean getParallelExecution() {
		return parallelExecution;
	}

	public void setParallelExecution(Boolean parallelExecution) {
		this.parallelExecution = parallelExecution;
	}

	public Boolean getIgnoreAndroidSerial() {
		return ignoreAndroidSerial;
	}

	public void setIgnoreAndroidSerial(Boolean ignoreAndroidSerial) {
		this.ignoreAndroidSerial = ignoreAndroidSerial;
	}

	@Override
	public String toString() {
		return "DevicesOptions [serialNumbers=" + serialNumbers + ", parallelExecution=" + parallelExecution
				+ ", ignoreAndroidSerial=" + ignoreAndroidSerial + "]";
	}
}
