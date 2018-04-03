package at.woodstick.pimutdroid.internal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class XmlFileMapper {

	private final ObjectMapper mapper;
	
	public XmlFileMapper(ObjectMapper mapper) {
		this.mapper = mapper;
	}

	public void writeTo(File file, Object value) throws IOException {
		writeTo(file.toPath(), value);
	}

	public void writeTo(Path filePath, Object value) throws IOException {
		mapper.writeValue(Files.newOutputStream(filePath), value);
	}
	
	public <T> T readFrom(File file, Class<T> valueType) throws IOException {
		return readFrom(file.toPath(), valueType);
	}
	
	public <T> T readFrom(Path filePath, Class<T> valueType) throws IOException {
		return mapper.readValue(Files.newInputStream(filePath), valueType);
	}
	
	public static final XmlFileMapper get() {
		final ObjectMapper mapper = new XmlMapper();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		
		return new XmlFileMapper(mapper);
	}
}
