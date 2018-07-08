package at.woodstick.pimutdroid.internal;

import static at.woodstick.pimutdroid.test.helper.TestHelper.asList;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMock;
import org.easymock.EasyMockRule;
import org.easymock.Mock;
import org.easymock.MockType;
import org.junit.Rule;
import org.junit.Test;

public class RunTestOnDeviceTest {

	private static final String ANDROID_ARGUMENT_LISTENER = "listener";
	private static final String ANDROID_ARGUMENT_PACKAGE  = "package";
	
	@Rule
	public EasyMockRule easyMockRule = new EasyMockRule(this);
	
	@Mock(type = MockType.STRICT)
	private AdbDeviceCommandBridge deviceBridge;
	
	@Test
	public void run_twoTargetApksAndTestOptions_bridgeCallsInOrderAndParamsCorrect() {
		
		String testListenerClass = "at.woodstick.MyListener";
		
		Map<String, List<String>> testOptions = new HashMap<>();
		testOptions.put(ANDROID_ARGUMENT_LISTENER, asList(testListenerClass));
		testOptions.put(ANDROID_ARGUMENT_PACKAGE, asList("at.woodstick.first", "at.woodstick.second"));
		
		
		String firstApkPath = "path/to/application/apk/1/test.apk";
		String secondApkPath = "path/to/application/apk/2/test.apk";
		
		List<String> appApkPaths = asList(firstApkPath, secondApkPath);
		
		String testApkPath = "path/to/test/apk/test.apk";
		
		String testPackage = "at.woodstick.mysampleapplication.test";
		String appPackage = "at.woodstick.mysampleapplication";
		String runnerClass = "android.support.test.runner.AndroidJUnitRunner";
		String localFilename = "adb-test-result.xml";
		
		String expectedRemotePath = "/storage/emulated/0/Android/data/" + appPackage + "/files";
		String expectedRemoteFilesRemoveBlob = expectedRemotePath + "/*.xml";
		String expectedRemoteResultFile = expectedRemotePath + "/report-0.xml";
		
		EasyMock.expect( deviceBridge.getDeviceId() ).andReturn("emultator-5556").once();
		
		EasyMock.expect( deviceBridge.installTestApkOnDevice(testApkPath) ).andReturn(true).once();

		EasyMock.expect( deviceBridge.removeResultOnDevice(expectedRemoteFilesRemoveBlob) ).andReturn(true).once();
		
		// First apk loop
		EasyMock.expect( deviceBridge.installOnDevice(firstApkPath) ).andReturn(true).once();
		
		EasyMock.expect( deviceBridge.runTests(testPackage, runnerClass, testOptions) ).andReturn(true).once();
		
		String localResultPath = Paths.get(firstApkPath).getParent().resolve(localFilename).toString();
		EasyMock.expect( deviceBridge.fetchResult(expectedRemoteResultFile, localResultPath) ).andReturn(true).once();
		
		EasyMock.expect( deviceBridge.removeResultOnDevice(expectedRemoteFilesRemoveBlob) ).andReturn(true).once();
		
		EasyMock.expect( deviceBridge.clearPackage(testPackage) ).andReturn(true).once();
		
		EasyMock.expect( deviceBridge.clearPackage(appPackage) ).andReturn(true).once();
		
		// Second apk loop
//		EasyMock.expect( deviceBridge.installTestApkOnDevice(testApkPath) ).andReturn(true).once();
		
		EasyMock.expect( deviceBridge.installOnDevice(secondApkPath) ).andReturn(true).once();
		
		EasyMock.expect( deviceBridge.runTests(testPackage, runnerClass, testOptions) ).andReturn(true).once();
		
		localResultPath = Paths.get(secondApkPath).getParent().resolve(localFilename).toString();
		EasyMock.expect( deviceBridge.fetchResult(expectedRemoteResultFile, localResultPath) ).andReturn(true).once();
		
		EasyMock.expect( deviceBridge.removeResultOnDevice(expectedRemoteFilesRemoveBlob) ).andReturn(true).once();
		
		EasyMock.expect( deviceBridge.clearPackage(testPackage) ).andReturn(true).once();
		
		EasyMock.expect( deviceBridge.clearPackage(appPackage) ).andReturn(true).once();
		
		EasyMock.expect( deviceBridge.uninstallFromDevice(appPackage) ).andReturn(true).once();
		
		EasyMock.replay( deviceBridge );
		
		RunTestOnDevice unitUnderTest = new RunTestOnDevice(deviceBridge, testOptions, appApkPaths, testApkPath, testPackage, appPackage, runnerClass, localFilename);
		
		unitUnderTest.run();
		
		EasyMock.verify( deviceBridge );
	}
	
}
