package at.woodstick.pimutdroid.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import org.easymock.EasyMockRule;
import org.easymock.Mock;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class MuidProviderTest {

	private static final String DEFAULT_PROPERTY_NAME = "pimut.muid";
	private static final String CUSTOM_PROPERTY_NAME  = "test.pit.mut.muid";
	
	@Rule
	public EasyMockRule mockRule = new EasyMockRule(this);
	
	@Mock
	private Project project;
	
	private MuidProvider unitUnderTest;
	
	@Before
	public void setUp() {
		unitUnderTest = new MuidProvider(project);
	}
	
	@Test
	public void constructor_notNull() {
		assertThat(unitUnderTest).isNotNull();
	}
	
	@Test
	public void constructor_withCustomProperty_notNull() {
		assertThat(new MuidProvider(project, CUSTOM_PROPERTY_NAME)).isNotNull();
	}
	
	@Test(expected = GradleException.class)
	public void getMuid_propertyNotSet_throwGradleException() {
		
		expect( project.hasProperty(DEFAULT_PROPERTY_NAME) ).andReturn(false).once();
		
		runGetMuid();
	}
	
	@Test
	public void getMuid_propertyNotSet_throwGradleExceptionWithCorrectMessageAndNoCause() {
		
		expect( project.hasProperty(DEFAULT_PROPERTY_NAME) ).andReturn(false).once();
		
		assertThatExceptionOfType(GradleException.class).isThrownBy(() -> {
			runGetMuid();
		})
		.withMessage("Property '%s' not set", DEFAULT_PROPERTY_NAME)
		.withNoCause();
	}
	
	@Test
	public void getMuid_propertySet_getCorrectMuid() {
		String expectedMuid = "Display_0.muid";
		
		expect( project.hasProperty(DEFAULT_PROPERTY_NAME) ).andReturn(true).once();
		expect( project.property(DEFAULT_PROPERTY_NAME) ).andReturn(expectedMuid).once();
		
		String actualMuid = runGetMuid();
		
		assertThat(actualMuid).isEqualTo(expectedMuid);
	}
	
	@Test(expected = GradleException.class)
	public void getMuid_withCustomProperty_propertyNotSet_throwGradleException() {
		
		unitUnderTest = new MuidProvider(project, CUSTOM_PROPERTY_NAME);
		
		expect( project.hasProperty(CUSTOM_PROPERTY_NAME) ).andReturn(false).once();
		
		runGetMuid();
	}
	
	@Test
	public void getMuid_withCustomProperty_propertyNotSet_throwGradleExceptionWithCorrectMessageAndNoCause() {
		
		unitUnderTest = new MuidProvider(project, CUSTOM_PROPERTY_NAME);
		
		expect( project.hasProperty(CUSTOM_PROPERTY_NAME) ).andReturn(false).once();
		
		assertThatExceptionOfType(GradleException.class).isThrownBy(() -> {
			runGetMuid();
		})
		.withMessage("Property '%s' not set", CUSTOM_PROPERTY_NAME)
		.withNoCause();
	}
	
	@Test
	public void getMuid_withCustomProperty_propertySet_getCorrectMuid() {
		
		unitUnderTest = new MuidProvider(project, CUSTOM_PROPERTY_NAME);
		
		String expectedMuid = "Display_0.muid";
		
		expect( project.hasProperty(CUSTOM_PROPERTY_NAME) ).andReturn(true).once();
		expect( project.property(CUSTOM_PROPERTY_NAME) ).andReturn(expectedMuid).once();
		
		String actualMuid = runGetMuid();
		
		assertThat(actualMuid).isEqualTo(expectedMuid);
	}
	
	protected String runGetMuid() {
		replay( project );
		
		String muid = unitUnderTest.getMuid();
		
		verify( project );
		
		return muid;
	}
}
