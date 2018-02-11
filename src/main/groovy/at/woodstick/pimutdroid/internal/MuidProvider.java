package at.woodstick.pimutdroid.internal;

import org.gradle.api.GradleException;
import org.gradle.api.Project;

public class MuidProvider {

	private static final String PROPERTY_NAME_MUID = "pimut.muid";
	
	private final Project project;
	private final String muidPropertyName;
	
	public MuidProvider(Project project) {
		this(project, PROPERTY_NAME_MUID);
	}

	public MuidProvider(Project project, String muidPropertyName) {
		this.project = project;
		this.muidPropertyName = muidPropertyName;
	}

	public String getMuid() {
		if(!project.hasProperty(muidPropertyName)) {
			throw new GradleException(String.format("Property '%s' not set", muidPropertyName));
		}
		
		final String muid = (String) project.property(muidPropertyName);
		
		return muid;
	}
	
}
