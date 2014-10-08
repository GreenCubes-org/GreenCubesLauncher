package org.greencubes.util;

import java.io.File;

public enum OperatingSystem {
	
	//@formatter:off
	LINUX("linux", new String[]{"linux", "unix"}),
	WINDOWS("windows", new String[]{"win"}),
	OSX("osx", new String[]{"mac"}),
	UNKNOWN("unknown", new String[0]);
	//@fomatter: on
	
	private final String[] aliases;
	public final String name;

	private OperatingSystem(String name, String[] aliases) {
		this.aliases = aliases;
		this.name = name;
	}

	public static String getJavaExecutable(boolean forceConsole) {
		String separator = System.getProperty("file.separator");
		String path = System.getProperty("java.home") + separator + "bin" + separator;
		if(!forceConsole && getCurrentPlatform() == WINDOWS && new File(path + "javaw.exe").isFile())
			return path + "javaw.exe";
		if(getCurrentPlatform() == WINDOWS)
			return path + "java.exe";
		return path + "java";
	}

	public static OperatingSystem getCurrentPlatform() {
		String osName = System.getProperty("os.name").toLowerCase();
		for(OperatingSystem os : values()) {
			for(String alias : os.aliases) {
				if(osName.contains(alias))
					return os;
			}
		}
		return UNKNOWN;
	}
}