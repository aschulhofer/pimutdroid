package at.woodstick.pimutdroid.result;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonPropertyOrder({ "name", "mutants", "killed", "score" })
public class PackageOverview {

	@JacksonXmlProperty(isAttribute = true)
	private final String name;
	
	@JacksonXmlProperty(isAttribute = true)
	private final int mutants;
	
	@JacksonXmlProperty(isAttribute = true)
	private final int killed;
	
	@JacksonXmlProperty(isAttribute = true)
	private final BigDecimal score;

	public PackageOverview(String name, int mutants, int killed, BigDecimal score) {
		this.name = name;
		this.mutants = mutants;
		this.killed = killed;
		this.score = score;
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
