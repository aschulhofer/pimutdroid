package at.woodstick.pimutdroid.internal;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class UtilsTest {
	
	@Test
	public void capitalize_withAllLowercaseString_isCapitalized() {
		final String lowercase = "abcdefg";
		final String expectedString = "Abcdefg";
		
		assertThat(Utils.capitalize(lowercase)).isEqualTo(expectedString);
	}
	
	@Test
	public void capitalize_withStringLowercaseString_isCapitalized() {
		final String lowercase = "aBCDefg";
		final String expectedString = "ABCDefg";
		
		assertThat(Utils.capitalize(lowercase)).isEqualTo(expectedString);
	}
	
	@Test
	public void capitalize_withLeadingBlankString_isCapitalizedNotTrimmed() {
		final String lowercase = " aBCDefg";
		final String expectedString = " aBCDefg";
		
		assertThat(Utils.capitalize(lowercase)).isEqualTo(expectedString);
	}
	
	@Test
	public void capitalize_withTrailingBlankString_isCapitalizedNotTrimmed() {
		final String lowercase = "aBCDefg ";
		final String expectedString = "ABCDefg ";
		
		assertThat(Utils.capitalize(lowercase)).isEqualTo(expectedString);
	}
	
	@Test
	public void capitalize_withUppercaseString_isSame() {
		final String lowercase = "ABCDEFG";
		final String expectedString = "ABCDEFG";
		
		assertThat(Utils.capitalize(lowercase)).isEqualTo(expectedString);
	}
	
	@Test
	public void capitalize_withLeadingNumberString_isSame() {
		final String lowercase = "1ABCDEFG";
		final String expectedString = "1ABCDEFG";
		
		assertThat(Utils.capitalize(lowercase)).isEqualTo(expectedString);
	}
	
	@Test
	public void capitalize_withEmptyString_isEmpty() {
		assertThat(Utils.capitalize("")).isEmpty();
	}
	
	@Test(expected = NullPointerException.class)
	public void capitalize_withNull_throwsNullpointer() {
		Utils.capitalize(null);
	}
}
