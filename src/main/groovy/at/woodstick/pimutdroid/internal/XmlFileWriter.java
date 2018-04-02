package at.woodstick.pimutdroid.internal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class XmlFileWriter {

	private final ObjectMapper mapper;
	
	public XmlFileWriter(ObjectMapper mapper) {
		this.mapper = mapper;
	}

	public void writeTo(File file, Object value) throws IOException {
		writeTo(file.toPath(), value);
	}

	public void writeTo(Path filePath, Object value) throws IOException {
		mapper.writeValue(Files.newOutputStream(filePath), value);
	}
	
	public static final XmlFileWriter get() {
		final ObjectMapper mapper = new XmlMapper();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		
		return new XmlFileWriter(mapper);
	}
	
}
