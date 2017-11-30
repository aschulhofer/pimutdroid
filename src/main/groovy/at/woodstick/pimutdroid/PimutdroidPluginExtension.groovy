package at.woodstick.pimutdroid;

import java.util.Set

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
	
	public PimutdroidPluginExtension() {
		
	}
	
//	File outputDir;
//	
//	void setOutputDir(String outputDirPath) {
//		this.outputDir = new File(outputDirPath);
//	}
}
