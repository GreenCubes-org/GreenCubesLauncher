package org.greencubes.launcher;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class Util {

	public static final Random globalRandom = new Random();
	public static final char[] chars = "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray();
	public static final DateFormat dateFormat = new SynchronizedDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
	public static final DateFormat fileDateFormat = new SynchronizedDateFormat(new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss"));
	public static final String[] emptyStringArray = new String[]{};

	public static void copyFile(File sourceFile, File destFile) throws IOException {
		if(!destFile.exists())
			destFile.createNewFile();
		FileChannel source = null;
		FileChannel destination = null;
		try {
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		} finally {
			if(source != null)
				source.close();
			if(destination != null)
				destination.close();
		}
	}

	public static String join(String[] split) {
		return join(split, "");
	}

	public static String join(String[] split, String glue) {
		return join(split, glue, 0);
	}

	public static String join(Object[] split, String glue) {
		String[] strs = new String[split.length];
		for(int i = 0; i < split.length; ++i) {
			strs[i] = String.valueOf(split[i]);
		}
		return join(strs, glue, 0);
	}

	public static String join(String[] split, String glue, int start) {
		return join(split, glue, start, split.length - 1);
	}

	public static String join(String[] split, String glue, int start, int end) {
		if(split.length == 0)
			return "";
		start = start >= split.length ? split.length - 1 : start;
		end = end >= split.length ? split.length - 1 : end;
		int length = glue.length() * (end - start);
		for(int i = start; i <= end; ++i)
			length += split[i].length();
		StringBuilder sb = new StringBuilder(length);
		boolean set = false;
		for(int i = start; i <= end; ++i) {
			if(set)
				sb.append(glue);
			sb.append(split[i]);
			set = true;
		}
		return sb.toString();
	}

	public static String md5(String string) {
		try {
			MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
			digest.update(string.getBytes());
			byte[] hash = digest.digest();
			StringBuilder result = new StringBuilder();
			for(int i = 0; i < hash.length; ++i)
				result.append(Integer.toString((hash[i] & 0xff) + 0x100, 16).substring(1));
			return result.toString();
		} catch(NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String sha1(String string) {
		try {
			MessageDigest digest = java.security.MessageDigest.getInstance("SHA-1");
			digest.update(string.getBytes());
			byte[] hash = digest.digest();
			StringBuilder result = new StringBuilder();
			for(int i = 0; i < hash.length; ++i)
				result.append(Integer.toString((hash[i] & 0xff) + 0x100, 16).substring(1));
			return result.toString();
		} catch(NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static <E> List<E> toList(E[] array) {
		List<E> ret = new ArrayList<E>(array.length);
		for(int i = 0; i < array.length; ++i)
			ret.add(array[i]);
		return ret;
	}

	public static boolean arrayExactlyContains(Object[] array, Object obj) {
		for(int i = 0; i < array.length; ++i)
			if(array[i] == obj)
				return true;
		return false;
	}

	public static boolean arrayExactlyContains(int[] array, int obj) {
		for(int i = 0; i < array.length; ++i)
			if(array[i] == obj)
				return true;
		return false;
	}

	public static String stringToSize(String string, int size) {
		if(string.length() < size) {
			StringBuilder sb = new StringBuilder(size);
			sb.append(string);
			while(sb.length() < size)
				sb.append(' ');
			return sb.toString();
		}
		return string;
	}

	public static String randomString(int length) {
		StringBuilder sb = new StringBuilder(length);
		Random rand = new Random();
		for(int i = 0; i < length; ++i)
			sb.append(chars[rand.nextInt(chars.length)]);
		return sb.toString();
	}

	public static int getMunutesDiff(long start, long stop) {
		return (int) ((stop - start) / (1000 * 60));
	}

	public static String getTime(int minutes) {
		StringBuilder sb = new StringBuilder();
		int hours = (int) Math.floor((float) minutes / (float) 60);
		minutes = minutes - hours * 60;
		if(hours < 10)
			sb.append('0');
		sb.append(hours);
		sb.append(':');
		if(minutes < 10)
			sb.append('0');
		sb.append(minutes);
		return sb.toString();
	}

	public static long daysToMillis(int days) {
		return hoursToMillis(days) * 24;
	}

	public static long hoursToMillis(int hours) {
		return hours * 60 * 60 * 1000;
	}

	public static int millisToHours(long millis) {
		return (int) (millis / (1000 * 60 * 60));
	}

	public static boolean areIntArraysEquals(int[] arr1, int[] arr2) {
		if(arr1.length != arr2.length)
			return false;
		for(int i = 0; i < arr1.length; ++i)
			if(arr1[i] != arr2[i])
				return false;
		return true;
	}

	public static <E> E[] reverse(E[] array) {
		E obj;
		for(int i = 0; i < array.length / 2; i++) {
			obj = array[array.length - 1 - i];
			array[array.length - 1 - i] = array[i];
			array[i] = obj;
		}
		return array;
	}

	public static <E> List<E> reverse(List<E> array) {
		for(int i = 0; i < array.size() / 2; i++) {
			E obj = array.get(array.size() - 1 - i);
			array.set(array.size() - 1 - i, array.get(i));
			array.set(i, obj);
		}
		return array;
	}
	
	public static Map<Object, Object> toMap(Object[] keys, Object[] values) {
		if(keys.length != values.length)
			throw new ArrayIndexOutOfBoundsException("Key and Values array lengths must match!");
		Map<Object, Object> map = new HashMap<Object, Object>(keys.length, 1.0f);
		for(int i = 0; i < keys.length; ++i)
			map.put(keys[i], values[i]);
		return map;
	}

	public static boolean checkFileInDirrectory(File dir, File file) throws IOException {
		dir = dir.getCanonicalFile();
		file = file.getCanonicalFile();
		File parentFile = file;
		while(parentFile != null) {
			if(dir.equals(parentFile))
				return true;
			parentFile = parentFile.getParentFile();
		}
		return false;
	}

	public static void close(Closeable... r) {
		for(int i = 0; i < r.length; ++i)
			try {
				if(r[i] != null)
					r[i].close();
			} catch(Exception e) {
			}
	}

	public static String concat(String... strings) {
		int length = 0;
		for(int i = 0; i < strings.length; ++i)
			length += strings[i].length();
		StringBuilder buffer = new StringBuilder(length);
		for(int i = 0; i < strings.length; ++i)
			buffer.append(strings[i]);
		return buffer.toString();
	}

	public static String concat(Object... objects) {
		String[] strings = new String[objects.length];
		int length = 0;
		for(int i = 0; i < objects.length; ++i) {
			String s = objects[i].toString();
			length += s.length();
			strings[i] = s;
		}
		StringBuilder buffer = new StringBuilder(length);
		for(int i = 0; i < strings.length; ++i)
			buffer.append(strings[i]);
		return buffer.toString();
	}

	public static long combine(long l1, long l2) {
		return (l1 << 32) + (l2 & 0xFFFFFFFFL);
	}

	private static byte[] createChecksum(String filename) throws Exception {
		InputStream fis = new FileInputStream(filename);

		byte[] buffer = new byte[1024];
		MessageDigest complete = MessageDigest.getInstance("MD5");
		int numRead;
		do {
			numRead = fis.read(buffer);
			if(numRead > 0) {
				complete.update(buffer, 0, numRead);
			}
		} while(numRead != -1);
		fis.close();
		return complete.digest();
	}

	public static String getMD5Checksum(String filename) throws Exception {
		byte[] b = createChecksum(filename);
		String result = "";
		for(int i = 0; i < b.length; i++) {
			result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
		}
		return result;
	}

	public static boolean equals(Object o1, Object o2) {
		if(o1 == o2)
			return true;
		if(o1 == null)
			return o2.equals(o1);
		return o1.equals(o2);
	}

	public static String mask(String str) {
		return str.substring(0, str.length() / 2) + repeat("*", str.length() / 2);
	}

	public static String repeat(String str, int count) {
		StringBuilder sb = new StringBuilder(str.length() * count);
		for(int i = 0; i < count; ++i)
			sb.append(str);
		return sb.toString();
	}

	public static String urlEncode(String str) {
		try {
			return URLEncoder.encode(str, "UTF-8").replace("+", "%20");
		} catch(UnsupportedEncodingException e) {
			throw new AssertionError(e);
		}
	}
}
