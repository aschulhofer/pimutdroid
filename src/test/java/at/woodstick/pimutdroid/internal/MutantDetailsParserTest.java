package at.woodstick.pimutdroid.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MutantDetailsParserTest {

	private static final String MUTANT_CLASS_ID = "DisplayMessageActivity_0";
	private static final String MUTANT_INNERCLASS_ID = "DisplayMessageActivity$1_0";
	
	private static final String TEST_FILENAME_CLASS 	 = "DisplayMessageActivity_0.mutant-details.txt";
	private static final String TEST_FILENAME_INNERCLASS = "DisplayMessageActivity$1_0.mutant-details.txt";

	private static final Path TEST_SOURCES_BASE_PATH = Paths.get("src/test/resources/at/woodstick/pimutdroid");
	private static final Path TEST_SOURCES_PATH = TEST_SOURCES_BASE_PATH.resolve("internal");
	
	private MutantDetailsParser unitUnderTest;
	
	private File fileWithClass;
	private File fileWithInnerClass;
	
	@Before
	public void setUp() {
		fileWithClass = TEST_SOURCES_PATH.resolve(TEST_FILENAME_CLASS).toFile();
		fileWithInnerClass = TEST_SOURCES_PATH.resolve(TEST_FILENAME_INNERCLASS).toFile();
		
		unitUnderTest = new MutantDetailsParser();
	}
	
	@After
	public void tearDown() {
		unitUnderTest = null;
	}
	
	@Test(expected = IOException.class)
	public void parseFromFile_noneExistingFile_throwIOException() throws IOException {
		unitUnderTest.parseFromFile(
			MUTANT_CLASS_ID, 
			TEST_SOURCES_BASE_PATH.resolve("interal").resolve("doesNotExist.txt").toFile()
		);
	}
	
	@Test
	public void parseFromFile_fileWithClass_allDataCorrect() throws IOException {
		MutantDetails mutantDetails = unitUnderTest.parseFromFile(MUTANT_CLASS_ID, fileWithClass);
	
		assertThat(mutantDetails).isNotNull();
		
		assertThat(mutantDetails.getMuid()).isEqualTo(MUTANT_CLASS_ID);
		
		assertThat(mutantDetails.getClazzPackage()).isEqualTo("at.woodstick.mysampleapplication");
		assertThat(mutantDetails.getClazzName()).isEqualTo("DisplayMessageActivity");
		assertThat(mutantDetails.getClazz()).isEqualTo("at.woodstick.mysampleapplication.DisplayMessageActivity");
		assertThat(mutantDetails.getFilename()).isEqualTo("DisplayMessageActivity.java");
		
		assertThat(mutantDetails.getMutator()).isEqualTo("org.pitest.mutationtest.engine.gregor.mutators.VoidMethodCallMutator");
		assertThat(mutantDetails.getDescription()).isEqualTo("removed call to android/support/v7/app/AppCompatActivity::onCreate");
		
		assertThat(mutantDetails.getLineNumber()).isEqualTo("12");
		assertThat(mutantDetails.getMethod()).isEqualTo("onCreate");
	}
	
	@Test
	public void parseFromFile_fileWithInnerClass_allDataCorrect() throws IOException {
		MutantDetails mutantDetails = unitUnderTest.parseFromFile(MUTANT_INNERCLASS_ID, fileWithInnerClass);
	
		assertThat(mutantDetails).isNotNull();
		
		assertThat(mutantDetails.getMuid()).isEqualTo(MUTANT_INNERCLASS_ID);
		
		assertThat(mutantDetails.getClazzPackage()).isEqualTo("at.woodstick.mysampleapplication");
		assertThat(mutantDetails.getClazzName()).isEqualTo("DisplayMessageActivity$1");
		assertThat(mutantDetails.getClazz()).isEqualTo("at.woodstick.mysampleapplication.DisplayMessageActivity$1");
		assertThat(mutantDetails.getFilename()).isEqualTo("DisplayMessageActivity.java");
		
		assertThat(mutantDetails.getMutator()).isEqualTo("org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator");
		assertThat(mutantDetails.getDescription()).isEqualTo("mutated return of Object value for at/woodstick/mysampleapplication/DisplayMessageActivity$1::decorate to ( if (x != null) null else throw new RuntimeException )");
		
		assertThat(mutantDetails.getLineNumber()).isEqualTo("18");
		assertThat(mutantDetails.getMethod()).isEqualTo("decorate");
	}
	
}
