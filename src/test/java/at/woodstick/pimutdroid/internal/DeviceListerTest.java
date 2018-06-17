package at.woodstick.pimutdroid.internal;

import static at.woodstick.pimutdroid.test.helper.TestHelper.TEST_RESOURCES_INTERAL_PACKAGE_PATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Map;

import org.assertj.core.api.SoftAssertions;
import org.easymock.EasyMockRule;
import org.easymock.Mock;
import org.easymock.MockType;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import at.woodstick.pimutdroid.test.assertion.DeviceAssert;

public class DeviceListerTest {

	private static final String UNAVAILABLE_DEVICE_ID = "unavailable device id";
	
	/*
	 * Devices are stored as "adb devices -l" output in devices.txt 
	 */
	private static final Device DEVICE_323048cb61c870dd = new Device("323048cb61c870dd", "m0xx", "GT_I9300", "m0");
	private static final Device DEVICE_EMULATOR_5556    = new Device("emulator-5556", "sdk_gphone_x86", "Android_SDK_built_for_x86", "generic_x86");
	private static final Device DEVICE_EMULATOR_5554    = new Device("emulator-5554", "sdk_gphone_x86", "Android_SDK_built_for_x86", "generic_x86");
	
	private static final int NUMBER_DEVICES = 3;
	
	@Rule
	public EasyMockRule mockRule = new EasyMockRule(this);
	
	@Mock(type = MockType.NICE)
	private ListDevicesCommand devicesCommand;
	
	private static String devicesTextString;
	
	private DeviceLister unitUnderTest;
	
	@BeforeClass
	public static void readDevicesText() throws IOException {
		devicesTextString = new String(Files.readAllBytes(TEST_RESOURCES_INTERAL_PACKAGE_PATH.resolve("devices.txt")), StandardCharsets.UTF_8);
	}
	
	@Before
	public void setUp() {
		unitUnderTest = new DeviceLister(devicesCommand);
	}
	
	@Test
	public void constructor_notNull() {
		assertThat(unitUnderTest).isNotNull();
	}
	
	@Test
	public void getFirstDevice_noDevicesRetrieved_returnsNull() {
		assertThat(unitUnderTest.getFirstDevice()).isNull();
	}
	
	@Test
	public void getFirstDevice_devicesRetrieved_returnsCorrectDevice() {
		retrieveDevices(devicesTextString);
		
		Device device = unitUnderTest.getFirstDevice();
		
		assertDevice(device, DEVICE_EMULATOR_5556);
	}
	
	@Test
	public void getDevice_devicesRetrieved_returnsCorrectDevice() {
		retrieveDevices(devicesTextString);
		
		Device device = unitUnderTest.getDevice(DEVICE_EMULATOR_5556.getId());
		
		assertDevice(device, DEVICE_EMULATOR_5556);
	}
	
	@Test
	public void hasDevice_devicesRetrieved_hasRetrievedDevice_returnTrue() {
		retrieveDevices(devicesTextString);
		
		boolean hasDevice = unitUnderTest.hasDevice(DEVICE_EMULATOR_5556.getId());
		
		assertThat(hasDevice).isTrue();
	}
	
	@Test
	public void hasDevice_devicesRetrieved_NotRetrievedDevice_returnFalse() {
		retrieveDevices(devicesTextString);
		
		boolean hasDevice = unitUnderTest.hasDevice(UNAVAILABLE_DEVICE_ID);
		
		assertThat(hasDevice).isFalse();
	}
	
	@Test
	public void noDevicesConnected_devicesRetrievedAndNoDevicesAvailable_isTrue() {
		retrieveDevices("");
		
		assertThat(unitUnderTest.noDevicesConnected()).isTrue();
	}
	
	@Test
	public void hasDevices_devicesRetrievedAndNoDevicesAvailable_isFalse() {
		retrieveDevices("");
		
		assertThat(unitUnderTest.hasDevices()).isFalse();
	}
	
	@Test
	public void noDevicesConnected_devicesRetrievedAndDevicesAvailable_isFalse() {
		retrieveDevices(devicesTextString);
		
		assertThat(unitUnderTest.noDevicesConnected()).isFalse();
	}
	
	@Test
	public void hasDevices_devicesRetrievedAndNoDevicesAvailable_isTrue() {
		retrieveDevices(devicesTextString);
		
		assertThat(unitUnderTest.hasDevices()).isTrue();
	}
	
	@Test
	public void getNumberOfDevices_devicesRetrieved_isThree() {
		retrieveDevices(devicesTextString);
		
		assertThat(unitUnderTest.getNumberOfDevices()).isEqualTo(NUMBER_DEVICES);
	}
	
