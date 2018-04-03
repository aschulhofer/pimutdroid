package at.woodstick.pimutdroid.result;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import at.woodstick.pimutdroid.internal.XmlFileMapper;

public class TestSuiteResultReaderTest {

	private static final String TESTSUITE_EXPECTED_RESULT_XML = "testsuite-expected-result.xml";
	private static final String TESTSUITE_WITH_FAILURE_XML = "testsuite-with-failure.xml";
	private static final String TESTSUITE_NO_FAILURE_XML = "testsuite-no-failure.xml";
	private static final String TESTSUITE_NO_FAILURE_DIFFERENT_TESTCASE_ORDER_XML = "testsuite-no-failure-different-testcase-order.xml";

	private TestSuiteResultReader uniUnderTest;
	
	private XmlFileMapper mapper;
	
	@Before
	public void setUp() {
		mapper = XmlFileMapper.get();
		
		uniUnderTest = new TestSuiteResultReader(mapper);
	}
	
	@After
	public void tearDown() {
		mapper = null;
		
		uniUnderTest = null;
	}
	
	@Test
	public void read_TestsuitesEquals_areEqual() throws JsonParseException, JsonMappingException, IOException {
		TestSuiteResult expectedResultObject = readTestResultXml(TESTSUITE_EXPECTED_RESULT_XML);
		TestSuiteResult noFailuresResult = readTestResultXml(TESTSUITE_NO_FAILURE_XML);
		
		assertThat(noFailuresResult).isEqualTo(expectedResultObject);
	}
	
	@Test
	public void read_TestsuitesEqualButDifferentTestcaseListOrder_areNotEqual() throws JsonParseException, JsonMappingException, IOException {
		TestSuiteResult expectedResultObject = readTestResultXml(TESTSUITE_EXPECTED_RESULT_XML);
		TestSuiteResult noFailuresDifferentOrderedTestcases = readTestResultXml(TESTSUITE_NO_FAILURE_DIFFERENT_TESTCASE_ORDER_XML);
		
		assertThat(noFailuresDifferentOrderedTestcases).isEqualTo(expectedResultObject);
	}
	
	@Test
	public void read_TestsuitesEqualButDifferentTestcaseListOrder_sortTestcaseLists_areEqual() throws JsonParseException, JsonMappingException, IOException {
		TestSuiteResult expectedResultObject = readTestResultXml(TESTSUITE_EXPECTED_RESULT_XML);
		TestSuiteResult noFailuresDifferentOrderedTestcases = readTestResultXml(TESTSUITE_NO_FAILURE_DIFFERENT_TESTCASE_ORDER_XML);
		
		assertThat(noFailuresDifferentOrderedTestcases.getTestcases()).isEqualTo(expectedResultObject.getTestcases());
	}
	
	@Test
	public void read_TestsuitesNotEqual_areNotEqual() throws JsonParseException, JsonMappingException, IOException {
		TestSuiteResult expectedResultObject = readTestResultXml(TESTSUITE_EXPECTED_RESULT_XML);
		TestSuiteResult testsuiteWithFailure = readTestResultXml(TESTSUITE_WITH_FAILURE_XML);
		
		assertThat(testsuiteWithFailure).isNotEqualTo(expectedResultObject);
	}
	
	Path getXmlPath(final String xmlName) {
		return Paths.get("src", "test", "resources", "at", "woodstick", "pimutdroid", "result", "schoepf", xmlName);
	}

	TestSuiteResult readTestResultXml(final String xmlName) throws IOException {
		return uniUnderTest.read(getXmlPath(xmlName));
	}
}
