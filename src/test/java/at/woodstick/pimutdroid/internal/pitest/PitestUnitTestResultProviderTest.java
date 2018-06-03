package at.woodstick.pimutdroid.internal.pitest;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import at.woodstick.pimutdroid.internal.UnitTestResult;
import at.woodstick.pimutdroid.result.Outcome;

public class PitestUnitTestResultProviderTest {

	private static final String MUTATIONS_EMPTY_XML_FILENAME = "mutations-empty.xml";
	private static final String MUTATIONS_XML_FILENAME = "mutations.xml";
	private static final String MUTATIONS_SINGLE_XML_FILENAME = "mutations-single.xml";
	
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
	public void hasResults_emptyXmlFile_noResult() throws JsonParseException, JsonMappingException, IOException {
		UnitTestResult pitestResult = getDutForFile(MUTATIONS_EMPTY_XML_FILENAME);
		
		assertThat(pitestResult.hasResults()).isFalse();
	}
	
	@Test
	public void hasResults_singleXmlFile_isTrue() throws JsonParseException, JsonMappingException, IOException {
		UnitTestResult pitestResult = getDutForFile(MUTATIONS_SINGLE_XML_FILENAME);
		
		assertThat(pitestResult.hasResults()).isTrue();
	}
	
	@Test
	public void hasResults_resultXmlFile_isTrue() throws JsonParseException, JsonMappingException, IOException {
		UnitTestResult pitestResult = getDutForFile(MUTATIONS_XML_FILENAME);
		
		assertThat(pitestResult.hasResults()).isTrue();
	}
	
	@Test
	public void getOutcome_emptyXmlFile_noResult() throws JsonParseException, JsonMappingException, IOException {
		UnitTestResult pitestResult = getDutForFile(MUTATIONS_EMPTY_XML_FILENAME);
		
		Outcome outcome = pitestResult.getOutcome("5", "org.pitest.mutationtest.engine.gregor.mutators.MathMutator", "add", "at.woodstick.mysampleapplication.util.DummyUtil", "DummyUtil.java");
		assertThat(outcome).isEqualTo(Outcome.NO_RESULT);
	}
	
	@Test
	public void getOutcome_singleXmlFile_lived() throws JsonParseException, JsonMappingException, IOException {
		UnitTestResult pitestResult = getDutForFile(MUTATIONS_SINGLE_XML_FILENAME);
		
		Outcome outcome = pitestResult.getOutcome("5", "org.pitest.mutationtest.engine.gregor.mutators.MathMutator", "add", "at.woodstick.mysampleapplication.util.DummyUtil", "DummyUtil.java");
		assertThat(outcome).isEqualTo(Outcome.LIVED);
	}
	
	@Test
	public void getOutcome_resultXmlFile_killed() throws JsonParseException, JsonMappingException, IOException {
		UnitTestResult pitestResult = getDutForFile(MUTATIONS_XML_FILENAME);
		
		Outcome outcome = pitestResult.getOutcome("6", "org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator", "add", "at.woodstick.mysampleapplication.util.DummyUtil", "DummyUtil.java");
		assertThat(outcome).isEqualTo(Outcome.KILLED);
	}

	@Test
	public void isKilled_emptyXmlFile_notKilled() throws JsonParseException, JsonMappingException, IOException {
		UnitTestResult pitestResult = getDutForFile(MUTATIONS_EMPTY_XML_FILENAME);
		
		boolean isKilled = pitestResult.isKilled("5", "org.pitest.mutationtest.engine.gregor.mutators.MathMutator", "add", "at.woodstick.mysampleapplication.util.DummyUtil", "DummyUtil.java");
		assertThat(isKilled).isFalse();
	}
	
	@Test
	public void isKilled_singleXmlFile_notKilled() throws JsonParseException, JsonMappingException, IOException {
		UnitTestResult pitestResult = getDutForFile(MUTATIONS_SINGLE_XML_FILENAME);
		
		boolean isKilled = pitestResult.isKilled("5", "org.pitest.mutationtest.engine.gregor.mutators.MathMutator", "add", "at.woodstick.mysampleapplication.util.DummyUtil", "DummyUtil.java");
		assertThat(isKilled).isFalse();
	}
	
	@Test
	public void isKilled_resultXmlFile_killed() throws JsonParseException, JsonMappingException, IOException {
		UnitTestResult pitestResult = getDutForFile(MUTATIONS_XML_FILENAME);
		
		boolean isKilled = pitestResult.isKilled("6", "org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator", "add", "at.woodstick.mysampleapplication.util.DummyUtil", "DummyUtil.java");
		assertThat(isKilled).isTrue();
	}
	
	protected PitestUnitTestResult getDutForFile(final String filename) throws JsonParseException, JsonMappingException, IOException {
		MutationsResultSet resultSet = readTestResultXml(filename);
		return new PitestUnitTestResult(resultSet);
	}
	
	Path getXmlPath(final String xmlName) {
		return Paths.get("src", "test", "resources", "at", "woodstick", "pimutdroid", "internal", "pitest", xmlName);
	}

	InputStream getXmlInputStream(final String xmlName) throws IOException {
		return Files.newInputStream(getXmlPath(xmlName));
	}
	
	MutationsResultSet readTestResultXml(final String xmlName) throws JsonParseException, JsonMappingException, IOException {
		return deserializer.readValue(getXmlInputStream(xmlName), MutationsResultSet.class);	
	}
}
