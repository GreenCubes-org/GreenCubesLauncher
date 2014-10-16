package org.greencubes.main;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.security.Security;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.swing.Timer;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.cef.CefApp;
import org.greencubes.launcher.LauncherOptions;
import org.greencubes.launcher.LauncherUpdate;
import org.greencubes.util.Encryption;
import org.greencubes.util.Util;
import org.json.JSONObject;
import org.json.JSONTokener;

public class Main {
	
	public static final String PASSWORD_RECOVER_URL = "https://greencubes.org/?action=recover#t";
	public static final String REGISTRATION_URL = "https://greencubes.org/?action=start";
	public static final String IPV4STACK = "-Djava.net.preferIPv4Stack=true";
	public static final boolean IS_64_BIT_JAVA;
	public static final boolean TEST = true;
	
	private static JSONObject config;
	
	public static RandomAccessFile userFile;
	public static FileChannel userFileChannel;
	
	public static void main(String[] args) {
		// Check arguments
		for(String arg : args) {
			if(arg.equals("-debug"))
				LauncherOptions.debug = true;
			if(arg.equals("-noupdate"))
				LauncherOptions.noUpdateLauncher = true;
		}
		// Load fonts
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
		Security.addProvider(new BouncyCastleProvider());
		/*
		// Some debug stuff
		try {
			BufferedImage bi = ImageIO.read(new File("screenshot-2014-10-02_99-99-99_1.png"));
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
			ImageIO.write(bi, "png", new File("screenshot-2014-10-02_99-99-99_1.encoded.png"));
		} catch(Exception e1) {
			e1.printStackTrace();
		}
		*/
		// Prevent launching of more than one launcher
		try {
			userFile = new RandomAccessFile("user.dat", "rw");
			FileLock fileLock = null;
			userFileChannel = userFile.getChannel();
			fileLock = userFileChannel.tryLock();
			if(fileLock == null) {
				System.err.println("Only one instance of launcher can be started");
				return;
			}
		} catch(Exception e) {
			return;
		}
		// Read config
		InputStream is = null;
		try {
			is = new FileInputStream(new File(Util.getAppDir("GreenCubes"), "launch.conf"));
			config = new JSONObject(new JSONTokener(is));
			LauncherOptions.sessionUser = config.optString("user");
			LauncherOptions.autoLogin = config.optBoolean("login");
		} catch(Exception e) {
			config = new JSONObject();
		} finally {
			Util.close(is);
		}
		if(LauncherOptions.sessionUser == null && new File("launcher.dat").exists()) {
			// Try pick user login from old launcher configs
			// And hope some idiot will try to decrypt this code not the real session load/save one
			DataInputStream dis = null;
			try {
				Cipher cipher = getCipher(2, "c8d3563578b9264ee7fc86d44bbb9a79");
				if(cipher == null)
					return;
				dis = new DataInputStream(new CipherInputStream(new FileInputStream(new File("launcher.dat")), cipher));
				LauncherOptions.sessionUser = dis.readUTF().substring(5);
				dis.close();
				new File("launcher.dat").delete();
			} catch(Throwable t) {
				// Ignore any exception as this is not important
			} finally {
				Util.close(dis);
			}
		}
		// Apply config
		if(config.optBoolean("debug"))
			LauncherOptions.debug = true;
		LauncherOptions.onClientStart = LauncherOptions.OnStartAction.values()[config.optInt("onstart", LauncherOptions.onClientStart.ordinal())];
		
		// Load saved session
		new LauncherUpdate();
	}
	
	public static JSONObject getConfig() {
		return config;
	}
	
	public static void close() {
		Util.close(userFileChannel, userFile);
		try {
			FileWriter fw = new FileWriter(new File(Util.getAppDir("GreenCubes"), "launch.conf"));
			config.write(fw);
			fw.close();
		} catch(Exception e) {
		}
		CefApp.getInstance().dispose();
		new Timer(3000, new ActionListener() {
			public void actionPerformed(ActionEvent paramAnonymous2ActionEvent) {
				System.exit(0);
			}
		}).start();
	}
	
	private static Cipher getCipher(int mode, String password) throws Exception {
		Random random = new Random(43287234L);
		byte[] salt = new byte[8];
		random.nextBytes(salt);
		PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, 5);
		SecretKey pbeKey = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(new PBEKeySpec(password.toCharArray()));
		Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");
		cipher.init(mode, pbeKey, pbeParamSpec);
		return cipher;
	}
	
	static {
		String[] opts = new String[]{"sun.arch.data.model", "com.ibm.vm.bitmode", "os.arch"};
		boolean is64bit = false;
		for(String opt : opts) {
			String val = System.getProperty(opt);
			if(val != null && val.contains("64")) {
				is64bit = true;
				break;
			}
		}
		IS_64_BIT_JAVA = is64bit;
	}
}
