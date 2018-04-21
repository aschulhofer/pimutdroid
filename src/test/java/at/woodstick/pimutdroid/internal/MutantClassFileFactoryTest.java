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

public class MutantClassFileFactoryTest {
	private MutantClassFileFactory unitUnderTest;
	
	@Before
	public void setUp() throws IOException {
		unitUnderTest = new MutantClassFileFactory(TEST_RESOURCES_INTERAL_PACKAGE_PATH);
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
	public void fromMarkerFile_nullAsParameter_throwsNullPointerException() {
		unitUnderTest.fromMarkerFile(null);
	}
	
	@Test
	public void fromMarkerFile_normalClassMarkerFile_correctClassFileCreated() {
		String mutantPackage = "at.woodstick.test";
		String mutantNumber = "0";
		String mutantClass = "DisplayMessageActivity";
		String markerFileName = getMarkerFileName(mutantClass, mutantNumber);
		String classFileName = getMutantClassFileName(mutantPackage, mutantClass);
		
		Path expectedClassPackageDir = Paths.get(mutantPackage);
		Path expectedClassRootDirPath = TEST_RESOURCES_INTERAL_PACKAGE_PATH
				.resolve(expectedClassPackageDir)
				.resolve(mutantClass)
				.resolve("mutants")
				.resolve(mutantNumber);
		
		File markerFile = expectedClassRootDirPath.resolve(markerFileName).toFile();
		File expectedClassFile = expectedClassRootDirPath.resolve(classFileName).toFile();
		
		MutantMarkerFile mutantMarkerFile = new MutantMarkerFile(markerFile, mutantNumber, mutantClass, markerFileName);
		
		MutantClassFile mutantClassFile = unitUnderTest.fromMarkerFile(mutantMarkerFile);
		
		assertThat(mutantClassFile).isNotNull();
		assertThat(mutantClassFile.getClassName()).isEqualTo("DisplayMessageActivity");
		assertThat(mutantClassFile.getFile()).isEqualTo(expectedClassFile);
		assertThat(mutantClassFile.getRelativePackageClassDirPath()).isEqualTo(expectedClassPackageDir);
		assertThat(mutantClassFile.isInnerClass()).isFalse();
	}
	
	@Test
	public void fromMarkerFile_innerClassMarkerFile_correctClassFileCreated() {
		
		String mutantPackage = "at.woodstick.test";
		String mutantNumber = "0";
		String mutantParentClass = "DisplayMessageActivity";
		String mutantClass = "DisplayMessageActivity$1";
		String markerFileName = getMarkerFileName(mutantClass, mutantNumber);
		String classFileName = getMutantClassFileName(mutantPackage, mutantClass);
		
		Path expectedClassPackageDir = Paths.get(mutantPackage);
		Path expectedInnerClassRootDirPath = TEST_RESOURCES_INTERAL_PACKAGE_PATH
				.resolve(expectedClassPackageDir)
				.resolve(mutantParentClass)
				.resolve(mutantClass)
				.resolve("mutants")
				.resolve(mutantNumber);
		
		File markerFile = expectedInnerClassRootDirPath.resolve(markerFileName).toFile();
		File expectedClassFile = expectedInnerClassRootDirPath.resolve(classFileName).toFile();
		
		MutantMarkerFile mutantMarkerFile = new MutantMarkerFile(markerFile, mutantNumber, mutantClass, markerFileName);
		
		MutantClassFile mutantClassFile = unitUnderTest.fromMarkerFile(mutantMarkerFile);
		
		assertThat(mutantClassFile).isNotNull();
		assertThat(mutantClassFile.getClassName()).isEqualTo(mutantClass);
		assertThat(mutantClassFile.getFile()).isEqualTo(expectedClassFile);
		assertThat(mutantClassFile.getRelativePackageClassDirPath()).isEqualTo(expectedClassPackageDir);
		assertThat(mutantClassFile.isInnerClass()).isTrue();
	}
}
