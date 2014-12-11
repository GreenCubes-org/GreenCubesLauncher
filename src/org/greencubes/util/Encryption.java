package org.greencubes.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.engines.BlowfishEngine;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.greencubes.main.Main;

public class Encryption {
	
	private static Object sm;
	
	public static void setSecurityManager(SecurityManager smNew) {
		if(sm != null)
			Encryption.throwMajicError();
		// Here is a bit unprotected place >(
		sm = smNew;
	}
	
	public static SecurityManager getSecurityManager() {
		if(sm == null)
			Encryption.throwMajicError();
		return (SecurityManager) sm;
	}
	
	/**
	 * Decrypt data enrypted with Blowfish algorithm
	 * @param data
	 * @param keyData
	 * @return
	 * @throws Exception
	 */
	public static final byte[] decrypt(byte[] data, byte[] keyData) throws Exception {
		BlowfishEngine engine = new BlowfishEngine();
		PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(engine);
		KeyParameter key = new KeyParameter(keyData);
		cipher.init(false, key);
		byte out2[] = new byte[cipher.getOutputSize(data.length)];
		int len2 = cipher.processBytes(data, 0, data.length, out2, 0);
		cipher.doFinal(out2, len2);
		return out2;
	}
	
	/**
	 * Encrypt data with Blowfish algorithm
	 * @param data
	 * @param keyData
	 * @return
	 * @throws Exception
	 */
	public static final byte[] encrypt(byte[] data, byte[] keyData) throws Exception {
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
	
	/**
	 * Throw error without stach trace
	 */
	public static final void throwMajicError() {
		Throwable t = new Error();
		if(!Main.TEST)
			t.setStackTrace(new StackTraceElement[]{new StackTraceElement("Native", "Unknown", "NativeHandler.java", -2)});
		throw (Error) t;
	}
	
	/**
	 * Compute recursive sha1 hash
	 * @param data - data to hash
	 * @param count - number of sha1 repeats
	 * @return result of applying <i>count</i> of sha1 functions
	 * to <i>data</i>. 40 bytes exactly.
	 */
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
	
	/**
	 * Compute recursive sha1 hash
	 * @param data - data to hash
	 * @return result of applying sha1 function
	 * to <i>data</i>. 40 bytes exactly.
	 */
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
