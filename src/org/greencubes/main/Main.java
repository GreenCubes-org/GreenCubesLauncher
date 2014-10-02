package org.greencubes.main;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.InputStream;

import org.greencubes.launcher.Launcher;
import org.greencubes.launcher.LauncherOptions;
import org.greencubes.launcher.LauncherUpdate;

public class Main {
	
	public static void main(String[] args) {
		for(String arg : args) {
			if(arg.equals("-debug"))
				LauncherOptions.debug = true;
			if(arg.equals("-noupdate"))
				LauncherOptions.noUpdateLauncher = true;
		}
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		try {
			InputStream is = Main.class.getResource("/res/font/ClearSans-Medium.ttf").openStream();
			ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, is));
			is.close();
			is = Main.class.getResource("/res/font/ClearSans-MediumItalic.ttf").openStream();
			ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, is));
			is.close();
			is = Main.class.getResource("/res/font/ClearSans-Bold.ttf").openStream();
			ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, is));
			is.close();
			is = Main.class.getResource("/res/font/ClearSans-BoldItalic.ttf").openStream();
			ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, is));
			is.close();
			is = Main.class.getResource("/res/font/ClearSans-Italic.ttf").openStream();
			ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, is));
			is.close();
			is = Main.class.getResource("/res/font/ClearSans-Regular.ttf").openStream();
			ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, is));
			is.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		if(!LauncherOptions.noUpdateLauncher)
			new LauncherUpdate();
		else
			new Launcher();
	}
}
