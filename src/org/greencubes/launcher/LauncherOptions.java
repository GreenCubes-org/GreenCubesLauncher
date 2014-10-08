package org.greencubes.launcher;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.greencubes.download.Downloader;
import org.greencubes.main.Main;

public class LauncherOptions {
	
	public static boolean debug = false;
	public static boolean noUpdateLauncher = false;
	public static OnStartAction onClientStart = OnStartAction.NO;
	private static List<BufferedImage> icons;
	private static Downloader downloader;
	
	public static enum OnStartAction {
		CLOSE, MINIMIZE, HIDE, NO;
	}
	
	public static Downloader getDownloader() {
		if(downloader == null) {
			if(Main.TEST) {
				downloader = new Downloader("https://greencubes.org/launcher_dev_lc17e22/"); // For test purposes
			} else {
				downloader = new Downloader("https://auth.greencubes.org/client/");
				downloader.addServer("https://auth1.greencubes.org/client/");
			}
		}
		return downloader;
	}
	
	public static List<BufferedImage> getIcons() {
		if(icons == null) {
			icons = new ArrayList<BufferedImage>();
			try {
				icons.add(ImageIO.read(LauncherOptions.class.getResource("/res/icons/gcico32x32.png")));
				icons.add(ImageIO.read(LauncherOptions.class.getResource("/res/icons/gcico48x48.png")));
				icons.add(ImageIO.read(LauncherOptions.class.getResource("/res/icons/gcico64x64.png")));
				icons.add(ImageIO.read(LauncherOptions.class.getResource("/res/icons/gcico128x128.png")));
				icons.add(ImageIO.read(LauncherOptions.class.getResource("/res/icons/gcico256x256.png")));
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		return icons;
	}
	
}
