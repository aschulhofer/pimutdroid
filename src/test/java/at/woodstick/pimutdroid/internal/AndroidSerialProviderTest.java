package at.woodstick.pimutdroid.internal;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

public class AndroidSerialProviderTest {

	private static final String ANDROID_SERIAL_NUMBER = "emulator-5554";
	private static final String ANDROID_SERIAL_ENV = "ANDROID_SERIAL";
	
	@Rule
	public final EnvironmentVariables environmentVariables = new EnvironmentVariables();
	
	private AndroidSerialProvider unitUnderTest;
	
	@Before
	public void setUp() {
		unitUnderTest = new AndroidSerialProvider();
	}

	@After
	public void tearDown() {
	}

	@Test
	public void constructor_notNull() {
		assertThat(unitUnderTest).isNotNull();
	}

	@Test
	public void getSerialAndHasSerial_envVariableSet_correctSerialReturned() {
		environmentVariables.set(ANDROID_SERIAL_ENV, ANDROID_SERIAL_NUMBER);
		
		assertThat(unitUnderTest.getAndroidSerial()).isEqualTo(ANDROID_SERIAL_NUMBER);
		assertThat(unitUnderTest.hasAndroidSerial()).isTrue();
	}
	
	@Test
	public void getSerialAndHasSerial_noEnvVariableSet_nullReturned() {
		assertThat(unitUnderTest.getAndroidSerial()).isNull();
		assertThat(unitUnderTest.hasAndroidSerial()).isFalse();
	}
}
