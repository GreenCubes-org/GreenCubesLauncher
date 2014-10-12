package org.greencubes.launcher;

public class LauncherUpdate {
	
	public LauncherUpdate() {
		if(LauncherOptions.noUpdateLauncher) {
			launcherLoad();
		} else {
			// TODO : Do update
		}
	}
	
	public void launcherLoad() {
		new LauncherLogin();
	}
	
}
