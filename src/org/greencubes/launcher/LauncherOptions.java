package org.greencubes.launcher;

public class LauncherOptions {
	
	public static boolean debug = false;
	public static boolean noUpdateLauncher = false;
	public static OnStartAction onClientStart = OnStartAction.NO;
	
	public static enum OnStartAction {
		CLOSE, MINIMIZE, HIDE, NO;
	}
	
}
