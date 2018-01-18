package at.woodstick.pimutdroid.internal;

import java.io.File;

public class MutantMarkerFile {
	private final File file;
	private final String subId;
	private final String mutantClassName;
	private final String fileName;
	
	public MutantMarkerFile(File file, String subId, String mutantClassName, String fileName) {
		this.file = file;
		this.subId = subId;
		this.mutantClassName = mutantClassName;
		this.fileName = fileName;
	}

	public File getFile() {
		return file;
	}

	public String getSubId() {
		return subId;
	}

	public String getMutantClassName() {
		return mutantClassName;
	}

	public String getFileName() {
		return fileName;
	}

	@Override
	public String toString() {
		return "MutantMarkerFile [file=" + file + ", subId=" + subId + ", mutantClassName=" + mutantClassName
				+ ", fileName=" + fileName + "]";
	}
}
