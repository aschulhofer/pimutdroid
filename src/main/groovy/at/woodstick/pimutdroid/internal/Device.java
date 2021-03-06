package at.woodstick.pimutdroid.internal;

import java.io.Serializable;

/**
 * 
 */
public class Device implements Serializable {
	
	/**
	 * Serializeable to be passed as argument to {@link RunTestOnDevice} 
	 */
	private static final long serialVersionUID = 1L;
	
	private final String id;
	private final String product;
	private final String model;
	private final String device;
	
	public Device(String id) {
		this(id, null, null, null);
	}

	public Device(String id, String product, String model, String device) {
		this.id = id;
		this.product = product;
		this.model = model;
		this.device = device;
	}

	public String getId() {
		return id;
	}

	public String getProduct() {
		return product;
	}

	public String getModel() {
		return model;
	}

	public String getDevice() {
		return device;
	}

	@Override
	public String toString() {
		return "Device [id=" + id + ", product=" + product + ", model=" + model + ", device=" + device + "]";
	}
}
