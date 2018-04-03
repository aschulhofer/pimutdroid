package at.woodstick.pimutdroid.result;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class TestResultSchoepfXmlMappingTest {

	private static final String TESTSUITE_NO_FAILURE_XML = "testsuite-no-failure.xml";
	private static final String TESTSUITE_WITH_FAILURE_XML = "testsuite-with-failure.xml";
	private static final String TESTSUITE_NO_FAILURE_DIFFERENT_TESTCASE_ORDER_XML = "testsuite-no-failure-different-testcase-order.xml";
	private static final String TESTSUITE_EXPECTED_RESULT_XML = "testsuite-expected-result.xml";
	
	private ObjectMapper deserializer;
	
	@Before
	public void setUp() {
		deserializer = new XmlMapper();
		deserializer.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	}
	
	@After
	public void tearDown() {
		deserializer = null;
	}
	
	@Test
	public void deserializeResultXmlFiles_TestsuitesEquals_areEqual() throws JsonParseException, JsonMappingException, IOException {
		TestSuiteResult expectedResultObject = readTestResultXml(TESTSUITE_EXPECTED_RESULT_XML);
		TestSuiteResult noFailuresResult = readTestResultXml(TESTSUITE_NO_FAILURE_XML);
		
		assertThat(noFailuresResult).isEqualTo(expectedResultObject);
	}
	
	@Test
	public void deserializeResultXmlFiles_TestsuitesEqualButDifferentTestcaseListOrder_areNotEqual() throws JsonParseException, JsonMappingException, IOException {
		TestSuiteResult expectedResultObject = readTestResultXml(TESTSUITE_EXPECTED_RESULT_XML);
		TestSuiteResult noFailuresDifferentOrderedTestcases = readTestResultXml(TESTSUITE_NO_FAILURE_DIFFERENT_TESTCASE_ORDER_XML);
		
		assertThat(noFailuresDifferentOrderedTestcases).isNotEqualTo(expectedResultObject);
	}
	
	@Test
	public void deserializeResultXmlFiles_TestsuitesEqualButDifferentTestcaseListOrder_sortTestcaseLists_areEqual() throws JsonParseException, JsonMappingException, IOException {
		TestSuiteResult expectedResultObject = readTestResultXml(TESTSUITE_EXPECTED_RESULT_XML);
		TestSuiteResult noFailuresDifferentOrderedTestcases = readTestResultXml(TESTSUITE_NO_FAILURE_DIFFERENT_TESTCASE_ORDER_XML);
		
		Collections.sort(expectedResultObject.getTestcases());
		Collections.sort(noFailuresDifferentOrderedTestcases.getTestcases());
		
		assertThat(noFailuresDifferentOrderedTestcases).isEqualTo(expectedResultObject);
	}
	
	@Test
	public void deserializeResultXmlFiles_TestsuitesNotEqual_areNotEqual() throws JsonParseException, JsonMappingException, IOException {
		TestSuiteResult expectedResultObject = readTestResultXml(TESTSUITE_EXPECTED_RESULT_XML);
		TestSuiteResult testsuiteWithFailure = readTestResultXml(TESTSUITE_WITH_FAILURE_XML);
		
		assertThat(testsuiteWithFailure).isNotEqualTo(expectedResultObject);
	}
	
	Path getXmlPath(final String xmlName) {
		return Paths.get("src", "test", "resources", "at", "woodstick", "pimutdroid", "result", "schoepf", xmlName);
	}

	InputStream getXmlInputStream(final String xmlName) throws IOException {
		return Files.newInputStream(getXmlPath(xmlName));
	}
	
	TestSuiteResult readTestResultXml(final String xmlName) throws JsonParseException, JsonMappingException, IOException {
		return deserializer.readValue(getXmlInputStream(xmlName), TestSuiteResult.class);	
	}
}
