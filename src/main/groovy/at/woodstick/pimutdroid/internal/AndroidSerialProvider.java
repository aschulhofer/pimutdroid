package at.woodstick.pimutdroid.internal;

public class AndroidSerialProvider {

	private static final String ANDROID_SERIAL_ENV = "ANDROID_SERIAL";
	
	public String getAndroidSerial() {
		return System.getenv(ANDROID_SERIAL_ENV);
	}
	
	public boolean hasAndroidSerial() {
		return ( getAndroidSerial() != null );
	}
	
}
