package at.woodstick.pimutdroid.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.easymock.EasyMockRule;
import org.easymock.Mock;
import org.gradle.api.GradleException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import at.woodstick.pimutdroid.configuration.DevicesOptions;
import at.woodstick.pimutdroid.test.assertion.DeviceAssert;

public class DeviceProviderTest {

	private static final String ANDROID_SERIAL_NUMBER_5554 = "emulator-5554";
	private static final String ANDROID_SERIAL_NUMBER_5556 = "emulator-5556";
	private static final String ANDROID_SERIAL_NUMBER_5557 = "emulator-5557";
	
	private static final Device DEVICE_EMULATOR_5554 = new Device(ANDROID_SERIAL_NUMBER_5554, "sdk_gphone_x86", "Android_SDK_built_for_x86", "generic_x86");
	private static final Device DEVICE_EMULATOR_5556 = new Device(ANDROID_SERIAL_NUMBER_5556, "sdk_gphone_x86", "Android_SDK_built_for_x86", "generic_x86");
	
	private Map<String, Device> devicesMap;
	
	@Rule
	public EasyMockRule mockRule = new EasyMockRule(this);
	
	private DeviceProvider unitUnderTest;
	
	@Mock
	private DeviceLister deviceLister;
	
	@Mock
	private AndroidSerialProvider serialProvider;
	
	private DevicesOptions options;
	
	private Set<String> serialNumbers;
	
	@Before
	public void setUp() {
		serialNumbers = new HashSet<>();
		
		options = new DevicesOptions();
		options.setIgnoreAndroidSerial(false);
		options.setParallelExecution(false);
		options.setSerialNumbers(new HashSet<>());
		
		devicesMap = new HashMap<>();
		
		unitUnderTest = new DeviceProvider(options, deviceLister, serialProvider);
	}

	@After
	public void tearDown() {
		serialNumbers.clear();
		serialNumbers = null;
		devicesMap.clear();
		devicesMap = null;
		options = null;
		unitUnderTest = null;
	}

	@Test
	public void constructor_notNull() {
		assertThat(unitUnderTest).isNotNull();
	}

	@Test
	public void getDevice_fromAndroidSerial_singleDevice_returnsCorrectDeviceFromAndroidSerial() {
		final String expectedSerialNumber = ANDROID_SERIAL_NUMBER_5554;
		
		addDeviceToMap(DEVICE_EMULATOR_5554);
		
		expectRetrieveDevices();
		expectAndroidSerial(expectedSerialNumber);
		expectedDeviceToBeReturned(expectedSerialNumber, DEVICE_EMULATOR_5554);
		
		replay( deviceLister, serialProvider );
		
		Device device = unitUnderTest.getDevice();
		
		DeviceAssert.assertThat(device).isDevice(DEVICE_EMULATOR_5554);
		
		verify( deviceLister, serialProvider );
	}
	
	@Test(expected = GradleException.class)
	public void getDevice_fromAndroidSerial_noDeviceConnected_throwException() {
		final String expectedSerialNumber = ANDROID_SERIAL_NUMBER_5554;
		
		expectRetrieveDevices();
		expectAndroidSerial(expectedSerialNumber);
		expectedNoDevice(expectedSerialNumber);
		
		replay( deviceLister, serialProvider );
		
		unitUnderTest.getDevice();
		
		verify( deviceLister, serialProvider );
	}
	
	@Test
	public void getDevice_ignoreAndroidSerial_singleDevice_returnsFirstDevice() {
		addDeviceToMap(DEVICE_EMULATOR_5554);
		addDeviceToMap(DEVICE_EMULATOR_5556);

		options.setIgnoreAndroidSerial(true);
		
		expectRetrieveDevices();
		expect( deviceLister.getFirstDevice() ).andReturn(DEVICE_EMULATOR_5556).once();
		
		replay( deviceLister, serialProvider );
		
		Device device = unitUnderTest.getDevice();
		
		DeviceAssert.assertThat(device).isDevice(DEVICE_EMULATOR_5556);
		
		verify( deviceLister, serialProvider );
	}
	
