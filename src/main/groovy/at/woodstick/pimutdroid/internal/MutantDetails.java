package at.woodstick.pimutdroid.internal;

public class MutantDetails {

	private String muid;
	private String clazzPackage;
	private String clazzName;
	private String clazz;
	private String method;
	private String mutator;
	private String filename;
	private String lineNumber;
	private String description;
	
	public MutantDetails() {
	}

	public String getMuid() {
		return muid;
	}

	public void setMuid(String muid) {
		this.muid = muid;
	}

	public String getClazzPackage() {
		return clazzPackage;
	}

	public void setClazzPackage(String clazzPackage) {
		this.clazzPackage = clazzPackage;
	}

	public String getClazzName() {
		return clazzName;
	}

	public void setClazzName(String clazzName) {
		this.clazzName = clazzName;
	}

	public String getClazz() {
		return clazz;
	}

	public void setClazz(String clazz) {
		this.clazz = clazz;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getMutator() {
		return mutator;
	}

	public void setMutator(String mutator) {
		this.mutator = mutator;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(String lineNumber) {
		this.lineNumber = lineNumber;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
