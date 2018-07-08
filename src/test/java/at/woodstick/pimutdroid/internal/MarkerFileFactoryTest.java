package at.woodstick.pimutdroid.internal;

import static at.woodstick.pimutdroid.test.helper.TestHelper.TEST_RESOURCES_INTERAL_PACKAGE_PATH;
import static at.woodstick.pimutdroid.test.helper.TestHelper.getMarkerFileName;
import static at.woodstick.pimutdroid.test.helper.TestHelper.getMutantClassFileName;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MarkerFileFactoryTest {

	private MarkerFileFactory unitUnderTest;
	
	@Before
	public void setUp() throws IOException {
		unitUnderTest = new MarkerFileFactory();
	}
	
	@After
	public void tearDown() {
		unitUnderTest = null;
	}
	
	@Test
	public void constructor_notNull() {
		assertThat(unitUnderTest).isNotNull();
	}
	
	@Test(expected = NullPointerException.class)
	public void fromClassFile_nullAsParameter_throwsNullPointerException() {
		unitUnderTest.fromClassFile(null);
	}
	
	@Test(expected = NullPointerException.class)
	public void fromMarkerFile_nullAsParameter_throwsNullPointerException() {
		unitUnderTest.fromMarkerFile(null);
	}
	
	@Test
	public void fromMarkerFile_normalClassMarkerFile_correctMarkerFileCreated() {
	
		String mutantPackage = "at.woodstick.test";
		String mutantNumber = "0";
		String mutantClass = "DisplayMessageActivity";
		String markerFileClassName = mutantPackage + "." + mutantClass;
		String markerFileName = getMarkerFileName(markerFileClassName, mutantNumber);
		
		Path expectedClassPackageDir = Paths.get(mutantPackage);
		Path expectedClassRootDirPath = TEST_RESOURCES_INTERAL_PACKAGE_PATH
				.resolve(expectedClassPackageDir)
				.resolve(mutantClass)
				.resolve("mutants")
				.resolve(mutantNumber);
		
		File markerFile = expectedClassRootDirPath.resolve(markerFileName).toFile();
		
		MutantMarkerFile mutantMarkerFile = unitUnderTest.fromMarkerFile(markerFile);
		
		assertThat(mutantMarkerFile).isNotNull();
		assertThat(mutantMarkerFile.getSubId()).isEqualTo(mutantNumber);
		assertThat(mutantMarkerFile.getFileName()).isEqualTo(markerFile.getName());
		assertThat(mutantMarkerFile.getMutantClassName()).isEqualTo(mutantClass);
		assertThat(mutantMarkerFile.getFile()).isEqualTo(markerFile);
	}
	
	@Test
	public void fromMarkerFile_innerClassMarkerFile_correctMarkerFileCreated() {
	
		String mutantPackage = "at.woodstick.test";
		String mutantNumber = "0";
		String mutantParentClass = "DisplayMessageActivity";
		String mutantClass = "DisplayMessageActivity$1";
		String markerFileClassName = mutantPackage + "." + mutantClass;
		String markerFileName = getMarkerFileName(markerFileClassName, mutantNumber);
		
		Path expectedClassPackageDir = Paths.get(mutantPackage);
		Path expectedInnerClassRootDirPath = TEST_RESOURCES_INTERAL_PACKAGE_PATH
				.resolve(expectedClassPackageDir)
				.resolve(mutantParentClass)
				.resolve(mutantClass)
				.resolve("mutants")
				.resolve(mutantNumber);
		
		File markerFile = expectedInnerClassRootDirPath.resolve(markerFileName).toFile();
		
		MutantMarkerFile mutantMarkerFile = unitUnderTest.fromMarkerFile(markerFile);
		
		assertThat(mutantMarkerFile).isNotNull();
		assertThat(mutantMarkerFile.getSubId()).isEqualTo(mutantNumber);
		assertThat(mutantMarkerFile.getFileName()).isEqualTo(markerFile.getName());
		assertThat(mutantMarkerFile.getMutantClassName()).isEqualTo(mutantClass);
		assertThat(mutantMarkerFile.getFile()).isEqualTo(markerFile);
	}
	
	@Test
	public void fromClassFile_normalClassMarkerFile_correctMarkerFileCreated() {
		String mutantPackage = "at.woodstick.test";
		String mutantNumber = "0";
		String mutantClass = "DisplayMessageActivity";
		String mutantClassName = mutantPackage + "." + mutantClass;
		String classFileName = getMutantClassFileName(mutantPackage, mutantClass);
		String markerFileName = getMarkerFileName(mutantClassName, mutantNumber);
		
		Path expectedClassPackageDir = Paths.get(mutantPackage);
		Path expectedClassRootDirPath = TEST_RESOURCES_INTERAL_PACKAGE_PATH
				.resolve(expectedClassPackageDir)
				.resolve(mutantClass)
				.resolve("mutants")
				.resolve(mutantNumber);
		
		File classFile = expectedClassRootDirPath.resolve(classFileName).toFile();
		File markerFile = expectedClassRootDirPath.resolve(markerFileName).toFile();
		
		MutantMarkerFile mutantMarkerFile = unitUnderTest.fromClassFile(classFile);
		
		assertThat(mutantMarkerFile).isNotNull();
		assertThat(mutantMarkerFile.getSubId()).isEqualTo(mutantNumber);
		assertThat(mutantMarkerFile.getFileName()).isEqualTo(markerFileName);
		assertThat(mutantMarkerFile.getMutantClassName()).isEqualTo(mutantClassName);
		assertThat(mutantMarkerFile.getFile()).isEqualTo(markerFile);
	}
	
	@Test
	public void fromClassFile_innerClassMarkerFile_correctMarkerFileCreated() {
		String mutantPackage = "at.woodstick.test";
		String mutantNumber = "0";
		String mutantParentClass = "DisplayMessageActivity";
		String mutantClass = "DisplayMessageActivity$1";
		String mutantClassName = mutantPackage + "." + mutantClass;
		String classFileName = getMutantClassFileName(mutantPackage, mutantClass);
		String markerFileName = getMarkerFileName(mutantClassName, mutantNumber);
		
		Path expectedClassPackageDir = Paths.get(mutantPackage);
		Path expectedInnerClassRootDirPath = TEST_RESOURCES_INTERAL_PACKAGE_PATH
				.resolve(expectedClassPackageDir)
				.resolve(mutantParentClass)
				.resolve(mutantClass)
				.resolve("mutants")
				.resolve(mutantNumber);
		
		File classFile = expectedInnerClassRootDirPath.resolve(classFileName).toFile();
		File markerFile = expectedInnerClassRootDirPath.resolve(markerFileName).toFile();
		
		MutantMarkerFile mutantMarkerFile = unitUnderTest.fromClassFile(classFile);
		
		assertThat(mutantMarkerFile).isNotNull();
		assertThat(mutantMarkerFile.getSubId()).isEqualTo(mutantNumber);
		assertThat(mutantMarkerFile.getFileName()).isEqualTo(markerFileName);
		assertThat(mutantMarkerFile.getMutantClassName()).isEqualTo(mutantClassName);
		assertThat(mutantMarkerFile.getFile()).isEqualTo(markerFile);
	}
}
