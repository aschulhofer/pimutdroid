package at.woodstick.pimutdroid.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.EasyMockRule;
import org.easymock.Mock;
import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.file.CopySpec;
import org.gradle.api.tasks.WorkResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class AppClassFilesTest {

	private static final Path ORIGINAL_CLASSES_DIR_PATH = Paths.get("original", "path");
	private static final Path BACKUP_CLASSES_DIR_PATH = Paths.get("backup", "path");
	
	private static final String ORIGINAL_CLASSES_DIR_PATH_STRING = ORIGINAL_CLASSES_DIR_PATH.toString();
	private static final String BACKUP_CLASSES_DIR_PATH_STRING = BACKUP_CLASSES_DIR_PATH.toString();
	
	@Rule
	public EasyMockRule mockRule = new EasyMockRule(this);
	
	private AppClassFiles unitUnderTest;
	
	@Mock
	private Project project;
	
	@Mock
	private CopySpec copySpec;
	
	@Mock
	private WorkResult workResult;
	
	private Capture<Action<CopySpec>> copySpecActionCapture;
	
	@Before
	public void setUp() {
		copySpecActionCapture = EasyMock.newCapture();
		unitUnderTest = new AppClassFiles(project, ORIGINAL_CLASSES_DIR_PATH, BACKUP_CLASSES_DIR_PATH);
	}
	
	@After
	public void tearDown() {
		unitUnderTest = null;
	}
	
	@Test
	public void constructor_dirsAsPaths_notNull() {
		AppClassFiles uut = new AppClassFiles(project, ORIGINAL_CLASSES_DIR_PATH, BACKUP_CLASSES_DIR_PATH);
		assertThat(uut).isNotNull();
	}

	@Test
	public void constructor_dirsAsStrings_notNull() {
		AppClassFiles uut = new AppClassFiles(project, ORIGINAL_CLASSES_DIR_PATH_STRING, BACKUP_CLASSES_DIR_PATH_STRING);
		assertThat(uut).isNotNull();
	}

	@Test
	public void backup_copyOnCorrectDirs_didWork_noError() {
		expectCopyFromInto(ORIGINAL_CLASSES_DIR_PATH_STRING, BACKUP_CLASSES_DIR_PATH_STRING);
		expectWorked();
		execute(unitUnderTest::backup);
	}
	
	@Test(expected = GradleException.class)
	public void backup_copyOnCorrectDirs_didNotWork_throwsException() {
		expectCopyFromInto(ORIGINAL_CLASSES_DIR_PATH_STRING, BACKUP_CLASSES_DIR_PATH_STRING);
		expectFailed();
		execute(unitUnderTest::backup);
	}
	
	@Test
	public void restore_copyOnCorrectDirs_didWork_noError() {
		expectCopyFromInto(BACKUP_CLASSES_DIR_PATH_STRING, ORIGINAL_CLASSES_DIR_PATH_STRING);
		expectWorked();
		execute(unitUnderTest::restore);
	}
	
	@Test(expected = GradleException.class)
	public void restore_copyOnCorrectDirs_didNotWork_throwsException() {
		expectCopyFromInto(BACKUP_CLASSES_DIR_PATH_STRING, ORIGINAL_CLASSES_DIR_PATH_STRING);
		expectFailed();
		execute(unitUnderTest::restore);
	}
	
	// ########################################################################
	
	protected void expectCopyFromInto(String from, String into) {
		expect( project.copy(EasyMock.capture(copySpecActionCapture)) ).andReturn(workResult).once();
		expect( copySpec.from(from) ).andReturn(copySpec);
		expect( copySpec.into(into) ).andReturn(copySpec);
	}
	
	protected void expectWorked() {
		expect( workResult.getDidWork() ).andReturn(true).once();
	}
	
	protected void expectFailed() {
		expect( workResult.getDidWork() ).andReturn(false).once();
	}
	
	protected void execute(Runnable method) {
		replay(project, copySpec, workResult);
				
		method.run();
		
		Action<CopySpec> csa = copySpecActionCapture.getValue();
		csa.execute(copySpec);
		
		verify(project, copySpec, workResult);
	}
}