	@Test
	public void getStoredDeviceList_noDevicesRetrieved_isEmpty() {
		retrieveDevices("");
		
		Collection<Device> devices = unitUnderTest.getStoredDeviceList();
		
		assertThat(devices).isEmpty();
	}
	
	@Test
	public void getStoredDeviceList_devicesRetrieved_hasSizeThree() {
		retrieveDevices(devicesTextString);
		
		Collection<Device> devices = unitUnderTest.getStoredDeviceList();
		
		assertThat(devices).hasSize(NUMBER_DEVICES);
		
		assertThat(devices).extracting(Device::getId).containsExactlyInAnyOrder(
			DEVICE_323048cb61c870dd.getId(), 
			DEVICE_EMULATOR_5556.getId(), 
			DEVICE_EMULATOR_5554.getId()
		);
	}
	
	@Test
	public void retrieveDevices_devicesFromTextfile_allDataCorrect() {
		Map<String, Device> devices = retrieveDevices(devicesTextString);

		assertThat(devices).isNotNull().isNotEmpty().hasSize(NUMBER_DEVICES);
		assertThat(devices).containsKeys(DEVICE_323048cb61c870dd.getId(), DEVICE_EMULATOR_5556.getId(), DEVICE_EMULATOR_5554.getId());
		
		assertDevice(devices, DEVICE_323048cb61c870dd);
		assertDevice(devices, DEVICE_EMULATOR_5556);
		assertDevice(devices, DEVICE_EMULATOR_5554);
	}
	
	@Test
	public void retrieveDevices_devicesFromTextfileDontStore_noDataStored() {
		Map<String, Device> devices = retrieveDevicesDontStore(devicesTextString);
		
		assertThat(devices).isNotNull().isNotEmpty().hasSize(NUMBER_DEVICES);
		assertThat(devices).containsKeys(DEVICE_323048cb61c870dd.getId(), DEVICE_EMULATOR_5556.getId(), DEVICE_EMULATOR_5554.getId());
		
		assertDevice(devices, DEVICE_323048cb61c870dd);
		assertDevice(devices, DEVICE_EMULATOR_5556);
		assertDevice(devices, DEVICE_EMULATOR_5554);
		
		SoftAssertions softAssert = new SoftAssertions();
		
		softAssert.assertThat(unitUnderTest.hasDevices()).isFalse();
		softAssert.assertThat(unitUnderTest.getFirstDevice()).isNull();
		softAssert.assertThat(unitUnderTest.noDevicesConnected()).isTrue();
		softAssert.assertThat(unitUnderTest.getNumberOfDevices()).isZero();
		softAssert.assertThat(unitUnderTest.getStoredDeviceList()).isEmpty();
		
		softAssert.assertThat(unitUnderTest.hasDevice(DEVICE_323048cb61c870dd.getId())).isFalse();
		softAssert.assertThat(unitUnderTest.hasDevice(DEVICE_EMULATOR_5556.getId())).isFalse();
		softAssert.assertThat(unitUnderTest.hasDevice(DEVICE_EMULATOR_5554.getId())).isFalse();
		
		softAssert.assertThat(unitUnderTest.getDevice(DEVICE_323048cb61c870dd.getId())).isNull();
		softAssert.assertThat(unitUnderTest.getDevice(DEVICE_EMULATOR_5556.getId())).isNull();
		softAssert.assertThat(unitUnderTest.getDevice(DEVICE_EMULATOR_5554.getId())).isNull();
		
		softAssert.assertAll();
		
	}
	
	protected Map<String, Device> retrieveDevices(String devicesTextString) {
		expect( devicesCommand.executeGetString() ).andReturn(devicesTextString).once();
		
		replay( devicesCommand );
		
		Map<String, Device> devices = unitUnderTest.retrieveDevices();
		
		verify( devicesCommand );
		
		return devices;
	}
	
	protected Map<String, Device> retrieveDevicesDontStore(String devicesTextString) {
		return retrieveDevices(devicesTextString, false);
	}
	
	protected Map<String, Device> retrieveDevices(String devicesTextString, boolean storeDevices) {
		expect( devicesCommand.executeGetString() ).andReturn(devicesTextString).once();
		
		replay( devicesCommand );
		
		Map<String, Device> devices = unitUnderTest.retrieveDevices(storeDevices);
		
		verify( devicesCommand );
		
		return devices;
	}
	
	protected void assertDevice(Map<String, Device> devices, Device expectedDevice) {
		assertDevice(devices.get(expectedDevice.getId()), expectedDevice);
	}
	
	protected void assertDevice(Device acutalDevice, Device expectedDevice) {
		DeviceAssert.assertThat(acutalDevice)
			.isNotNull()
			.hasId(expectedDevice.getId())
			.hasModel(expectedDevice.getModel())
			.hasProduct(expectedDevice.getProduct())
			.hasDevice(expectedDevice.getDevice())
		;
	}
}
