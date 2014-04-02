package org.greencubes.launcher;

public enum EnumOS {

	LINUX, WINDOWS, MACOS, SOLARIS, UNKNOWN;

	private static EnumOS os;

	public static EnumOS getOS() {
		if(os != null)
			return os;
		String s = System.getProperty("os.name").toLowerCase();
		if(s.contains("win"))
			os = WINDOWS;
		if(s.contains("mac"))
			os = MACOS;
		if(s.contains("solaris"))
			os = SOLARIS;
		if(s.contains("sunos"))
			os = SOLARIS;
		if(s.contains("linux"))
			os = LINUX;
		if(s.contains("unix"))
			os = LINUX;
		os = UNKNOWN;
		return os;
	}
}