	@Test
	public void getDevice_noAndroidSerial_multipleDevices_returnsFirstDevice() {
		addDeviceToMap(DEVICE_EMULATOR_5554);
		addDeviceToMap(DEVICE_EMULATOR_5556);
		
		expectRetrieveDevices();
		expectNoAndroidSerial();
		expect( deviceLister.getFirstDevice() ).andReturn(DEVICE_EMULATOR_5556).once();
		
		replay( deviceLister, serialProvider );
		
		Device device = unitUnderTest.getDevice();
		
		DeviceAssert.assertThat(device).isDevice(DEVICE_EMULATOR_5556);
		
		verify( deviceLister, serialProvider );
	}
	
	@Test
	public void getDevice_noAndroidSerial_multipleDevicesAndConfiguredDevice_returnConfiguredDevice() {
		addDeviceToMap(DEVICE_EMULATOR_5554);
		addDeviceToMap(DEVICE_EMULATOR_5556);
		
		serialNumbers.add(ANDROID_SERIAL_NUMBER_5554);
		options.setSerialNumbers(serialNumbers);
		
		expectRetrieveDevices();
		expectNoAndroidSerial();
		expectedDeviceToBeReturned(ANDROID_SERIAL_NUMBER_5554, DEVICE_EMULATOR_5554);
		
		replay( deviceLister, serialProvider );
		
		Device device = unitUnderTest.getDevice();
		
		DeviceAssert.assertThat(device).isDevice(DEVICE_EMULATOR_5554);
		
		verify( deviceLister, serialProvider );
	}
	
	@Test(expected = PimutdroidException.class)
	public void getDevice_noAndroidSerial_multipleDevicesAndConfiguredDevice_throwException() {
		addDeviceToMap(DEVICE_EMULATOR_5554);
		addDeviceToMap(DEVICE_EMULATOR_5556);
		
		serialNumbers.add(ANDROID_SERIAL_NUMBER_5557);
		options.setSerialNumbers(serialNumbers);

		expectRetrieveDevices();
		expectNoAndroidSerial();
		expectedNoDevice(ANDROID_SERIAL_NUMBER_5557);
		
		replay( deviceLister, serialProvider );
		
		Device device = unitUnderTest.getDevice();
		
		DeviceAssert.assertThat(device).isDevice(DEVICE_EMULATOR_5554);
		
		verify( deviceLister, serialProvider );
	}
	
	@Test
	public void getDevices_noParallelExecution_androidSerialSet_returnDeviceFromAndroidSerial() {
		final String expectedSerialNumber = ANDROID_SERIAL_NUMBER_5554;
		
		addDeviceToMap(DEVICE_EMULATOR_5554);
		addDeviceToMap(DEVICE_EMULATOR_5556);
		
		expectRetrieveDevices();
		expectAndroidSerial(expectedSerialNumber);
		expectedDeviceToBeReturned(expectedSerialNumber, DEVICE_EMULATOR_5554);
		
		replay( deviceLister, serialProvider );
		
		List<Device> devices = unitUnderTest.getDevices();
		
		assertThat(devices).isNotNull().isNotEmpty().hasSize(1);
		
		Device actualDevice = devices.get(0);
		DeviceAssert.assertThat(actualDevice).isDevice(DEVICE_EMULATOR_5554);
		
		verify( deviceLister, serialProvider );
	}
	
