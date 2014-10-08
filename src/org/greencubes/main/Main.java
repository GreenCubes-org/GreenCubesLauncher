package org.greencubes.main;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;

import org.greencubes.launcher.LauncherOptions;
import org.greencubes.util.Util;
import org.json.JSONObject;
import org.json.JSONTokener;

public class Main {
	
	private static JSONObject config;
	
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
		InputStream is = null;
		try {
			is = new FileInputStream(new File(Util.getAppDir("GreenCubes"),"launch.conf"));
			config = new JSONObject(new JSONTokener(is));
		} catch(Exception e) {
			config = new JSONObject();
		} finally {
			Util.close(is);
		}
		// TODO : Start launcher
	}
	
	public static JSONObject getConfig() {
		return config;
	}
	
	public static void close() {
		try {
			FileWriter fw = new FileWriter(new File(Util.getAppDir("GreenCubes"),"launch.conf"));
			config.write(fw);
			fw.close();
		} catch(Exception e) {}
		System.exit(0);
	}
}
