package at.woodstick.pimutdroid.internal;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import at.woodstick.pimutdroid.configuration.InstrumentationTestOptions;
import at.woodstick.pimutdroid.result.ClassOverview;
import at.woodstick.pimutdroid.result.Mutant;
import at.woodstick.pimutdroid.result.MutantGroup;
import at.woodstick.pimutdroid.result.Mutation;
import at.woodstick.pimutdroid.result.MutationOverview;
import at.woodstick.pimutdroid.result.MutationResult;
import at.woodstick.pimutdroid.result.Outcome;
import at.woodstick.pimutdroid.result.PackageOverview;
import at.woodstick.pimutdroid.result.TestSetup;
import at.woodstick.pimutdroid.task.MutantDetailResult;
import at.woodstick.pimutdroid.task.MutantGroupKey;

public class MutationResultBuilder {
	
	private InstrumentationTestOptions testOptions;
	private Set<String> targetedMutants;
	private String resultTimeStampString;
	
	private Map<MutantGroupKey, List<MutantDetailResult>> mutantGroupMap;
	private int totalMutants;
	private int totalKilled;
	
	public MutationResultBuilder(InstrumentationTestOptions testOptions, Set<String> targetedMutants,
			String resultTimeStampString, Map<MutantGroupKey, List<MutantDetailResult>> mutantGroupMap,
			int totalMutants, int totalKilled) {
		this.testOptions = testOptions;
		this.targetedMutants = targetedMutants;
		this.resultTimeStampString = resultTimeStampString;
		this.mutantGroupMap = mutantGroupMap;
		this.totalMutants = totalMutants;
		this.totalKilled = totalKilled;
	}

	public MutationResult build() {
		final MutationScore totalMutationScore = MutationScore.of(totalMutants, totalKilled);
		
		Collection<MutantGroup> mutantGroupList = getMutantGroupList(mutantGroupMap);
		
		MutationOverview overview = getMutationOverview(totalMutationScore, mutantGroupList);
		
		TestSetup testSetup = getTestSetup();
		
		MutationResult mutationResult = new MutationResult(resultTimeStampString, overview, testSetup, mutantGroupList);
		
		return mutationResult;
	}
	
	private MutationOverview getMutationOverview(MutationScore totalMutationScore, Collection<MutantGroup> mutantGroupList) {
		Map<PackageOverviewKey, OverviewDataValue> packageOverviewMap = new TreeMap<PackageOverviewKey, OverviewDataValue>(PackageOverviewKeyComparator.getDefault());
		Map<ClassOverviewKey, OverviewDataValue> classOverviewMap = new TreeMap<ClassOverviewKey, OverviewDataValue>(ClassOverviewKeyComparator.getDefault());
		
		for(MutantGroup mutantGroup : mutantGroupList) {
			String mutantPackage = mutantGroup.getMutantPackage();
			String filename = mutantGroup.getFile();
			int mutants = mutantGroup.getMutants();
			int killed = mutantGroup.getKilled();
			
			PackageOverviewKey packageKey = PackageOverviewKey.of(mutantPackage);
			ClassOverviewKey classKey = ClassOverviewKey.of(mutantPackage, filename);
			
			OverviewDataValue packageDataValue;
			if(packageOverviewMap.containsKey(packageKey)) {
				packageDataValue = packageOverviewMap.get(packageKey);
			} else {
				packageDataValue = OverviewDataValue.empty();
				packageOverviewMap.put(packageKey, packageDataValue);
			}
			packageDataValue.addMutants(mutants);
			packageDataValue.addKilled(killed);
			
			OverviewDataValue classDataValue;
			if(classOverviewMap.containsKey(classKey)) {
				classDataValue = classOverviewMap.get(classKey);
			} else {
				classDataValue = OverviewDataValue.empty();
				classOverviewMap.put(classKey, classDataValue);
			}
			classDataValue.addMutants(mutants);
			classDataValue.addKilled(killed);
		}
		
		List<PackageOverview> packageOverviewList = new ArrayList<>();
		
		
		packageOverviewMap.forEach((PackageOverviewKey packageKey, OverviewDataValue data) -> {
			MutationScore scoreData = MutationScore.of(data.getMutants(), data.getKilled());
			
			PackageOverview packageOverview = new PackageOverview(packageKey.getMutantPackage(), scoreData.getMutants(), scoreData.getKilled(), scoreData.getScore());
			packageOverviewList.add(packageOverview);
		});
		
		List<ClassOverview> classOverviewList = new ArrayList<>();
		classOverviewMap.forEach((ClassOverviewKey classKey, OverviewDataValue data) -> { 
			MutationScore scoreData = MutationScore.of(data.getMutants(), data.getKilled());
			
			ClassOverview classOverview = new ClassOverview(classKey.getMutantPackage(), classKey.getFilename(), scoreData.getMutants(), scoreData.getKilled(), scoreData.getScore());
			classOverviewList.add(classOverview);
		});
		
		MutationOverview overview = new MutationOverview(totalMutationScore.getKilled(), totalMutationScore.getMutants(), totalMutationScore.getScore(), packageOverviewList, classOverviewList);
		
		return overview;
	}
	
