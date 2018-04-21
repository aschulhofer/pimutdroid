package at.woodstick.pimutdroid.test.assertion;

import java.util.Objects;

import org.assertj.core.api.AbstractAssert;

import at.woodstick.pimutdroid.internal.Device;

public class DeviceAssert extends AbstractAssert<DeviceAssert, Device> {

	public DeviceAssert(Device actual) {
		super(actual, DeviceAssert.class);
	}

	public static DeviceAssert assertThat(Device actual) {
		return new DeviceAssert(actual);
	}
	
	public DeviceAssert hasId(String id) {
		isNotNull();
		
		if(! Objects.equals(actual.getId(), id)) {
			failWithMessage("Expected device id to be <%s> but was <%s>", id, actual.getId());
		}
		
		return this;
	}
	
	public DeviceAssert hasDevice(String device) {
		isNotNull();
		
		if(! Objects.equals(actual.getDevice(), device)) {
			failWithMessage("Expected device device to be <%s> but was <%s>", device, actual.getDevice());
		}
		
		return this;
	}
	
	public DeviceAssert hasModel(String model) {
		isNotNull();
		
		if(! Objects.equals(actual.getModel(), model)) {
			failWithMessage("Expected device model to be <%s> but was <%s>", model, actual.getModel());
		}
		
		return this;
	}
	
	public DeviceAssert hasProduct(String product) {
		isNotNull();
		
		if(! Objects.equals(actual.getProduct(), product)) {
			failWithMessage("Expected device product to be <%s> but was <%s>", product, actual.getProduct());
		}
		
		return this;
	}
}
