package org.greencubes.launcher;

import javax.swing.JFrame;

public class LauncherUpdate {
	
	private JFrame frame;
	
	public LauncherUpdate() {
		if(LauncherOptions.noUpdateLauncher) {
			launcherLoad();
		} else {
			// TODO : Do update
			launcherLoad();
		}
	}
	
	public void launcherLoad() {
		// It is already in other thread
		new LauncherLogin(frame); // Send current frame so next window can destroy it when ready
	}
	
}
