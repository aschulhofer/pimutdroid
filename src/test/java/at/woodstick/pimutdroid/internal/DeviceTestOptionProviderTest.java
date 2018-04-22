package at.woodstick.pimutdroid.internal;

import static at.woodstick.pimutdroid.test.helper.TestHelper.newInstrumentationTestOptions;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import at.woodstick.pimutdroid.configuration.InstrumentationTestOptions;

public class DeviceTestOptionProviderTest {

	private static final String DEFAULT_RUN_LISTENER = "de.schroepf.androidxmlrunlistener.XmlRunListener";

	private static final String ANDROID_ARGUMENT_LISTENER = "listener";
	private static final String ANDROID_ARGUMENT_PACKAGE  = "package";
	private static final String ANDROID_ARGUMENT_CLASS    = "class";
	
	@Test
	public void getOptions_emptyTestOptions_emptyDeviceTestOptions() {
		InstrumentationTestOptions options = newInstrumentationTestOptions().get();
		
		DeviceTestOptionsProvider unitUnderTest = new DeviceTestOptionsProvider(options);
		
		Map<String, List<String>> deviceTestOptions = unitUnderTest.getOptions();
		
		assertThat(deviceTestOptions).isEmpty();
	}
	
	@Test
	public void getOptions_runListenerProvidedEmptyTestOptions_returnListenerEntrySet() {
		InstrumentationTestOptions options = newInstrumentationTestOptions().get();
		
		DeviceTestOptionsProvider unitUnderTest = new DeviceTestOptionsProvider(options, DEFAULT_RUN_LISTENER);
		
		Map<String, List<String>> deviceTestOptions = unitUnderTest.getOptions();
		
		assertThat(deviceTestOptions).hasSize(1);
		assertThat(deviceTestOptions.get(ANDROID_ARGUMENT_LISTENER)).hasSize(1).containsExactlyInAnyOrder(DEFAULT_RUN_LISTENER);
	}
	
	@Test
	public void getOptions_testOptionsWithTestPackages_returnPackageEntrySet() {
		InstrumentationTestOptions options = newInstrumentationTestOptions().withTestPackages("at.package.one", "at.package.two").get();
		
		DeviceTestOptionsProvider unitUnderTest = new DeviceTestOptionsProvider(options);
		
		Map<String, List<String>> deviceTestOptions = unitUnderTest.getOptions();
		
		assertThat(deviceTestOptions).hasSize(1);
		assertThat(deviceTestOptions.get(ANDROID_ARGUMENT_PACKAGE)).containsExactlyInAnyOrder("at.package.one", "at.package.two");
	}
	
	@Test
	public void getOptions_testOptionsWithTestClasses_returnClassEntrySet() {
		InstrumentationTestOptions options = newInstrumentationTestOptions().withTestClasses("at.package.one.Main", "at.package.two.Main").get();
		
		DeviceTestOptionsProvider unitUnderTest = new DeviceTestOptionsProvider(options);
		
		Map<String, List<String>> deviceTestOptions = unitUnderTest.getOptions();
		
		assertThat(deviceTestOptions).hasSize(1);
		assertThat(deviceTestOptions.get(ANDROID_ARGUMENT_CLASS)).containsExactlyInAnyOrder("at.package.one.Main", "at.package.two.Main");
	}
	
	@Test
	public void getOptions_runListenerAndTestOptionsWithTestPackagesAndTestClasses_returnOptionsForListenerAndClassArgument() {
		InstrumentationTestOptions options = newInstrumentationTestOptions()
				.withTestPackages("at.package.one", "at.package.two")
				.withTestClasses("at.package.one.Main", "at.package.two.Main").get();
		
		DeviceTestOptionsProvider unitUnderTest = new DeviceTestOptionsProvider(options, DEFAULT_RUN_LISTENER);
		
		Map<String, List<String>> deviceTestOptions = unitUnderTest.getOptions();
		
		assertThat(deviceTestOptions).hasSize(2);
		assertThat(deviceTestOptions.get(ANDROID_ARGUMENT_LISTENER)).hasSize(1).containsExactlyInAnyOrder(DEFAULT_RUN_LISTENER);
		assertThat(deviceTestOptions.get(ANDROID_ARGUMENT_CLASS)).hasSize(2).containsExactlyInAnyOrder("at.package.one.Main", "at.package.two.Main");
	}
	
	@Test
	public void getOptions_runListenerAndTestOptionsWithTestPackages_returnOptionsForListenerAndClassArgument() {
		InstrumentationTestOptions options = newInstrumentationTestOptions()
				.withTestPackages("at.package.one", "at.package.two")
				.get();
		
		DeviceTestOptionsProvider unitUnderTest = new DeviceTestOptionsProvider(options, DEFAULT_RUN_LISTENER);
		
		Map<String, List<String>> deviceTestOptions = unitUnderTest.getOptions();
		
		assertThat(deviceTestOptions).hasSize(2);
		assertThat(deviceTestOptions.get(ANDROID_ARGUMENT_LISTENER)).hasSize(1).containsExactlyInAnyOrder(DEFAULT_RUN_LISTENER);
		assertThat(deviceTestOptions.get(ANDROID_ARGUMENT_PACKAGE)).hasSize(2).containsExactlyInAnyOrder("at.package.one", "at.package.two");
	}
}
