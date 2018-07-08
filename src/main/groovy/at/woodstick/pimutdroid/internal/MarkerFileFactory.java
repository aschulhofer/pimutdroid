package at.woodstick.pimutdroid.internal;

import java.io.File;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

public class MarkerFileFactory {
	static final Logger LOGGER = Logging.getLogger(MarkerFileFactory.class);

	public static final String FILE_EXTENSION = "muid";
	static final String SEPARATOR = "_";
	
	protected String getMarkerFileName(String mutantClassName, String subId) {
		return String.format("%s%s%s.%s", mutantClassName, SEPARATOR, subId, getMarkerFileExtension());
	}
	
	public String getMarkerFileExtension() {
		return FILE_EXTENSION;
	}
	
	public MutantMarkerFile fromClassFile(final File classFile) {
		final File fileDir = classFile.getParentFile();
		final String classFileName = classFile.getName();
		int fileExtensionPos = classFileName.lastIndexOf(".");
		final String className = classFileName.substring(0, fileExtensionPos);
		
		final String subId = classFile.getParentFile().getName();
		
		final String filename = getMarkerFileName(className, subId);
		
		final File muidFile = new File(fileDir, filename);
		final MutantMarkerFile markerFile = new MutantMarkerFile(muidFile, subId, className, filename);

		return markerFile;
	}
	
	public MutantMarkerFile fromMarkerFile(final File markerFile) {
		final String filename = markerFile.getName();
		
		int separatorPos = filename.lastIndexOf(SEPARATOR);
		int fileExtensionPos = filename.lastIndexOf(".");
		
		/* 
		 * E.g.: at.woodstick.app.MySampleClass_123.muid
		 * 
		 * separatorPos -----------------------|
		 * fileExtensionPos -----------------------|
		 * 
		 *       at.woodstick.app.MySampleClass
		 * classNamePos ---------|============|
		 * 
		 *                                     _123.muid
		 * subId -------------------------------|=|
		 */
		
		final String fullClassName = filename.substring(0, separatorPos);
		int classNamePos = fullClassName.lastIndexOf(".");
		
		final String className = fullClassName.substring(classNamePos+1, fullClassName.length());
		
		final String subId = filename.substring(separatorPos+1, fileExtensionPos);
		
		final MutantMarkerFile mutantMarkerFile = new MutantMarkerFile(markerFile, subId, className, filename);

		return mutantMarkerFile;
	}
	
}
