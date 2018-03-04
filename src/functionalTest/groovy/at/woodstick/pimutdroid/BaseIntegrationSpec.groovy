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

	void setupAndroidProject(String relativeProjectPath) {
		copyResources(relativeProjectPath, ".")
	}
	
	void verifyAndroidProject() {
		assert fileExists('app/build.gradle') == true
	}
}
