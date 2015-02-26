package org.greencubes.launcher;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Member;
import java.nio.ByteBuffer;
import java.security.Permission;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.greencubes.client.Client;
import org.greencubes.download.Downloader;
import org.greencubes.main.Main;
import org.greencubes.swing.MacOSX;
import org.greencubes.util.Encryption;
import org.greencubes.util.OperatingSystem;
import org.greencubes.util.Util;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * <p>Most protected and encrypted class. Do not touch
 * if you are not an author.</p>
 * <p>Also, most unreadable. It is for security reasons, trust me!</p>
 * 
 * @author Rena
 */
public class LauncherOptions {
	
	private static List<BufferedImage> icons;
	private static ThreadLocal<Downloader> threadLocalDownloader = new ThreadLocal<Downloader>() {
		@Override
		protected Downloader initialValue() {
			return newDownloader();
		}
	};
	private static Map<Client, Downloader> clientDownloaders = new HashMap<Client, Downloader>();
	
	public static boolean debug = false;
	public static boolean noUpdateLauncher = false;
	public static OnStartAction onClientStart;
	public static boolean autoLogin = false;
	public static boolean showLocalServer = false;
	public static boolean autoUpdate = false;
	public static OnStartAction onLauncherClose = OnStartAction.CLOSE;
	public static OnStartAction onLauncherMinimize = OnStartAction.MINIMIZE;
	
	private static long sessionKeyAddress = -1;
	public static String sessionId;
	public static String sessionUser;
	public static int sessionUserId;
	public static JSONObject userInfo;
	
	public static Downloader getClientDownloader(Client client) {
		synchronized(clientDownloaders) {
			Downloader d = clientDownloaders.get(client);
			if(d == null) {
				d = newClientDownloader();
				clientDownloaders.put(client, d);
			}
			return d;
		}
	}
	
	public static Downloader getDownloader() {
		return threadLocalDownloader.get();
	}
	
	private static Downloader newDownloader() {
		Downloader downloader;
		if(Main.TEST) {
			downloader = new Downloader("https://greencubes.org/login/"); // For test purposes
		} else {
			downloader = new Downloader("https://auth.greencubes.org/");
			downloader.addServer("https://auth1.greencubes.org/");
		}
		return downloader;
	}
	
