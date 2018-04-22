package at.woodstick.pimutdroid.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import at.woodstick.pimutdroid.test.helper.TestHelper;

public class AdbCommandFactoryTest {

	private static final File ADB_EXECUTEABLE = Paths.get("path/to/adb/executeable").toFile();
	private static final String ADB_DEVICE_ID_PARAM = "-s";
	private static final String DEVICE_ID = "emulator-5556";
	
	private AdbCommandFactory unitUnderTest;
	
	@Before
	public void setUp() {
		unitUnderTest = AdbCommandFactory.newFactory(ADB_EXECUTEABLE);
	}
	
	@Test
	public void constructor_notNull() {
		assertThat(new AdbCommandFactory(ADB_EXECUTEABLE)).isNotNull();
	}
	
	@Test
	public void newFactory_staticConstructor_notNull() {
		assertThat(AdbCommandFactory.newFactory(ADB_EXECUTEABLE)).isNotNull();
	}
	
	@Test
	public void installReplaceApk_createCommand_correctCommandListInCommand() {
		String apkPath = "path/to/apk";
		
		ConsoleCommand command = unitUnderTest.installReplaceApk(DEVICE_ID, apkPath);
		
		assertThat(command).isNotNull();
		assertThat(command.getCommandList()).containsExactly(
			ADB_EXECUTEABLE,
			ADB_DEVICE_ID_PARAM,
			DEVICE_ID,
			"install",
			"-r",
			apkPath
		);
	}
	
	@Test
	public void runTests_createCommand_correctCommandListInCommand() {
		List<String> testOptionList = TestHelper.asList("-e", "class", "at.woodstick.Test", "-e", "listener", "at.woodstick.MyListener");
		String testPackage = "at.woodstick.mysampleapplication.test";
		String runner = "runner";
		
		ConsoleCommand command = unitUnderTest.runTests(DEVICE_ID, testOptionList, testPackage, runner);
		
		assertThat(command).isNotNull();
		assertThat(command.getCommandList()).containsExactly(
			ADB_EXECUTEABLE,
			ADB_DEVICE_ID_PARAM,
			DEVICE_ID,
			"shell", "am", "instrument",
			testOptionList,
			"-w",
			testPackage + "/" + runner
		);
	}
	
	@Test
	public void pull_createCommand_correctCommandListInCommand() {
		String remotePath = "path/to/device/file";
		String localPath = "C:\\path\\to\\store\\local";
		
		ConsoleCommand command = unitUnderTest.pull(DEVICE_ID, remotePath, localPath);
		
		assertThat(command).isNotNull();
		assertThat(command.getCommandList()).containsExactly(
			ADB_EXECUTEABLE,
			ADB_DEVICE_ID_PARAM,
			DEVICE_ID,
			"pull",
			remotePath,
			localPath
		);
	}
	
	@Test
	public void remove_createCommand_correctCommandListInCommand() {
		String remotePath = "path/to/device/file/report-0.xml";
		
		ConsoleCommand command = unitUnderTest.remove(DEVICE_ID, remotePath);
		
		assertThat(command).isNotNull();
		assertThat(command.getCommandList()).containsExactly(
			ADB_EXECUTEABLE,
			ADB_DEVICE_ID_PARAM,
			DEVICE_ID,
			"shell", "rm",
			remotePath
		);
	}
	
}
