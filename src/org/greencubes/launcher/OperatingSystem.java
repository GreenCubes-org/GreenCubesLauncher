package org.greencubes.launcher;

import java.io.File;

public enum OperatingSystem {
	LINUX("linux",new String[]{"linux", "unix"}), WINDOWS("windows", new String[]{"win"}), OSX("osx", new String[]{"mac"}), UNKNOWN("unknown",new String[0]);

	private final String[] aliases;
	public final String name;

	private OperatingSystem(String name, String[] aliases) {
		this.aliases = aliases;
		this.name = name;
	}

	public String getJavaDir() {
		String separator = System.getProperty("file.separator");
		String path = System.getProperty("java.home") + separator + "bin" + separator;

		if((getCurrentPlatform() == WINDOWS) && (new File(path + "javaw.exe").isFile()))
			return path + "javaw.exe";

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