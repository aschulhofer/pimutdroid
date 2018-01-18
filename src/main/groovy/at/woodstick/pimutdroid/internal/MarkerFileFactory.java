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
		final String[] classNameSegments = classFile.getName().split("\\.");
		final String className = classNameSegments[classNameSegments.length-2];
		final String subId = classFile.getParentFile().getName();
		
		final String filename = getMarkerFileName(className, subId);
		
		final File muidFile = new File(fileDir, filename);
		final MutantMarkerFile markerFile = new MutantMarkerFile(muidFile, subId, className, filename);

		return markerFile;
	}
	
	public MutantMarkerFile fromMarkerFile(final File markerFile) {
		final String filename = markerFile.getName();
		
		final String[] filenameSegements = filename.split(SEPARATOR);
		
		final String className = filenameSegements[0];
		final String subId = filenameSegements[1];
		
		final MutantMarkerFile mutantMarkerFile = new MutantMarkerFile(markerFile, subId, className, filename);

		return mutantMarkerFile;
	}
	
}
