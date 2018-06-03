package at.woodstick.pimutdroid.internal.pitest;

import java.util.List;
import java.util.Optional;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import at.woodstick.pimutdroid.internal.UnitTestResult;
import at.woodstick.pimutdroid.result.Outcome;

public class PitestUnitTestResult implements UnitTestResult {

	private final static Logger LOGGER = Logging.getLogger(PitestUnitTestResult.class);
	
	private static final String KILLED_STATUS = "KILLED";
	
	private MutationsResultSet resultSet;
	private List<MutationResult> resultList;

	public PitestUnitTestResult(MutationsResultSet resultSet) {
		this.resultSet = resultSet;
		resultList = resultSet.getResult();
	}

	protected Optional<MutationResult> getResult(String index, String mutator, String method, String mutatedClass, String sourceFile) {
		if(resultSet.isEmpty()) {
			LOGGER.debug("Result empty");
			return Optional.empty();
		}
		
		for(MutationResult result : resultList) {
			if(result.getIndex().equals(index) && 
				result.getMutator().equals(mutator) && 
				result.getMethod().equals(method) && 
				result.getMutatedClass().equals(mutatedClass) && 
				result.getSourceFile().equals(sourceFile)
			) {
				LOGGER.debug("Result found for ('{}', '{}', '{}', '{}', '{}') - {}", index, mutator, method, mutatedClass, sourceFile, result);
				
				return Optional.of(result);
			}
		}
		
		LOGGER.debug("No result found for ('{}', '{}', '{}', '{}', '{}')", index, mutator, method, mutatedClass, sourceFile);
		
		return Optional.empty();
	}
	
	@Override
	public Outcome getOutcome(String index, String mutator, String method, String mutatedClass, String sourceFile) {
		Optional<MutationResult> result = getResult(index, mutator, method, mutatedClass, sourceFile);
		
		if(!result.isPresent()) {
			return Outcome.NO_RESULT;
		}
		
		if(KILLED_STATUS.equals(result.get().getStatus())) {
			return Outcome.KILLED;
		}
		
		return Outcome.LIVED;
	}

	@Override
	public boolean isKilled(String index, String mutator, String method, String mutatedClass, String sourceFile) {
		return getOutcome(index, mutator, method, mutatedClass, sourceFile) == Outcome.KILLED;
	}

	@Override
	public boolean hasResults() {
		return !resultSet.isEmpty();
	}
}
