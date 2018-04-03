package at.woodstick.pimutdroid.result;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;

import at.woodstick.pimutdroid.internal.XmlFileMapper;

public class TestSuiteResultReader {

	private XmlFileMapper mapper;
	
	public TestSuiteResultReader(XmlFileMapper mapper) {
		this.mapper = mapper;
	}

	public TestSuiteResult read(File file) throws IOException {
		return read(file.toPath());
	}
	
	public TestSuiteResult read(Path filePath) throws IOException {
		TestSuiteResult result = mapper.readFrom(filePath, TestSuiteResult.class);
		
		Collections.sort(result.getTestcases());
		
		return result;
	}
	
}
