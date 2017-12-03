package at.woodstick.pimutdroid;

import info.solidsoft.gradle.pitest.PitestPluginExtension;

class PimutdroidPluginExtension {
	PitestPluginExtension pitest;

	String packageDir;
	String mutantsDir;
	Boolean outputMutateAll;
	Boolean outputMutantCreation;
	Integer maxFirstMutants;
	
	String outputDir;
	
	String testReportDir;
	String testResultDir;
	
	Boolean skipInnerClasses;
	
	Set<String> targetMutants;
	
	String mutantResultRootDir;
	String appResultRootDir;
	String classFilesDir;
	
	public PimutdroidPluginExtension() {
		
	}
	
	
}
