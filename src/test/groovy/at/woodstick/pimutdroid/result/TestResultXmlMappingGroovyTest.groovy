package at.woodstick.pimutdroid.result;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import groovy.transform.CompileStatic

@CompileStatic
public class TestResultXmlMappingGroovyTest {

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
	public void deserializeResultXml_TestsuiteWithTestcasesXml_CorrectDeserialization() throws JsonParseException, JsonMappingException, IOException {
		TestSuiteResult resultObject = readTestResultXml("android-test-result.xml");
		
		assertThat(resultObject.getTests()).isEqualTo(6L);
		assertThat(resultObject.getFailures()).isEqualTo(1L);
		assertThat(resultObject.getSkipped()).isEqualTo(1L);
		assertThat(resultObject.getErrors()).isEqualTo(0L);
		
		assertThat(resultObject.getTestcases().get(4).getSkipped()).isNotNull();
		assertThat(resultObject.getTestcases().get(4).isSkipped()).isFalse();
		
		assertThat(resultObject.getTestcases().get(5).getSkipped()).isNotNull();
		assertThat(resultObject.getTestcases().get(5).isSkipped()).isTrue();
	}
	
	@Test
	public void deserializeResultXml_EmptyTestsuiteXml_CorrectDeserialization() throws JsonParseException, JsonMappingException, IOException {
		TestSuiteResult resultObject = readTestResultXml("empty-android-test-result.xml");
		
		assertThat(resultObject.getTests()).isEqualTo(0L);
	}
	
	@Test
	public void deserializeResultXml_RunListenerXml_CorrectDeserialization() throws JsonParseException, JsonMappingException, IOException {
		TestSuiteResult resultObject = readTestResultXml("report-0.xml");
		
		assertThat(resultObject.getTests()).isEqualTo(6L);
	}
	
	@Test
	public void deserializeResultXml_RunListenerExpectedXml_CorrectDeserialization() throws JsonParseException, JsonMappingException, IOException {
		TestSuiteResult resultObject = readTestResultXml("report-0-expected.xml");
		
		assertThat(resultObject.getTests()).isEqualTo(6L);
	}

	Path getXmlPath(final String xmlName) {
		return Paths.get("src", "test", "resources", "at", "woodstick", "pimutdroid", "result", xmlName);
	}

	InputStream getXmlInputStream(final String xmlName) {
		return Files.newInputStream(getXmlPath(xmlName));
	}
	
	TestSuiteResult readTestResultXml(final String xmlName) {
		return deserializer.readValue(getXmlInputStream(xmlName), TestSuiteResult.class);	
	}
}