	private Collection<MutantGroup> getMutantGroupList(final Map<MutantGroupKey, List<MutantDetailResult>> mutantGroupMap) {
		TreeSet<MutantGroup> mutantGroupList = new TreeSet<>(MutantGroupComparator.getDefault());
		
		mutantGroupMap.forEach((MutantGroupKey key, List<MutantDetailResult> detailList) -> {
			
			List<Mutant> mutantList = new ArrayList<>();
			int mutants = detailList.size();
			int killed = 0;
			
			for(MutantDetailResult resultDetails : detailList) { 
				
				MutantDetails details = resultDetails.getDetails();
				Mutation mutation = new Mutation(details.getMethod(), details.getLineNumber(), details.getMutator(), details.getDescription());
				Mutant mutant = new Mutant(details.getMuid(), resultDetails.getOutcome(), mutation);
				mutantList.add(mutant);
				
				if(resultDetails.getOutcome() == Outcome.KILLED) {
					killed++;
				}
			}
			
			MutantGroup group = new MutantGroup(
				key.getMutantPackage(),
				key.getMutantClass(),
				mutants,
				killed,
				calculateScore(mutants, killed),
				key.getFilename(),
				mutantList
			);
			
			mutantGroupList.add(group);
		});
		
		return mutantGroupList;
	}
	
	private TestSetup getTestSetup() {
		Set<String> packages = testOptions.getTargetTests().getPackages();
		Set<String> classes = testOptions.getTargetTests().getClasses();
		
		if(packages == null) {
			packages = Collections.emptySet();
		}
		
		if(classes == null) {
			classes = Collections.emptySet();
		}
		
		TestSetup testSetup = new TestSetup(packages, classes, targetedMutants, testOptions.getRunner());
		return testSetup;
	}
	
	private BigDecimal calculateScore(int mutants, int killed) {
		return MutationScore.of(mutants, killed).getScore();
	}
	
	public static MutationResultBuilder builder() {
		return new MutationResultBuilder(new InstrumentationTestOptions(), new HashSet<>(1), "", new HashMap<>(1), 0, 0);
	}
	
	public MutationResultBuilder withTestOptions(InstrumentationTestOptions testOptions) {
		this.testOptions = testOptions;
		return this;
	}
	
	public MutationResultBuilder withTargetedMutants(Set<String> targetedMutants) {
		this.targetedMutants = targetedMutants;
		return this;
	}
	
	public MutationResultBuilder withMutantResults(Map<MutantGroupKey, List<MutantDetailResult>> mutantGroupMap) {
		this.mutantGroupMap = mutantGroupMap;
		return this;
	}
	
	public MutationResultBuilder withTotalMutants(int totalMutants) {
		this.totalMutants = totalMutants;
		return this;
	}
	
	public MutationResultBuilder withKilledMutants(int killedMutants) {
		this.totalKilled = killedMutants;
		return this;
	}
	
	public MutationResultBuilder withResultTimestamp(String resultTimeStampString) {
		this.resultTimeStampString = resultTimeStampString;
		return this;
	}
}
