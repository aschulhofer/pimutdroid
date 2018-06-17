package at.woodstick.pimutdroid.task;

import java.nio.file.Path;
import java.nio.file.Paths;

import at.woodstick.pimutdroid.internal.AppApk;

public class BackupApplicationApksTask extends PimutBaseTask {

	private AppApk appApk;
	private AppApk testApk;
	
	private Path appResultRootDir; 
	
	@Override
	protected void beforeTaskAction() {
		if(appApk == null) {
			appApk = getAppApk();
		}
		
		if(testApk == null) {
			testApk = getTestApk();
		}
		
		if(appResultRootDir == null) {
			appResultRootDir = Paths.get(extension.getAppResultRootDir());
		}
	}
	
	@Override
	protected void exec() {
		// Copy unmutated apk
		appApk.copyTo(appResultRootDir);
		
		// Copy test apk
		testApk.copyTo(appResultRootDir);
	}

	public void setAppApk(AppApk appApk) {
		this.appApk = appApk;
	}

	public void setTestApk(AppApk testApk) {
		this.testApk = testApk;
	}

	public void setAppResultRootDir(Path appResultRootDir) {
		this.appResultRootDir = appResultRootDir;
	}
}
