package at.woodstick.pimutdroid.result;

import java.math.BigDecimal;
import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "overview")
@JsonPropertyOrder({"mutants", "killed", "score"})
public class MutationOverview {

	@JacksonXmlProperty(localName = "killed", isAttribute = true)
	private final int numberOfMutantsKilled;
	
	@JacksonXmlProperty(localName = "mutants", isAttribute = true)
	private final int numberOfMutants;
	
	@JacksonXmlProperty(localName = "score", isAttribute = true)
	private final BigDecimal mutationScore;
	
	@JacksonXmlElementWrapper(localName = "packages")
	@JacksonXmlProperty(localName = "package")
	private final Collection<PackageOverview> packageOverview;
	
	@JacksonXmlElementWrapper(localName = "classes")
	@JacksonXmlProperty(localName = "class")
	private final Collection<ClassOverview> classOverview;

	public MutationOverview(int numberOfMutantsKilled, int numberOfMutants, BigDecimal mutationScore,
			Collection<PackageOverview> packageOverview, Collection<ClassOverview> classOverview) {
		this.numberOfMutantsKilled = numberOfMutantsKilled;
		this.numberOfMutants = numberOfMutants;
		this.mutationScore = mutationScore;
		this.packageOverview = packageOverview;
		this.classOverview = classOverview;
	}

	public int getNumberOfMutantsKilled() {
		return numberOfMutantsKilled;
	}

	public int getNumberOfMutants() {
		return numberOfMutants;
	}

	public BigDecimal getMutationScore() {
		return mutationScore;
	}

	public Collection<PackageOverview> getPackageOverview() {
		return packageOverview;
	}

	public Collection<ClassOverview> getClassOverview() {
		return classOverview;
	}
}
