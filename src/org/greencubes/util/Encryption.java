package org.greencubes.util;

import java.lang.reflect.ReflectPermission;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.engines.BlowfishEngine;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.greencubes.main.Main;

public class Encryption {
	
	private static SecurityManager sm;
	
	public static void setSecurityManager(SecurityManager smNew) {
		if(sm != null)
			Encryption.throwMajicError();
		try {
			sm.checkPermission(new ReflectPermission("suppressAccessChecks"));
			Encryption.throwMajicError();
		} catch(Exception e) {
			// There should be exception
		}
		sm = smNew;
	}
	
	public static SecurityManager getSecurityManager() {
		if(sm == null)
			Encryption.throwMajicError();
		return sm;
	}
	
	public static byte[] decrypt(byte[] data, byte[] keyData) throws Exception {
		BlowfishEngine engine = new BlowfishEngine();
		PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(engine);
		KeyParameter key = new KeyParameter(keyData);
		cipher.init(false, key);
		byte out2[] = new byte[cipher.getOutputSize(data.length)];
		int len2 = cipher.processBytes(data, 0, data.length, out2, 0);
		cipher.doFinal(out2, len2);
		return out2;
	}
	
	public static byte[] encrypt(byte[] data, byte[] keyData) throws Exception {
		BlowfishEngine engine = new BlowfishEngine();
		PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(engine);
		KeyParameter key = new KeyParameter(keyData);
		cipher.init(true, key);
		byte out[] = new byte[cipher.getOutputSize(data.length)];
		int len1 = cipher.processBytes(data, 0, data.length, out, 0);
		try {
			cipher.doFinal(out, len1);
		} catch(CryptoException e) {
			Encryption.throwMajicError();
			return null;
		}
		return out;
	}
	
	public static void throwMajicError() {
		// Throw error without stacktrace
		Throwable t = new Error();
		if(!Main.TEST)
			t.setStackTrace(new StackTraceElement[]{new StackTraceElement("Native", "Unknown", "NativeHandler.java", -2)});
		throw (Error) t;
	}
	
	public static byte[] multiSha1(byte[] data, int count) {
		try {
			MessageDigest digest = java.security.MessageDigest.getInstance("SHA-1");
			for(int i = 0; i < count; ++i) {
				digest.update(data);
				data = digest.digest();
				digest.reset();
			}
			return data;
		} catch(NoSuchAlgorithmException e) {
			throwMajicError();
		}
		return null;
	}
	
	public static byte[] sha1(byte[] data) {
		try {
			MessageDigest digest = java.security.MessageDigest.getInstance("SHA-1");
			digest.update(data);
			return digest.digest();
		} catch(NoSuchAlgorithmException e) {
			throwMajicError();
		}
		return null;
	}
	
	public static void init() {
		// Some security checks to ensure this class is not altered
		if(Integer.parseInt("116") != ("t".getBytes()[0] & 0xFF))
			throwMajicError();
		// Empty method to initialize class when it is needed
	}
}
