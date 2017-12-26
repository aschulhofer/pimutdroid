package at.woodstick.pimutdroid;

import javax.inject.Inject

import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory

import at.woodstick.pimutdroid.configuration.InstrumentationTestOptions
import groovy.transform.CompileStatic
import info.solidsoft.gradle.pitest.PitestPluginExtension;

@CompileStatic
class PimutdroidPluginExtension {
	PitestPluginExtension pitest;

	final InstrumentationTestOptions instrumentationTestOptions = new InstrumentationTestOptions();
	
	String packageDir;
	String mutantsDir;
	Boolean outputMutateAll;
	Boolean outputMutantCreation;
	Integer maxFirstMutants;
	
	String outputDir;
	
	String testReportDir;
	String testResultDir;
	
	String mutantResultRootDir;
	String appResultRootDir;
	String classFilesDir;
	
	public PimutdroidPluginExtension() {
		
	}
	
// In gradle 4.3.1 project.objects
//	@Inject
//	PimutdroidPluginExtension(ObjectFactory objectFactory) {
//		instrumentationTestOptions = objectFactory.newInstance(InstrumentationTestOptions.class);
//	}
	
	public void instrumentationTestOptions(Action<? extends InstrumentationTestOptions> action) {
		action.execute(instrumentationTestOptions);
	}
	
}
