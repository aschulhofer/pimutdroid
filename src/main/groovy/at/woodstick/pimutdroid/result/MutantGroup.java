package at.woodstick.pimutdroid.result;

import java.math.BigDecimal;
import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonPropertyOrder({"package", "file", "class", "mutants", "killed", "score"})
public class MutantGroup {

	@JacksonXmlProperty(localName = "package", isAttribute = true)
	private final String mutantPackage;
	
	@JacksonXmlProperty(localName = "class", isAttribute = true)
	private final String mutantClass;
	
	@JacksonXmlProperty(isAttribute = true)
	private final int mutants;
	
	@JacksonXmlProperty(isAttribute = true)
	private final int killed;
	
	@JacksonXmlProperty(isAttribute = true)
	private final BigDecimal score;
	
	@JacksonXmlProperty(isAttribute = true)
	private final String file;
	
	@JacksonXmlElementWrapper(useWrapping = false)
	@JacksonXmlProperty(localName = "mutant")
	private final Collection<Mutant> mutantList;

	public MutantGroup(String mutantPackage, String mutantClass, int mutants, int killed, BigDecimal score, String file,
			Collection<Mutant> mutantList) {
		this.mutantPackage = mutantPackage;
		this.mutantClass = mutantClass;
		this.mutants = mutants;
		this.killed = killed;
		this.score = score;
		this.file = file;
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

	public BigDecimal getScore() {
		return score;
	}

	public String getFile() {
		return file;
	}

	public Collection<Mutant> getMutantList() {
		return mutantList;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((file == null) ? 0 : file.hashCode());
		result = prime * result + killed;
		result = prime * result + ((mutantClass == null) ? 0 : mutantClass.hashCode());
		result = prime * result + ((mutantList == null) ? 0 : mutantList.hashCode());
		result = prime * result + ((mutantPackage == null) ? 0 : mutantPackage.hashCode());
		result = prime * result + mutants;
		result = prime * result + ((score == null) ? 0 : score.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MutantGroup other = (MutantGroup) obj;
		if (file == null) {
			if (other.file != null)
				return false;
		} else if (!file.equals(other.file))
			return false;
		if (killed != other.killed)
			return false;
		if (mutantClass == null) {
			if (other.mutantClass != null)
				return false;
		} else if (!mutantClass.equals(other.mutantClass))
			return false;
		if (mutantList == null) {
			if (other.mutantList != null)
				return false;
		} else if (!mutantList.equals(other.mutantList))
			return false;
		if (mutantPackage == null) {
			if (other.mutantPackage != null)
				return false;
		} else if (!mutantPackage.equals(other.mutantPackage))
			return false;
		if (mutants != other.mutants)
			return false;
		if (score == null) {
			if (other.score != null)
				return false;
		} else if (!score.equals(other.score))
			return false;
		return true;
	}
}
