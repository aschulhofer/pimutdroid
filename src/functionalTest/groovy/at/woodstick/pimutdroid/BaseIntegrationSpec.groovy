package at.woodstick.pimutdroid;

import java.io.File

import groovy.transform.CompileStatic
import spock.lang.Shared

abstract class BaseIntegrationSpec extends nebula.test.IntegrationSpec {

	@Shared protected String emulatorId;
	
	def setupSpec() {
		emulatorId = loadEmulatorId();
		
		System.setProperty('ignoreDeprecations', 'true');
	}
	
	def setup() {
		//fork = true;
		setProjectDir();
	}
	
	void startEmulators() {
		
	}
	
	String loadEmulatorId() throws IOException {
		Properties gradleProps = new Properties();
		
		InputStream is = getClass().getClassLoader().getResourceAsStream("emulator.properties");
		if(is == null) {
			throw new IOException("emulator.properties not found in spec");
		}
		
		gradleProps.load((InputStream)is);
		
		return gradleProps.getOrDefault("emulator.port", null);
	}  
	
	File setProjectDir() {
		projectDir = new File("build/nebulatest/${this.class.simpleName}/${testName.methodName.replaceAll(/\W+/, '-')}").absoluteFile
		if (projectDir.exists()) {
			projectDir.deleteDir()
		}
		projectDir.mkdirs()
		
		return projectDir
	}

	void setupBaseProject() {
		setupBaseProject(true);
	}
	
	void setupBaseProjectWithoutExport() {
		setupBaseProject(false);
	}
	
	void setupBaseProject(boolean includePitetExportPlugin) {
		copyResources("base-project/simple-android-application", ".")
		
		if(includePitetExportPlugin) {
			copyResources("pitest-export-build", ".")
		}
	}
	
	void setupBuildFile(final String buildFile) {
		copyResources("build-files/${buildFile}", "app/build.gradle")
	}
	
	void setupAndroidProject(String relativeProjectPath) {
		copyResources(relativeProjectPath, ".")
	}
	
	void setupBaseProjectForBuild(final String buildFile) {
		setupBaseProject();
		setupBuildFile(buildFile);
	}
	
	void setupBaseProjectWithoutExportForBuild(final String buildFile) {
		setupBaseProjectWithoutExport();
		setupBuildFile(buildFile);
	}
	
	void verifyAndroidProject() {
		assert fileExists('app/build.gradle') == true
	}
}