	private static Downloader newClientDownloader() {
		Downloader downloader;
		downloader = new Downloader("https://greencubes.org/login/");
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
				if(Main.TEST)
					e.printStackTrace();
			}
			if(OperatingSystem.getCurrentPlatform() == OperatingSystem.OSX)
				MacOSX.setIcons(icons);
		}
		return icons;
	}
	
	public static void setSession(int userId, String userName, byte[] key) {
		if(sessionKeyAddress == -1)
			sessionKeyAddress = Util.getUnsafe().allocateMemory(128);
		for(int i = 0; i < key.length; ++i) {
			Util.getUnsafe().putByte(sessionKeyAddress + i, key[i]);
			key[i] = -1;
		}
		sessionUser = userName;
		sessionUserId = userId;
		try {
			Main.getConfig().put("user", userName);
			Main.getConfig().put("login", autoLogin);
		} catch(JSONException e) {}
	}
	
	public static void auth(String userName, char[] password) throws IOException, AuthError {
		Map<String, String> post = new HashMap<String, String>();
		post.put("user", String.valueOf(userName));
		post.put("passwordE1", Base64.encodeBase64String(new String(password).getBytes()));
		String answer = getDownloader().readURL("login.php", post);
		JSONObject jo;
		try {
			jo = new JSONObject(answer);
		} catch(JSONException e) {
			throw new IOException("Wrong response: " + answer, e);
		}
		if(jo.optInt("response", -1) != 0)
			throw new AuthError(jo.optInt("response", -1), jo.optString("message") + " (" + jo.optInt("response", -1) + ")");
		setSession(jo.optInt("userid"), jo.optString("username"), jo.optString("key").getBytes());
		sessionId = jo.optString("session");
		saveSession();
	}
	
	public static void logOff() {
		try {
			// Ensure no one replaced current threads class and security manager is in place
			if(!Class.forName("java.lang.Thread").getMethod("getStackTrace").equals(Thread.currentThread().getClass().getMethod("getStackTrace")))
				throw new RuntimeException();
			Encryption.getSecurityManager();
		} catch(Exception e) {
			Encryption.throwMajicError();
			return;
		}
		if(sessionKeyAddress != -1) {
			if(sessionUserId > 0) {
				byte[] sessionKey = new byte[128];
				for(int i = 0; i < 128; ++i)
					sessionKey[i] = Util.getUnsafe().getByte(sessionKeyAddress + i);
				try {
					Map<String, String> post = new HashMap<String, String>();
					post.put("user", String.valueOf(sessionUserId));
					post.put("key", new String(sessionKey));
					post.put("drop", "1");
					getDownloader().readURL("login.php", post);
					// We are not so interested in answer
				} catch(IOException e) {
				}
			}
			Util.getUnsafe().freeMemory(sessionKeyAddress);
			sessionKeyAddress = -1;
		}
		sessionUserId = 0;
		try {
			Main.userFileChannel.position(0);
			Main.userFileChannel.truncate(0);
		} catch(IOException e) {
			if(Main.TEST)
				e.printStackTrace();
		}
	}
	
	public static void authSession() throws IOException, AuthError {
		if(sessionUserId <= 0 || sessionKeyAddress == -1 || sessionUser == null)
			return;
		try {
			// Ensure no one replaced current threads class and security manager is in place
			if(!Class.forName("java.lang.Thread").getMethod("getStackTrace").equals(Thread.currentThread().getClass().getMethod("getStackTrace")))
				throw new RuntimeException();
			Encryption.getSecurityManager();
		} catch(Exception e) {
			Encryption.throwMajicError();
			return;
		}
		byte[] sessionKey = new byte[128];
		for(int i = 0; i < 128; ++i)
			sessionKey[i] = Util.getUnsafe().getByte(sessionKeyAddress + i);
		Map<String, String> post = new HashMap<String, String>();
		post.put("user", String.valueOf(sessionUserId));
		post.put("key", new String(sessionKey));
		String answer = getDownloader().readURL("login.php", post);
		JSONObject jo;
		try {
			jo = new JSONObject(answer);
		} catch(JSONException e) {
			throw new IOException("Wrong response: " + answer, e);
		}
		if(jo.optInt("response", -1) != 0)
			throw new AuthError(jo.optInt("response", -1), jo.optString("message") + " (" + jo.optInt("response", -1) + ")");
		setSession(jo.optInt("userid"), jo.optString("username"), jo.optString("key").getBytes());
		sessionId = jo.optString("session");
		saveSession();
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
		if(sessionUserId <= 0 || sessionKeyAddress == -1 || sessionUser == null || !autoLogin)
			return;
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream os = new DataOutputStream(bos);
			File f = new File("user.dat");
			os.writeInt(sessionUserId ^ Integer.parseInt("111011001110011101011010101", 2));
			ByteArrayOutputStream decodedData = new ByteArrayOutputStream();
			DataOutputStream decodedOs = new DataOutputStream(decodedData);
			decodedOs.writeUTF(sessionUser);
			byte[] sessionKey = new byte[128];
			for(int i = 0; i < 128; ++i)
				sessionKey[i] = Util.getUnsafe().getByte(sessionKeyAddress + i);
			decodedOs.write(sessionKey);
			decodedOs.writeInt(1028 ^ 1);
			decodedOs.writeUTF("dd");
			Random r = new Random(sessionUserId ^ Integer.parseInt("100011001111010001111010001", 2) * sessionUser.hashCode() + sessionKey[0] * sessionKey[1] - sessionKey[12] ^ sessionKey[100]);
			while(decodedOs.size() < 256)
				decodedOs.write(r.nextInt());
			decodedOs.close(); // It is just polite to close streams
			byte[] decodedDataArray = decodedData.toByteArray();
			byte[] encodedData = Encryption.encrypt(decodedDataArray, Encryption.secureMultiSha384(("7d2510b1a6dd84a3121e62b4c4050949" + Integer.toOctalString(sessionUserId) + f.getAbsolutePath() + System.getProperty("os.name") + System.getProperty("user.name") + System.getProperty("user.home")).getBytes(), 1000));
			os.writeShort(encodedData.length ^ ~256);
			os.write(encodedData);
			byte[] shitload = new byte[Math.max(0, 1024 - os.size())];
			byte[] shad = encodedData;
			for(int i = 0; i < shitload.length;) {
				shad = Encryption.secureSha384(shad);
				for(int i1 = 0; i1 < shad.length && i < shitload.length; ++i1) {
					shitload[i++] = (byte) (shad[i1]);
				}
			}
			os.write(shitload);
			Main.userFileChannel.position(0);
			ByteBuffer buf = ByteBuffer.wrap(bos.toByteArray());
			while(buf.hasRemaining()) {
				Main.userFileChannel.write(buf);
			}
		} catch(Exception e) {
			if(Main.TEST)
				e.printStackTrace();
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
		try {
			if(Main.userFileChannel.size() < 1024)
				return;
			Main.userFileChannel.position(0);
			ByteBuffer buf = ByteBuffer.allocate((int) Main.userFileChannel.size());
			while(Main.userFileChannel.read(buf) > 0)
				;
			buf.flip();
			byte[] file = new byte[(int) Main.userFileChannel.size()];
			buf.get(file);
			ByteArrayInputStream bis = new ByteArrayInputStream(file);
			DataInputStream is = new DataInputStream(bis);
			File f = new File("user.dat");
			sessionUserId = is.readInt() ^ Integer.parseInt("111011001110011101011010101", 2);
			byte[] encodedData = new byte[is.readShort() ^ ~256];
			is.readFully(encodedData);
			byte[] decodedData = Encryption.decrypt(encodedData, Encryption.secureMultiSha384(("7d2510b1a6dd84a3121e62b4c4050949" + Integer.toOctalString(sessionUserId) + f.getAbsolutePath() + System.getProperty("os.name") + System.getProperty("user.name") + System.getProperty("user.home")).getBytes(), 1000));
			ByteArrayInputStream decodedBis = new ByteArrayInputStream(decodedData);
			DataInputStream decodedIs = new DataInputStream(decodedBis);
			sessionUser = decodedIs.readUTF();
			if(sessionKeyAddress == -1)
				sessionKeyAddress = Util.getUnsafe().allocateMemory(128);
			byte[] sessionKey = new byte[128];
			decodedIs.readFully(sessionKey);
			if((decodedIs.readInt() ^ 1) != 1028)
				throw new IOException();
			if(!decodedIs.readUTF().equals("dd"))
				throw new IOException();
			Random r = new Random(sessionUserId ^ Integer.parseInt("100011001111010001111010001", 2) * sessionUser.hashCode() + sessionKey[0] * sessionKey[1] - sessionKey[12] ^ sessionKey[100]);
			for(int i = 0; i < 256 - 128 - 8 - 2 - sessionUser.length(); ++i) {
				if((r.nextInt() & 0xFF) != decodedIs.read())
					throw new IOException();
			}
			Util.close(decodedIs); // It is just polite to close streams
			int shitloadLen = Math.max(0, 1024 - encodedData.length - 6);
			byte[] shad = encodedData;
			for(int i = 0; i < shitloadLen;) {
				shad = Encryption.secureSha384(shad);
				for(int i1 = 0; i1 < shad.length && i < shitloadLen; ++i1) {
					if(is.readByte() != shad[i1])
						throw new IOException();
					i++;
				}
			}
			if(Main.TEST) {
				System.out.println("~~~ Session loaded. ~~~");
				System.out.println("From " + new File("user.dat").getAbsolutePath());
				System.out.println("Userid: " + sessionUserId + ", user: " + sessionUser + ", key: ");
				System.out.println(new String(sessionKey));
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
			sessionUserId = 0;
		}
	}
	
	public static void init() {
		SecurityManager sm = new SecurityManager() {
			@Override
			public void checkPermission(Permission perm) {
				if(perm.getName().startsWith("accessClassInPackage"))
					return;
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
				// Do not check thread class access because it causes stack owerflow
				if(clazz == Thread.class || which == Member.PUBLIC)
					return;
				try {
					// Ensure that Thread class is not fake
					if(!Class.forName("java.lang.Thread").getMethod("getStackTrace").equals(Thread.currentThread().getClass().getMethod("getStackTrace")))
						throw new RuntimeException();
				} catch(Exception e) {
					Encryption.throwMajicError();
					return;
				}
				if(clazz == LauncherOptions.class) {
					StackTraceElement[] stes = Thread.currentThread().getStackTrace();
					if(!stes[3].getClassName().equals(LauncherOptions.class.getName()))
						Encryption.throwMajicError();
				} else if(clazz == Encryption.class) {
					StackTraceElement[] stes = Thread.currentThread().getStackTrace();
					if(!stes[3].getClassName().equals(Encryption.class.getName()))
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
		onClientStart = OnStartAction.NO;
		com.sun.management.OperatingSystemMXBean bean = (com.sun.management.OperatingSystemMXBean) java.lang.management.ManagementFactory.getOperatingSystemMXBean();
		long max = bean.getTotalPhysicalMemorySize();
		if(max < 3L * 1024L * 1024L * 1024L) {
			if(Main.TEST)
				System.out.println("Less than 3Gb memory, setting default start action to close.");
			onClientStart = OnStartAction.CLOSE;
		}
	}

	public static enum OnStartAction {
		CLOSE("settings.onstart.close"), MINIMIZE("settings.onstart.minimize"), HIDE("settings.onstart.hide"), NO("settings.onstart.no");
		
		public final String langKey;
		
		private OnStartAction(String langKey) {
			this.langKey = langKey;
		}
	}
}
