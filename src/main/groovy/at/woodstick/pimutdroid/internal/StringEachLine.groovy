package at.woodstick.pimutdroid.internal;

import java.util.function.Consumer
import java.util.function.Function

import groovy.transform.CompileStatic

@CompileStatic
public class StringEachLine {

	private String string;

	public StringEachLine(String string) {
		this.string = string;
	}
	
	public void call(Consumer<String> lineCallback) {
		string.eachLine({ String line ->
			lineCallback.accept(line);
		})
	}
	
	public static final StringEachLine eachLineOf(String string) {
		return new StringEachLine(string);
	}
}
