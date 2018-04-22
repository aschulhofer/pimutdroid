package at.woodstick.pimutdroid.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import at.woodstick.pimutdroid.configuration.InstrumentationTestOptions;
import at.woodstick.pimutdroid.configuration.TargetTests;

public class DeviceTestOptionsProvider {

	protected static final String INSTRUMENTATION_ARG_CLASS    = "class";
	protected static final String INSTRUMENTATION_ARG_PACKAGE  = "package";
	protected static final String INSTRUMENTATION_ARG_LISTENER = "listener";
	
	private InstrumentationTestOptions options;
	private String listenerFullClassName;

	public DeviceTestOptionsProvider(InstrumentationTestOptions options) {
		this(options, null);
	}
	
	public DeviceTestOptionsProvider(InstrumentationTestOptions options, String listenerFullClassName) {
		this.options = options;
		this.listenerFullClassName = listenerFullClassName;
	}

	protected Map<String, List<String>> createOptions() {
		TargetTests targetTests = options.getTargetTests();
		
		Map<String, List<String>> options = new HashMap<>();
		
		if(targetTests == null) {
			return options;
		}
		
		Set<String> classes = targetTests.getClasses();
		
		if(classes != null) {
			options.put(INSTRUMENTATION_ARG_CLASS, new ArrayList<>(classes));
			
			return options;
		}
		
		Set<String> packages = targetTests.getPackages();
		
		if(packages != null) {
			options.put(INSTRUMENTATION_ARG_PACKAGE, new ArrayList<>(packages));
			
			return options;
		}
		
		return options;
	}
	
	public Map<String, List<String>> getOptions() {
		Map<String, List<String>> testOptions = createOptions();
		
		if(listenerFullClassName != null) {
			// TODO: Move to separate configuration class ?
			testOptions.put(INSTRUMENTATION_ARG_LISTENER, Arrays.asList(listenerFullClassName));
		}
		
		return testOptions;
	}
}
