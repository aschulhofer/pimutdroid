package at.woodstick.pimutdroid.internal.pitest;

import java.io.File;
import java.io.IOException;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import at.woodstick.pimutdroid.internal.PimutdroidException;
import at.woodstick.pimutdroid.internal.UnitTestResult;
import at.woodstick.pimutdroid.internal.UnitTestResultProvider;
import at.woodstick.pimutdroid.internal.XmlFileMapper;

public class PitestUnitTestResultProvider implements UnitTestResultProvider {

	private final static Logger LOGGER = Logging.getLogger(PitestUnitTestResultProvider.class);
	
	private File resultFile;
	private XmlFileMapper fileMapper;

	public PitestUnitTestResultProvider(File resultFile, XmlFileMapper fileMapper) {
		this.resultFile = resultFile;
		this.fileMapper = fileMapper;
	}

	@Override
	public UnitTestResult getResult() {
		try {
			MutationsResultSet resultSet = fileMapper.readFrom(resultFile, MutationsResultSet.class);
			return new PitestUnitTestResult(resultSet);
		} catch (IOException e) {
			LOGGER.error("Unable to read unit test xml result file", e);
			throw new PimutdroidException("Unable to read unit test xml result file", e);
		}
	}

}
