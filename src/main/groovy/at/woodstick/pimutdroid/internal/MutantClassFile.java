package at.woodstick.pimutdroid.internal;

import java.io.File;
import java.nio.file.Path;

public class MutantClassFile {

	private File file;
	private String className;
	private boolean isInnerClass;
	private Path relativePackageClassDirPath;
	
	public MutantClassFile(File file, String className, boolean isInnerClass, Path relativePackageClassDirPath) {
		this.file = file;
		this.className = className;
		this.isInnerClass = isInnerClass;
		this.relativePackageClassDirPath = relativePackageClassDirPath;
	}

	public File getFile() {
		return file;
	}

	public String getClassName() {
		return className;
	}

	public boolean isInnerClass() {
		return isInnerClass;
	}

	public Path getRelativePackageClassDirPath() {
		return relativePackageClassDirPath;
	}

	@Override
	public String toString() {
		return "MutantClassFile [file=" + file + ", className=" + className + ", isInnerClass=" + isInnerClass
				+ ", relativePackageClassDirPath=" + relativePackageClassDirPath + "]";
	}
}
