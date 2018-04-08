package at.woodstick.pimutdroid.result;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonPropertyOrder({ "package", "name", "mutants", "killed", "score" })
public class ClassOverview {

	@JacksonXmlProperty(localName = "package", isAttribute = true)
	private final String packageName;
	
	@JacksonXmlProperty(isAttribute = true)
	private final String name;
	
	@JacksonXmlProperty(isAttribute = true)
	private final int mutants;
	
	@JacksonXmlProperty(isAttribute = true)
	private final int killed;
	
	@JacksonXmlProperty(isAttribute = true)
	private final BigDecimal score;

	public ClassOverview(String packageName, String name, int mutants, int killed, BigDecimal score) {
		this.packageName = packageName;
		this.name = name;
		this.mutants = mutants;
		this.killed = killed;
		this.score = score;
	}

	public String getPackageName() {
		return packageName;
	}

	public String getName() {
		return name;
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
}
