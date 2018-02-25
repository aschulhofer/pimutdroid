package at.woodstick.pimutdroid.internal;

public final class Utils {

	private Utils() {}
	
	public static String capitalize(final CharSequence string) {
		if(string.length() == 0) {
			return "";
		}
		
		return "" + Character.toUpperCase(string.charAt(0)) + string.subSequence(1, string.length());
	}
	
}
