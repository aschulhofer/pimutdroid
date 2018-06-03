package at.woodstick.pimutdroid.internal.pitest;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class PitestMutationXmlMapperTest {

	private static final String MUTATIONS_EMPTY_XML_FILENAME = "mutations-empty.xml";
	private static final String MUTATIONS_XML_FILENAME = "mutations.xml";
	private static final String MUTATIONS_SINGLE_XML_FILENAME = "mutations-single.xml";
	private static final int NUM_MUTATION_ENTRIES = 6;
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
	public void readXml_listIsEmpty() throws JsonParseException, JsonMappingException, IOException {
		MutationsResultSet resultSet = readTestResultXml(MUTATIONS_EMPTY_XML_FILENAME);
		
		assertThat(resultSet).isNotNull();
		assertThat(resultSet.isEmpty()).isTrue();
		assertThat(resultSet.getResult()).isNull();
	}
	
	@Test
	public void readXml_listHasSizeOfSix() throws JsonParseException, JsonMappingException, IOException {
		MutationsResultSet resultSet = readTestResultXml(MUTATIONS_XML_FILENAME);
		assertThat(resultSet.isEmpty()).isFalse();
		assertThat(resultSet).isNotNull();
		
		List<MutationResult> resultList = resultSet.getResult();
		assertThat(resultList).isNotNull();
		assertThat(resultList).isNotEmpty().hasSize(NUM_MUTATION_ENTRIES);
	}
	
	@Test
	public void readXml_listHasSizeOfOne_resultRowCorrect() throws JsonParseException, JsonMappingException, IOException {
		MutationsResultSet resultSet = readTestResultXml(MUTATIONS_SINGLE_XML_FILENAME);
		assertThat(resultSet.isEmpty()).isFalse();
		assertThat(resultSet).isNotNull();
		
		List<MutationResult> resultList = resultSet.getResult();
		assertThat(resultList).isNotNull();
		assertThat(resultList).isNotEmpty().hasSize(1);
		
		MutationResult internalResult = resultList.get(0);
		
		assertThat(internalResult.getDetected()).isEqualTo("false");
		assertThat(internalResult.getStatus()).isEqualTo("NO_COVERAGE");
		assertThat(internalResult.getSourceFile()).isEqualTo("DummyUtil.java");
		assertThat(internalResult.getMutatedClass()).isEqualTo("at.woodstick.mysampleapplication.util.DummyUtil");
		assertThat(internalResult.getIndex()).isEqualTo("5");
		assertThat(internalResult.getMutator()).isEqualTo("org.pitest.mutationtest.engine.gregor.mutators.MathMutator");
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