	@Test
	public void getDevices_androidSerialNotIgnoredAndParallelExecutionAndConfiguredDevices_androidSerialSet_returnDeviceFromAndroidSerial() {
		final String expectedSerialNumber = ANDROID_SERIAL_NUMBER_5554;
		
		addDeviceToMap(DEVICE_EMULATOR_5554);
		addDeviceToMap(DEVICE_EMULATOR_5556);
		
		serialNumbers.add(ANDROID_SERIAL_NUMBER_5554);
		serialNumbers.add(ANDROID_SERIAL_NUMBER_5556);
		
		options.setParallelExecution(true);
		options.setSerialNumbers(serialNumbers);
		
		expectRetrieveDevices();
		expectAndroidSerial(expectedSerialNumber);
		expectedDeviceToBeReturned(expectedSerialNumber, DEVICE_EMULATOR_5554);
		
		replay( deviceLister, serialProvider );
		
		List<Device> devices = unitUnderTest.getDevices();
		
		assertThat(devices).isNotNull().isNotEmpty().hasSize(1);
		
		Device actualDevice = devices.get(0);
		DeviceAssert.assertThat(actualDevice).isDevice(DEVICE_EMULATOR_5554);
		
		verify( deviceLister, serialProvider );
	}
	
	@Test
	public void getDevices_androidSerialIgnoredAndParallelExecutionAndNoConfiguredDevices_returnAvailableDevices() {
		addDeviceToMap(DEVICE_EMULATOR_5554);
		addDeviceToMap(DEVICE_EMULATOR_5556);
		
		options.setIgnoreAndroidSerial(true);
		options.setParallelExecution(true);
		options.setSerialNumbers(serialNumbers);
		
		expectRetrieveDevices();
		expect( deviceLister.getStoredDeviceList() ).andReturn(devicesMap.values()).once();
		
		replay( deviceLister, serialProvider );
		
		List<Device> devices = unitUnderTest.getDevices();
		
		assertThat(devices).isNotNull().isNotEmpty().hasSize(2);
		
		DeviceAssert.assertThat(devices.get(0)).isDevice(DEVICE_EMULATOR_5556);
		DeviceAssert.assertThat(devices.get(1)).isDevice(DEVICE_EMULATOR_5554);
		
		verify( deviceLister, serialProvider );
	}
	
	@Test
	public void getDevices_androidSerialIgnoredAndParallelExecutionAndConfiguredDevices_returnConfiguredDevices() {
		addDeviceToMap(DEVICE_EMULATOR_5554);
		addDeviceToMap(DEVICE_EMULATOR_5556);
		
		serialNumbers.add(ANDROID_SERIAL_NUMBER_5554);
		serialNumbers.add(ANDROID_SERIAL_NUMBER_5556);
		
		options.setIgnoreAndroidSerial(true);
		options.setParallelExecution(true);
		options.setSerialNumbers(serialNumbers);
		
		expectRetrieveDevices();
		expectedDeviceToBeReturned(ANDROID_SERIAL_NUMBER_5554, DEVICE_EMULATOR_5554);
		expectedDeviceToBeReturned(ANDROID_SERIAL_NUMBER_5556, DEVICE_EMULATOR_5556);
		
		replay( deviceLister, serialProvider );
		
		List<Device> devices = unitUnderTest.getDevices();
		
		assertThat(devices).isNotNull().isNotEmpty().hasSize(2);
		
		DeviceAssert.assertThat(devices.get(0)).isDevice(DEVICE_EMULATOR_5556);
		DeviceAssert.assertThat(devices.get(1)).isDevice(DEVICE_EMULATOR_5554);
		
		verify( deviceLister, serialProvider );
	}
	
	@Test
	public void getDevices_androidSerialIgnoredAndParallelExecutionAndConfiguredDevices_oneConfiguredDeviceNotAvailable_returnOtherConfiguredDevices() {
		addDeviceToMap(DEVICE_EMULATOR_5554);
		addDeviceToMap(DEVICE_EMULATOR_5556);
		
		serialNumbers.add(ANDROID_SERIAL_NUMBER_5554);
		serialNumbers.add(ANDROID_SERIAL_NUMBER_5556);
		serialNumbers.add(ANDROID_SERIAL_NUMBER_5557);
		
		options.setIgnoreAndroidSerial(true);
		options.setParallelExecution(true);
		options.setSerialNumbers(serialNumbers);
		
		expectRetrieveDevices();
		expectedDeviceToBeReturned(ANDROID_SERIAL_NUMBER_5554, DEVICE_EMULATOR_5554);
		expectedDeviceToBeReturned(ANDROID_SERIAL_NUMBER_5556, DEVICE_EMULATOR_5556);
		expectedNoDevice(ANDROID_SERIAL_NUMBER_5557);
		
		replay( deviceLister, serialProvider );
		
		List<Device> devices = unitUnderTest.getDevices();
		
		assertThat(devices).isNotNull().isNotEmpty().hasSize(2);
		
		DeviceAssert.assertThat(devices.get(0)).isDevice(DEVICE_EMULATOR_5556);
		DeviceAssert.assertThat(devices.get(1)).isDevice(DEVICE_EMULATOR_5554);
		
		verify( deviceLister, serialProvider );
	}
	
