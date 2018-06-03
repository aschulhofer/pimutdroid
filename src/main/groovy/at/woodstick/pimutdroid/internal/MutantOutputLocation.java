package at.woodstick.pimutdroid.internal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class MutantOutputLocation {

	private Path mutantClassFilesRootDirPath;
	private Path mutantResultRootDirPath;
	
	private File mutantMarkerFile;
	private Path targetDirPath;
	
	public MutantOutputLocation(Path mutantClassFilesRootDirPath, Path mutantResultRootDirPath, File mutantMarkerFile) {
		this.mutantClassFilesRootDirPath = mutantClassFilesRootDirPath;
		this.mutantResultRootDirPath = mutantResultRootDirPath;
		this.mutantMarkerFile = mutantMarkerFile;
		ensureTargetDirPath();
	}

	protected void ensureTargetDirPath() {
		Path mutantDirPath = mutantMarkerFile.getParentFile().toPath();
		
		Path relativeMutantDirPath = mutantClassFilesRootDirPath.relativize(mutantDirPath);
		
		targetDirPath = mutantResultRootDirPath.resolve(relativeMutantDirPath);
	}
	
	public Path getTargetDirPath() {
		return targetDirPath;
	}
	
	public void createDirectory() throws IOException {
		Files.createDirectories(targetDirPath);
	}
	
	public void copyMarkerFile() throws IOException {
		createDirectory();
		Files.copy(mutantMarkerFile.toPath(), targetDirPath.resolve(mutantMarkerFile.getName()), StandardCopyOption.REPLACE_EXISTING);
	}
}
