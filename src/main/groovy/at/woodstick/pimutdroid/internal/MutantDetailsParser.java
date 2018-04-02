package at.woodstick.pimutdroid.internal;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MutantDetailsParser {

	private static final Pattern MUTANT_DETAIL_PATTERN = Pattern.compile(".*?clazz=(.*?),\\smethod=(.*?),.*?mutator=(.*?)\\],\\sfilename=(.*?),.*?lineNumber=(.*?),.*?description=(.*?),\\stestsInOrder.*");
	private static final Pattern MUTANT_CLASS_PATTERN = Pattern.compile("^(.*?)\\.([\\w$]+)$");
	
	public MutantDetails parseFromFile(final String muid, final File detailsFile) throws IOException {
		MutantDetails details = parseFromFile(detailsFile);
		
		details.setMuid(muid);
		
		return details;
	}
	
	public MutantDetails parseFromFile(final File detailsFile) throws IOException {
		MutantDetails details = new MutantDetails();
		
		String detailsString = readFile(detailsFile);
		
		Matcher matcher = MUTANT_DETAIL_PATTERN.matcher(detailsString.trim());
		
		if(matcher.matches()) {
			String clazz = matcher.group(1);
			String method = matcher.group(2);
			String mutator = matcher.group(3);
			String filename = matcher.group(4);
			String lineNumber = matcher.group(5);
			String description = matcher.group(6);
			
			Matcher clazzMatcher = MUTANT_CLASS_PATTERN.matcher(clazz);
			
			if(clazzMatcher.matches()) {
				details.setClazzPackage(clazzMatcher.group(1));
				details.setClazzName(clazzMatcher.group(2));
			}
			
			details.setClazz(clazz);
			details.setMethod(method);
			details.setMutator(mutator);
			details.setFilename(filename);
			details.setLineNumber(lineNumber);
			details.setDescription(description);
		}
		
		return details;
	}
	
	protected String readFile(File file) throws IOException {
		return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
	}
}
