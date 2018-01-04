package at.woodstick.pimutdroid.internal;

import java.io.File;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

public class MarkerFileProvider {
	static final Logger LOGGER = Logging.getLogger(MarkerFileProvider.class);

	public static final String FILE_EXTENSION = "muid";
	
	protected String getMarkerFileName(String mutantClassName, String subId) {
		return String.format("%s_%s.%s", mutantClassName, subId, getMarkerFileExtension());
	}
	
	public String getMarkerFileExtension() {
		return FILE_EXTENSION;
	}
	
	public MutantMarkerFile fromClassFile(final File classFile) {
		final File fileDir = classFile.getParentFile();
		final String[] classNameSegments = classFile.getName().split("\\.");
		final String className = classNameSegments[classNameSegments.length-2];
		final String subId = classFile.getParentFile().getName();
		
		final String fileName = getMarkerFileName(className, subId);
		
		final File muidFile = new File(fileDir, fileName);
		final MutantMarkerFile markerFile = new MutantMarkerFile(muidFile, className, subId, fileName);

		return markerFile;
	}
	
	public MutantMarkerFile fromMarkerFile(final File markerFile) {
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
}
