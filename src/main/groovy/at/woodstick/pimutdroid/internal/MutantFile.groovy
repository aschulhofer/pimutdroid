package at.woodstick.pimutdroid.internal

import java.io.File

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

@Deprecated
//TODO: better name
class MutantFile {
	private final static Logger LOGGER = Logging.getLogger(MutantFile);
	
	private final int id;
	private final File classFile;
	
	public MutantFile(int mutantId, File mutantFile) {
		this.id = mutantId;
		this.classFile = mutantFile;
	}
	
	public int getId() {
		return id;
	}

	public String getName() {
		return classFile.getName();
	}
	
	public File getFile() {
		return classFile;
	}
	
	public Map<String, ?> getTargetFileInfo() {
		return getTargetFileInfoFromMutantClass(getName());
	}
	
	private String classFileNameToRelativePackagePath(final String className) {
		def pathSegs = className.split("\\.")

		// remove class segment and file name
		pathSegs = (pathSegs - pathSegs[-1] - pathSegs[-2])

		return pathSegs.join("/")
	}

	private String classFileNameWithoutRelativePackagePath(final String className) {
		def pathSegs = className.split("\\.")
		return pathSegs[-2] + "." + pathSegs[-1]
	}

	private Map<String, ?> getTargetFileInfoFromMutantClass(final String className) {
		def pathSegs = className.split("\\.")

		def fileName = pathSegs[-2] + "." + pathSegs[-1];

		// remove class segment and file name
		pathSegs = (pathSegs - pathSegs[-1] - pathSegs[-2])

		def filePath = pathSegs.join("/")

		return [name: fileName, path: filePath, className: fileName.replace(".class", "")]
	}
}
