package at.woodstick.pimutdroid.internal;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class ListDevicesCommand extends ConsoleCommand {

	ListDevicesCommand(List<?> commandList) {
		super(commandList);
	}

	public static ListDevicesCommand newInstance(File adbExecuteable) {
		List<?> commandList = Arrays.asList(adbExecuteable, "devices", "-l");
		
		return new ListDevicesCommand(commandList);
	}
}
