package org.greencubes.launcher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.greencubes.main.Main;
import org.greencubes.util.I18n;
import org.greencubes.util.OperatingSystem;
import org.greencubes.util.Util;

public class LauncherUpdate {
	
	/**
	 * Big delay, because stopping of launcher takes more than 3 seconds.
	 */
	private static final long RENAME_DELAY = 6000;
	
	private JFrame frame;
	
	/**
	 * Should not be invoked in AWT thread
	 */
	public LauncherUpdate() {
		if(LauncherOptions.noUpdateLauncher) {
			launcherLoad();
		} else {
			// TODO : Cool update window
			System.out.println("Updating launcher from stub 'ok.jar'...");
			File ok = new File("ok.jar");
			if(!ok.exists())
				throw new RuntimeException("OK file is not exists!");
			System.out.println("New file found.");
			File up = new File("up.jar");
			if(!up.exists())
				throw new RuntimeException("Can not find renamer!");
			System.out.println("Renamer found.");
			List<String> command = new ArrayList<String>();
			command.add(OperatingSystem.getJavaExecutable(false));
			command.add("-jar");
			command.add(up.getAbsolutePath());
			command.add(Main.launcherFile.getAbsolutePath());
			command.add(ok.getAbsolutePath());
			command.add(Long.toString(RENAME_DELAY));
			command.add(OperatingSystem.getJavaExecutable(false));
			command.add("-jar");
			command.add(Main.launcherFile.getAbsolutePath());
			command.add("-updated");
			ProcessBuilder pb = new ProcessBuilder(command).redirectErrorStream(true);
			JOptionPane.showMessageDialog(null, I18n.get("Было загружено обновление лаунчера.\nПосле нажатия кнопки OK лаунчер будет перезапущен.\nЕсли лаунчер через несколько секунд снова не запустится -\nпопробуйте запустить его самостоятельно."), I18n.get("Обновление"), JOptionPane.INFORMATION_MESSAGE);
			System.out.println("Starting updater. In 3 seconds launcher must be updated, and new process must be started. Will be shown message 'Everyting is ok!' is everyting is really ok.");
			try {
				System.out.println("Starting with " + Util.toString(command));
				pb.start();
				System.out.println("Updater started. Exiting launcher.");
				Main.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
			//launcherLoad();
		}
	}
	
	/**
	 * Should not be invoked in AWT thread
	 */
	public void launcherLoad() {
		new LauncherLogin(frame); // Send current frame so next window can destroy it when ready
	}
	
}