	@Test
	public void getDevices_androidSerialIgnoredAndNoParallelExecutionAndConfiguredDevices_returnFirstConfiguredDevice() {
		addDeviceToMap(DEVICE_EMULATOR_5554);
		addDeviceToMap(DEVICE_EMULATOR_5556);
		
		serialNumbers.add(ANDROID_SERIAL_NUMBER_5554);
		serialNumbers.add(ANDROID_SERIAL_NUMBER_5556);
		
		options.setIgnoreAndroidSerial(true);
		options.setParallelExecution(false);
		options.setSerialNumbers(serialNumbers);
		
		expectRetrieveDevices();
		expectedDeviceToBeReturned(ANDROID_SERIAL_NUMBER_5556, DEVICE_EMULATOR_5556);
		
		replay( deviceLister, serialProvider );
		
		List<Device> devices = unitUnderTest.getDevices();
		
		assertThat(devices).isNotNull().isNotEmpty().hasSize(1);
		
		DeviceAssert.assertThat(devices.get(0)).isDevice(DEVICE_EMULATOR_5556);
		
		verify( deviceLister, serialProvider );
	}
	
	@Test
	public void getDevices_androidSerialIgnoredAndNoParallelExecutionAndNoConfiguredDevices_returnFirstConfiguredDevice() {
		addDeviceToMap(DEVICE_EMULATOR_5554);
		addDeviceToMap(DEVICE_EMULATOR_5556);
		
		options.setIgnoreAndroidSerial(true);
		options.setParallelExecution(false);
		
		expectRetrieveDevices();
		expect( deviceLister.getFirstDevice() ).andReturn(DEVICE_EMULATOR_5556).once();
		
		replay( deviceLister, serialProvider );
		
		List<Device> devices = unitUnderTest.getDevices();
		
		assertThat(devices).isNotNull().isNotEmpty().hasSize(1);
		
		DeviceAssert.assertThat(devices.get(0)).isDevice(DEVICE_EMULATOR_5556);
		
		verify( deviceLister, serialProvider );
	}
	
	// ########################################################################
	
	protected void addDeviceToMap(Device device) {
		devicesMap.put(device.getId(), device);
	}
	
	protected void expectRetrieveDevices() {
		expect( deviceLister.retrieveDevices() ).andReturn(devicesMap).once();
		expect( deviceLister.noDevicesConnected() ).andReturn(devicesMap.isEmpty()).once();
	}
	
	protected void expectedNoDevice(String expectedSerialNumber) {
		expect( deviceLister.hasDevice(expectedSerialNumber) ).andReturn(false).once();
	}
	
	protected void expectedDeviceToBeReturned(String expectedSerialNumber, Device device) {
		expect( deviceLister.hasDevice(expectedSerialNumber) ).andReturn(true).once();
		expect( deviceLister.getDevice(expectedSerialNumber) ).andReturn(device).once();
	}
	
	protected void expectNoAndroidSerial() {
		expectAndroidSerial(null);
	}
	
	protected void expectAndroidSerial(String serialNumber) {
		boolean hasSerial = serialNumber != null;
		
		expect( serialProvider.hasAndroidSerial() ).andReturn(hasSerial).once();
		
		if(hasSerial) {
			expect( serialProvider.getAndroidSerial() ).andReturn(serialNumber).once();
		}
	}
}
