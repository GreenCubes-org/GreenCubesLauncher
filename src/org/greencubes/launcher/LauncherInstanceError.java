package org.greencubes.launcher;

import java.awt.Frame;

import javax.swing.JOptionPane;

import org.greencubes.main.Main;
import org.greencubes.util.I18n;

public class LauncherInstanceError {
	
	public static void showError() {
		final Frame f = new Frame(I18n.get("title"));
		f.setIconImages(LauncherOptions.getIcons());
		f.setLocationRelativeTo(null);
		f.setUndecorated(true);
		f.setVisible(true);
		JOptionPane.showMessageDialog(f, I18n.get("oneinstance"), I18n.get("error"), JOptionPane.ERROR_MESSAGE);
		f.dispose();
		Main.close();
	}
	
}
