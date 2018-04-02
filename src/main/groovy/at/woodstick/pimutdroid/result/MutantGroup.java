package at.woodstick.pimutdroid.result;

import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class MutantGroup {

	@JacksonXmlProperty(localName = "package", isAttribute = true)
	private final String mutantPackage;
	
	@JacksonXmlProperty(localName = "class", isAttribute = true)
	private final String mutantClass;
	
	@JacksonXmlProperty(isAttribute = true)
	private final int mutants;
	
	@JacksonXmlProperty(isAttribute = true)
	private final int killed;
	
	@JacksonXmlElementWrapper(useWrapping = false)
	@JacksonXmlProperty(localName = "mutant")
	private final List<Mutant> mutantList;

	public MutantGroup(String mutantPackage, String mutantClass, int mutants, int killed, List<Mutant> mutantList) {
		this.mutantPackage = mutantPackage;
		this.mutantClass = mutantClass;
		this.mutants = mutants;
		this.killed = killed;
		this.mutantList = mutantList;
	}

	public String getMutantPackage() {
		return mutantPackage;
	}

	public String getMutantClass() {
		return mutantClass;
	}

	public int getMutants() {
		return mutants;
	}

	public int getKilled() {
		return killed;
	}

	public List<Mutant> getMutantList() {
		return mutantList;
	}
}
