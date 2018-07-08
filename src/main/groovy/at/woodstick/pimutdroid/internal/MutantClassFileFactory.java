package at.woodstick.pimutdroid.internal;

import java.io.File;
import java.nio.file.Path;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

public class MutantClassFileFactory {
	static final Logger LOGGER = Logging.getLogger(MutantClassFileFactory.class);
	
	static final String CLASS_FILE_EXTENSION = "class";
	static final String INNER_CLASS_PATTERN = "$";
	
	private Path mutantClassFilesRootDirPath;
	
	public MutantClassFileFactory(Path mutantClassFilesRootDirPath) {
		this.mutantClassFilesRootDirPath = mutantClassFilesRootDirPath;
	}

	protected String getClassNameWithExtension(final String className) {
		return String.format("%s.%s", className, CLASS_FILE_EXTENSION);
	}
	
	protected boolean isInnerClass(final String className) {
		return className.contains(INNER_CLASS_PATTERN);
	}
	
	protected Path getPackageClassPath(File classFile, boolean isInnerClass) {
		File packageClassDir = classFile.getParentFile().getParentFile().getParentFile().getParentFile(); 
		
		if(isInnerClass) {
			packageClassDir = packageClassDir.getParentFile();
		}
		
		return packageClassDir.toPath();
	}
	
	public MutantClassFile fromMarkerFile(final MutantMarkerFile markerFile) {
		final String className = markerFile.getMutantClassName();
		final boolean isInnerClass = isInnerClass(className);
		final File classFile = markerFile.getFile();
		Path packageClassDirPath = getPackageClassPath(classFile, isInnerClass);
		Path relativePackageClassDirPath = mutantClassFilesRootDirPath.relativize(packageClassDirPath);

		String classPackage = relativePackageClassDirPath.toString().replace(File.separator, ".");
		
		Path directory = classFile.getParentFile().toPath();
		Path classFilePath = directory.resolve(classPackage + "." + getClassNameWithExtension(className));
		
		MutantClassFile mutantClassFile = new MutantClassFile(classFilePath.toFile(), className, isInnerClass, relativePackageClassDirPath);
		
		return mutantClassFile;
	}
}
