package at.woodstick.pimutdroid.internal;

import java.util.Collection

import javax.management.InstanceAlreadyExistsException

import org.gradle.api.GradleException
import org.gradle.api.Project

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.api.ApplicationVariant

import at.woodstick.pimutdroid.PimutdroidBasePlugin
import at.woodstick.pimutdroid.PimutdroidPlugin
import at.woodstick.pimutdroid.PimutdroidPluginExtension;
import groovy.transform.CompileStatic
import info.solidsoft.gradle.pitest.PitestPluginExtension

@CompileStatic
public class DefaultExtensionValuesCheck implements ExtensionValuesCheck {

	private String projectName;
	private File buildDir;
	private File reportsDir;
	private PimutdroidPluginExtension extension;
	private BaseExtension androidExtension;
	private PitestPluginExtension pitestExtension;
	private Collection<AndroidVariant> variants;
	
	public DefaultExtensionValuesCheck(String projectName, File buildDir, File reportsDir, PimutdroidPluginExtension extension,
			BaseExtension androidExtension, PitestPluginExtension pitestExtension, Collection<AndroidVariant> variants) {
		this.projectName = projectName;
		this.buildDir = buildDir;
		this.reportsDir = reportsDir;
		this.extension = extension;
		this.androidExtension = androidExtension;
		this.pitestExtension = pitestExtension;
		this.variants = variants;
	}

	@Override
	public void checkAndSetValues() {
		
		
		
		if(extension.applicationIdSuffix == null) {
			extension.applicationIdSuffix = androidExtension.defaultConfig.applicationIdSuffix ?: null;
		}
		
		if(extension.testBuildType == null) {
			extension.testBuildType = androidExtension.testBuildType;
		}
		
		if(extension.productFlavor == null) {
			extension.productFlavor = androidExtension.getProductFlavors().isEmpty() ? "" : androidExtension.getProductFlavors().getAsMap().firstKey();
		}
		
		Optional<String> flavor = extension.productFlavor.isEmpty() ? (Optional<String>)Optional.empty() : Optional.of(extension.productFlavor);
		Optional<AndroidVariant> foundVariant = getVariant(extension.testBuildType, flavor);
		
		if(!foundVariant.isPresent()) {
			throw new GradleException("Can't retrieve android variant for '${extension.testBuildType}' and '${extension.productFlavor}'");
		}
		
		AndroidVariant variant = foundVariant.get();
		
		String mutantClassesFlavorBuildDirName = extension.productFlavor ? (extension.productFlavor + extension.testBuildType.capitalize()) : extension.testBuildType;
		String flavorBuildTypeApkName = extension.productFlavor ? (extension.productFlavor + "-" + extension.testBuildType) : extension.testBuildType;
		String flavorBuildTypePath = variant.getDirName();
		
		if(extension.applicationId == null) {
			extension.applicationId = variant.getApplicationId();
		}
		
		if(extension.testApplicationId == null) {
			extension.testApplicationId = (androidExtension.defaultConfig.testApplicationId ?: "${extension.applicationId}.test");
		}
		
		if(extension.applicationPackage == null) {
			extension.applicationPackage = extension.applicationId
		}
		
		if(extension.packageDir == null) {
			extension.packageDir = extension.applicationPackage.replaceAll("\\.", "/")
		}
		
		if(extension.mutantClassesDir == null) {
			extension.mutantClassesDir = "${pitestExtension.reportDir}/${mutantClassesFlavorBuildDirName}";
		}
		
		if(extension.instrumentationTestOptions.runner == null) {
			final String androidConfigRunner = androidExtension.defaultConfig.testInstrumentationRunner;
			if(androidConfigRunner == null) {
				extension.instrumentationTestOptions.runner = PimutdroidBasePlugin.RUNNER;
			}
			else {
				extension.instrumentationTestOptions.runner = androidConfigRunner;
			}
		}
		
		if(extension.instrumentationTestOptions.targetMutants == null || extension.instrumentationTestOptions.targetMutants.empty) {
			extension.instrumentationTestOptions.targetMutants = [extension.applicationPackage + ".*"].toSet();
		}
		
		if(extension.outputDir == null) {
			extension.outputDir = "${buildDir}/mutation";
		}
		
		if(extension.mutantResultRootDir == null) {
			extension.mutantResultRootDir = "${extension.outputDir}/mutants"
		}
		
		if(extension.appResultRootDir == null) {
			extension.appResultRootDir = "${extension.outputDir}/app/${flavorBuildTypePath}"
		}
		
		if(extension.classFilesDir == null) {
			extension.classFilesDir = "${buildDir}/intermediates/classes/${flavorBuildTypePath}"
		}
		
		if(extension.muidProperty == null) {
			extension.muidProperty = PimutdroidBasePlugin.PROPERTY_NAME_MUID;
		}
		
		if(extension.apkAppOutputRootDir == null) {
			extension.apkAppOutputRootDir = "${buildDir}/outputs/apk/${flavorBuildTypePath}";
		}
		
		if(extension.apkTestOutputRootDir == null) {
			extension.apkTestOutputRootDir = "${buildDir}/outputs/apk/androidTest/${flavorBuildTypePath}";
		}
		
		if(extension.classFilesBackupDir == null) {
			extension.classFilesBackupDir = "${extension.appResultRootDir}/backup/classes";
		}
		
		if(extension.mutantReportRootDir == null) {
			extension.mutantReportRootDir = "${reportsDir}/mutation";
		}
		
		if(extension.apkName == null) {
			extension.apkName = "${projectName}-${flavorBuildTypeApkName}.apk";
		}
		
		if(extension.testApkName == null) {
			extension.testApkName = "${projectName}-${flavorBuildTypeApkName}-androidTest.apk";
		}
		
		if(extension.expectedTestResultFilename == null) {
			extension.expectedTestResultFilename = "${projectName.toLowerCase()}-expected-test-result.xml"
		}
		
		if(extension.mutantTestResultFilename == null) {
			extension.mutantTestResultFilename = "${projectName.toLowerCase()}-mutant-test-result.xml"
		}
	}
	
	protected Optional<AndroidVariant> getVariant(String buildType, Optional<String> flavor) {
		for(AndroidVariant variant : variants) {
			
			if(flavor.isPresent()) {
				if(variant.getBuildTypeName().equals(buildType) && variant.getFlavorName().equals(flavor.get())) {
					return Optional.of(variant);
				}
			} else {
				if(variant.getBuildTypeName().equals(buildType)) {
					return Optional.of(variant);
				}
			}
			
		}
		
		return Optional.empty();
	}
}
