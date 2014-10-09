package org.greencubes.main;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.greencubes.launcher.LauncherOptions;
import org.greencubes.util.Encryption;
import org.greencubes.util.Util;
import org.json.JSONObject;
import org.json.JSONTokener;

public class Main {
	
	public static final String IPV4STACK = "-Djava.net.preferIPv4Stack=true";
	public static final boolean TEST = true;
	
	private static JSONObject config;
	
	public static void main(String[] args) {
		for(String arg : args) {
			if(arg.equals("-debug"))
				LauncherOptions.debug = true;
			if(arg.equals("-noupdate"))
				LauncherOptions.noUpdateLauncher = true;
		}
		Security.addProvider(new BouncyCastleProvider());
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
		
		// Init our hardcore security shit
		Util.getUnsafe();
		Encryption.init();
		LauncherOptions.init();

		/*
		try {
			BufferedImage bi = ImageIO.read(new File("grid.png"));
			int[] data = bi.getRGB(0, 0, bi.getWidth(), bi.getHeight(), null, 0, bi.getWidth());
			ByteArrayOutputStream bao = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(bao);
			for(int i = 0; i < data.length; ++i)
				dos.writeInt(data[i]);
			dos.close();
			byte[] newData = Encryption.encrypt(bao.toByteArray(), Util.md5("OH MY GOSH, kITTENS ARE awesOme!!!13dsdddddffFFFFUUUUUUUUUUUUUUU").getBytes());
			DataInputStream dis = new DataInputStream(new ByteArrayInputStream(newData));
			for(int i = 0; i < data.length; ++i)
				data[i] = dis.readInt();
			bi.setRGB(0, 0, bi.getWidth(), bi.getHeight(), data, 0, bi.getWidth());
			ImageIO.write(bi, "png", new File("grid.encoded.png"));
		} catch(Exception e1) {
			e1.printStackTrace();
		}
		*/
		
		InputStream is = null;
		try {
			is = new FileInputStream(new File(Util.getAppDir("GreenCubes"),"launch.conf"));
			config = new JSONObject(new JSONTokener(is));
		} catch(Exception e) {
			config = new JSONObject();
		} finally {
			Util.close(is);
		}
		if(config.optBoolean("debug"))
			LauncherOptions.debug = true;
		LauncherOptions.onClientStart = LauncherOptions.OnStartAction.values()[config.optInt("onstart", LauncherOptions.onClientStart.ordinal())];
		LauncherOptions.saveSession();
		LauncherOptions.loadSession();
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
