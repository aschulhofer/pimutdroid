package at.woodstick.pimutdroid.internal;

import static at.woodstick.pimutdroid.test.helper.TestHelper.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMockRule;
import org.easymock.Mock;
import org.junit.Rule;
import org.junit.Test;

import at.woodstick.pimutdroid.test.helper.TestHelper;

public class AdbDeviceCommandBridgeTest {

	private static final String DEVICE_ID = "emulator-5554";
	private static final Device DEVICE = new Device(DEVICE_ID); 
	private static final String DUMMY_COMMAND_OUTPUT = "DUMMY_COMMAND_OUTPUT";
	
	private static final String ANDROID_ARGUMENT_LISTENER = "listener";
	private static final String ANDROID_ARGUMENT_CLASS    = "class";
	
	@Rule
	public EasyMockRule mockRule = new EasyMockRule(this);
	
	@Mock
	private AdbCommandFactory commandFactory;
	
	@Mock
	private ConsoleCommand command;
	
	@Test
	public void installOnDevice_noCommandError_installReplaceCommandCreatedAndCalled() {
		assertInstallOnDeviceCommand(true);
	}
	
	@Test
	public void installOnDevice_withCommandError_installReplaceCommandCreatedAndCalled() {
		assertInstallOnDeviceCommand(false);
	}
	
	@Test
	public void removeResultOnDevice_noCommandError_removeCommandCreatedAndCalled() {
		assertRemoveResultOnDeviceCommand(true);
	}
	
	@Test
	public void removeResultOnDevice_withCommandError_removeCommandCreatedAndCalled() {
		assertRemoveResultOnDeviceCommand(false);
	}
	
	@Test
	public void fetchResult_noCommandError_pullCommandCreatedAndCalled() {
		assertFetchResultCommand(true);
	}
	
	@Test
	public void fetchResult_withCommandError_pullCommandCreatedAndCalled() {
		assertFetchResultCommand(false);
	}
	
	@Test
	public void runTests_noCommandError_pullCommandCreatedAndCalled() {
		assertRunTests(true);
	}
	
	@Test
	public void runTests_withCommandError_pullCommandCreatedAndCalled() {
		assertRunTests(false);
	}
	
	// ########################################################################
	
	protected void assertInstallOnDeviceCommand(boolean successfulAdbCommand) {
		String apkPath = "path/to/apk";

		expect( commandFactory.installReplaceApk(DEVICE_ID, apkPath) ).andReturn(command).once();
		expect( command.enableTimeout() ).andReturn(command).once();
		
		expectSuccessfulCommand(successfulAdbCommand);
		
		replay( commandFactory, command );
		
		AdbDeviceCommandBridge unitUnderTest = new AdbDeviceCommandBridge(DEVICE, commandFactory);
		
		boolean result = unitUnderTest.installOnDevice(apkPath);
		
		verify( commandFactory, command );
		
		assertThat(result).isEqualTo(successfulAdbCommand);
	}
	
	protected void assertRemoveResultOnDeviceCommand(boolean successfulAdbCommand) {
		String remotePath = "path/to/remote/file/*.xml";

		expect( commandFactory.remove(DEVICE_ID, remotePath) ).andReturn(command).once();
		
		expectSuccessfulCommand(successfulAdbCommand);
		
		replay( commandFactory, command );
		
		AdbDeviceCommandBridge unitUnderTest = new AdbDeviceCommandBridge(DEVICE, commandFactory);
		
		boolean result = unitUnderTest.removeResultOnDevice(remotePath);
		
		verify( commandFactory, command );
		
		assertThat(result).isEqualTo(successfulAdbCommand);
	}
	
	protected void assertFetchResultCommand(boolean successfulAdbCommand) {
		String remotePath = "path/to/remote/file/*.xml";
		String localPath = "C:\\path\\to\\local\\target\\file.xml";

		expect( commandFactory.pull(DEVICE_ID, remotePath, localPath) ).andReturn(command).once();
		
		expectSuccessfulCommand(successfulAdbCommand);
		
		replay( commandFactory, command );
		
		AdbDeviceCommandBridge unitUnderTest = new AdbDeviceCommandBridge(DEVICE, commandFactory);
		
		boolean result = unitUnderTest.fetchResult(remotePath, localPath);
		
		verify( commandFactory, command );
		
		assertThat(result).isEqualTo(successfulAdbCommand);
	}
	
	protected void assertRunTests(boolean successfulAdbCommand) {
		Map<String, List<String>> testOptions = new HashMap<>();
		testOptions.put(ANDROID_ARGUMENT_LISTENER, asList("at.woodstick.MyListener"));
		testOptions.put(ANDROID_ARGUMENT_CLASS, asList("at.woodstick.Test", "at.woodstick.Test2"));
		
		List<String> expectedTestOptions = TestHelper.mapTestOptionsMapToList(testOptions);
		String testPackage = "at.woodstick.mysampleapplication.test";
		String runner = "runner";

		expect( commandFactory.runTests(DEVICE_ID, expectedTestOptions, testPackage, runner) ).andReturn(command).once();
		
		expectSuccessfulCommand(successfulAdbCommand);
		
		replay( commandFactory, command );
		
		AdbDeviceCommandBridge unitUnderTest = new AdbDeviceCommandBridge(DEVICE, commandFactory);
		
		boolean result = unitUnderTest.runTests(testPackage, runner, testOptions);
		
		verify( commandFactory, command );
		
		assertThat(result).isEqualTo(successfulAdbCommand);
	}
	
	// ########################################################################
	
	protected void expectSuccessfulCommand(boolean expectSuccessful) {
		if(expectSuccessful) {
			expectSuccessfulCommand();
		} else {
			expectFailedCommand();
		}
	}
	
	protected void expectSuccessfulCommand() {
		expect( command.executeGetString() ).andReturn(DUMMY_COMMAND_OUTPUT).once();
		expect( command.getExitValue() ).andReturn(0).once();
		expect( command.hasErrorExitValue() ).andReturn(false).once();
	}
	
	protected void expectFailedCommand() {
		expect( command.executeGetString() ).andReturn(DUMMY_COMMAND_OUTPUT).once();
		expect( command.getExitValue() ).andReturn(1).once();
		expect( command.hasErrorExitValue() ).andReturn(true).once();
	}
}
