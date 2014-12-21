package org.greencubes.launcher;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.greencubes.main.Main;
import org.greencubes.util.OperatingSystem;

public class LauncherUpdate {

	private static final long RENAME_DELAY = 2000;
	
	private JFrame frame;
	
	/**
	 * Should not be invoked in AWT thread
	 */
	public LauncherUpdate() {
		if(LauncherOptions.noUpdateLauncher) {
			launcherLoad();
		} else {
			/*
			 * 0. Create cool window
			 * 1. Check for update
			 * 2. Download files in patch directory
			 * 3. Create patch.json
			 * 4. Start patcher
			 * 5. Wait for LOADED signal from patcher
			 * 6. Close launcher
			 */
		}
	}
	
	private List<String> getLaunchParameters() {
		List<String> command = new ArrayList<String>();
		command.add(OperatingSystem.getJavaExecutable(false));
		command.add("-jar");
		command.add(Main.launcherFile.getAbsolutePath());
		command.add("-updated");
		return command;
	}
	
	/**
	 * Should not be invoked in AWT thread
	 */
	public void launcherLoad() {
		new LauncherLogin(frame); // Send current frame so next window can destroy it when ready
	}
	
}
