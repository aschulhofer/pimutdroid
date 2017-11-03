package at.woodstick.pimutdroid;

import com.android.dx.dex.file.FieldAnnotationStruct

import info.solidsoft.gradle.pitest.PitestPluginExtension;

class PimutdroidPluginExtension {
	PitestPluginExtension pitest;

	String packageDir;
	String mutantsDir;
	Boolean outputMutateAll;
	Boolean outputMutantCreation;
	Integer maxFirstMutants;
	
	String outputDir;
	
//	File outputDir;
//	
//	void setOutputDir(String outputDirPath) {
//		this.outputDir = new File(outputDirPath);
//	}
}
