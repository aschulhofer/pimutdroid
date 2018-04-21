package at.woodstick.pimutdroid.test.helper;

public enum TestMutator {

	  EQUAL_IF("org.pitest.mutationtest.engine.gregor.mutators.RemoveConditionalMutator_EQUAL_IF", "removed conditional - replaced equality check with true")
	, EQUAL_ELSE("org.pitest.mutationtest.engine.gregor.mutators.RemoveConditionalMutator_EQUAL_ELSE", "removed conditional - replaced equality check with false")
	, VOID_METHOD("org.pitest.mutationtest.engine.gregor.mutators.VoidMethodCallMutator", "removed call to android/support/v7/app/AppCompatActivity::onCreate")
	;
	
	private String mutator;
	private String description;
	
	private TestMutator(String mutator, String description) {
		this.mutator = mutator;
		this.description = description;
	}

	public String getMutator() {
		return mutator;
	}

	public String getDescription() {
		return description;
	}
	
}
