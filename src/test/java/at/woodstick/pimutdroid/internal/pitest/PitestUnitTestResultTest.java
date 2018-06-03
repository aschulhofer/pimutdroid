package at.woodstick.pimutdroid.internal.pitest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.File;
import java.io.IOException;

import org.easymock.EasyMockRule;
import org.easymock.Mock;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import at.woodstick.pimutdroid.internal.PimutdroidException;
import at.woodstick.pimutdroid.internal.UnitTestResult;
import at.woodstick.pimutdroid.internal.XmlFileMapper;

public class PitestUnitTestResultTest {

	@Rule
	public EasyMockRule rule = new EasyMockRule(this);
	
	@Mock
	private File resultFile;
	
	@Mock
	private XmlFileMapper xmlMapper;
	
	private PitestUnitTestResultProvider dut;
	
	@Before
	public void setUp() {
		dut = new PitestUnitTestResultProvider(resultFile, xmlMapper);
	}
	
	@After
	public void tearDown() {
		dut = null;
	}
	
	@Test
	public void constructor_notNull() throws JsonParseException, JsonMappingException, IOException {
		assertThat(dut).isNotNull();
	}
	
	@Test(expected = PimutdroidException.class)
	public void getResult_ioException_exceptionIsThrown() throws JsonParseException, JsonMappingException, IOException {
		expect( xmlMapper.readFrom(resultFile, MutationsResultSet.class) ).andThrow(new IOException("No file"));
		
		replay( xmlMapper );
		
		dut.getResult();
	}
	
	@Test
	public void getResult_resultAvailable_returnUnitResult() throws JsonParseException, JsonMappingException, IOException {
		MutationsResultSet resultSet = new MutationsResultSet();
		
		expect( xmlMapper.readFrom(resultFile, MutationsResultSet.class) ).andReturn(resultSet);
		
		replay( xmlMapper );
		
		UnitTestResult result = dut.getResult();
		
		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(PitestUnitTestResult.class);
		assertThat(result.hasResults()).isFalse();
		
		verify( xmlMapper );
	}
}
