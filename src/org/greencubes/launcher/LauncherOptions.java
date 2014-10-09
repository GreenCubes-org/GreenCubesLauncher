package org.greencubes.launcher;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Permission;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

import org.bouncycastle.util.Arrays;
import org.greencubes.download.Downloader;
import org.greencubes.main.Main;
import org.greencubes.util.Encryption;
import org.greencubes.util.Util;

@SuppressWarnings("restriction")
public class LauncherOptions {
	
	public static boolean debug = false;
	public static boolean noUpdateLauncher = false;
	public static OnStartAction onClientStart = OnStartAction.NO;
	private static List<BufferedImage> icons;
	private static Downloader downloader;
	
	private static long sessionKeyAddress = -1;
	public static String sessionUser;
	public static int sessionUserId;
	
	public static enum OnStartAction {
		CLOSE, MINIMIZE, HIDE, NO;
	}
	
	public static Downloader getDownloader() {
		if(downloader == null) {
			if(Main.TEST) {
				downloader = new Downloader("https://greencubes.org/"); // For test purposes
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
	
	public static void saveSession() {
		try {
			// Ensure no one replaced current threads class and security manager is in place
			if(!Class.forName("java.lang.Thread").getMethod("getStackTrace").equals(Thread.currentThread().getClass().getMethod("getStackTrace")))
				throw new RuntimeException();
			Encryption.getSecurityManager();
		} catch(Exception e) {
			Encryption.throwMajicError();
			return;
		}
		if(sessionUserId <= 0 || sessionKeyAddress == -1 || sessionUser == null)
			return;
		DataOutputStream os = null;
		try {
			File f = new File("user.dat");
			if(!f.exists()) {
				if(!f.createNewFile())
					throw new IOException("Unable to save session");
			} else if(!f.isFile()) {
				throw new IOException("Session file is not a file");
			}
			os = new DataOutputStream(new FileOutputStream(f));
			os.writeInt(sessionUserId ^ Integer.parseInt("111011001110011101011010101", 2));
			ByteArrayOutputStream decodedData = new ByteArrayOutputStream();
			DataOutputStream decodedOs = new DataOutputStream(decodedData);
			decodedOs.writeUTF(sessionUser);
			byte[] sessionKey = new byte[512];
			for(int i = 0; i < 512; ++i)
				sessionKey[i] = Util.getUnsafe().getByte(sessionKeyAddress + i);
			decodedOs.write(sessionKey);
			decodedOs.writeInt(1028 ^ 1);
			decodedOs.writeUTF("dd");
			decodedOs.close(); // It is just polite to close streams
			byte[] decodedDataArray = Arrays.copyOf(decodedData.toByteArray(), 1000);
			byte[] encodedData = Encryption.encrypt(decodedDataArray, Encryption.multiSha1(("7d2510b1a6dd84a3121e62b4c4050949" + Integer.toOctalString(sessionUserId) + f.getAbsolutePath() + System.getProperty("os.name") + System.getProperty("user.name") + System.getProperty("user.home")).getBytes(),1000));
			os.write(encodedData);
			Random r = new Random(sessionUserId ^ Integer.parseInt("100011001111010001111010001", 2));
			byte[] shitload = new byte[20];
			r.nextBytes(shitload);
			os.write(shitload);
		} catch(Exception e) {
			if(Main.TEST)
				e.printStackTrace();
		} finally {
			Util.close(os);
		}
	}
	
	public static void loadSession() {
		try {
			// Ensure no one replaced current threads class and security manager is in place
			if(!Class.forName("java.lang.Thread").getMethod("getStackTrace").equals(Thread.currentThread().getClass().getMethod("getStackTrace")))
				throw new RuntimeException();
			Encryption.getSecurityManager();
		} catch(Exception e) {
			Encryption.throwMajicError();
			return;
		}
		DataInputStream is = null;
		try {
			File f = new File("user.dat");
			if(!f.isFile())
				return;
			is = new DataInputStream(new FileInputStream(f));
			sessionUserId = is.readInt() ^ Integer.parseInt("111011001110011101011010101", 2);
			byte[] encodedData = new byte[1008];
			is.readFully(encodedData);
			byte[] decodedData = Encryption.decrypt(encodedData, Encryption.multiSha1(("7d2510b1a6dd84a3121e62b4c4050949" + Integer.toOctalString(sessionUserId) + f.getAbsolutePath() + System.getProperty("os.name") + System.getProperty("user.name") + System.getProperty("user.home")).getBytes(),1000));
			DataInputStream decodedIs = new DataInputStream(new ByteArrayInputStream(decodedData));
			sessionUser = decodedIs.readUTF();
			if(sessionKeyAddress == -1)
				sessionKeyAddress = Util.getUnsafe().allocateMemory(512);
			byte[] sessionKey = new byte[512];
			decodedIs.readFully(sessionKey);
			if((decodedIs.readInt() ^ 1) != 1028)
				throw new IOException();
			if(!decodedIs.readUTF().equals("dd"))
				throw new IOException();
			Util.close(decodedIs); // It is just polite to close streams
			if(Main.TEST) {
				System.out.println("~~~ Session loaded. ~~~");
				System.out.println("Userid: " + sessionUserId + ", user: " + sessionUser + ", key: ");
				System.out.println(Util.byteArrayToHex(sessionKey));
			}
			for(int i = 0; i < sessionKey.length; ++i)
				Util.getUnsafe().putByte(sessionKeyAddress + i, sessionKey[i]);
		} catch(Exception e) {
			if(Main.TEST)
				e.printStackTrace();
			if(sessionKeyAddress != -1) {
				Util.getUnsafe().freeMemory(sessionKeyAddress);
				sessionKeyAddress = -1;
			}
			sessionUser = null;
			sessionUserId = 0;
		} finally {
			Util.close(is);
		}
	}
	
	public static void init() {
		SecurityManager sm = new SecurityManager() {
			@Override
			public void checkPermission(Permission perm) {
				try {
					if(!Class.forName("java.lang.Thread").getMethod("getStackTrace").equals(Thread.currentThread().getClass().getMethod("getStackTrace")))
						throw new RuntimeException();
				} catch(Exception e) {
					Encryption.throwMajicError();
					return;
				}
				if(perm.getName().equals("setSecurityManager"))
					Encryption.throwMajicError();
			}
			
			@Override
			public void checkMemberAccess(Class<?> clazz, int which) {
				if(clazz == LauncherOptions.class) {
					StackTraceElement[] stes = Thread.currentThread().getStackTrace();
					if(!stes[3].getClassName().equals(LauncherOptions.class.getName()))
						Encryption.throwMajicError();
				}
			}
		};
		System.setSecurityManager(sm);
		Encryption.setSecurityManager(sm);
	}
	
	static {
		if(Integer.parseInt("68") != ("D".getBytes()[0] & 0xFF))
			Encryption.throwMajicError();
	}
}
