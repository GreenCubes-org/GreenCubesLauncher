package org.greencubes.util;

import gnu.trove.TIntCollection;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TCharIntMap;
import gnu.trove.map.hash.TCharIntHashMap;
import gnu.trove.map.hash.THashMap;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

import org.greencubes.util.collections.FastList;
import org.greencubes.util.logging.LogManager;

public final class Util {

	public static final Random globalRandom = new Random();
	public static final char[] chars = "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray();
	public static final DateFormat dateFormat = new SynchronizedDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
	public static final DateFormat fileDateFormat = new SynchronizedDateFormat(new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss"));
	public static final float degree = (float) (180.0D / Math.PI);
	public static final String[] emptyStringArray = new String[]{};

	private static final Pattern stringFormat = Pattern.compile("(\247[0-9a-fA-F]|\247[usip]|\247r[0-9a-z]{8})", Pattern.CASE_INSENSITIVE);

	public static boolean isQuads(int n) {
		return (n & 3) == 0;
	}

	public static int toEstimateIntTime(long longTime) {
		return (int) ((longTime / 1000L) >> 2);
	}

	public static long fromEstimateIntTime(int intTime) {
		return 1000L * (((long) intTime) << 2);
	}

	public static float roundFloat(float a, float mod) {
		return a - (a % mod);
	}

	public static float degreeMiddle(float deg1, float deg2) {
		double r1 = Math.toRadians(degreeRound(deg1));
		double r2 = Math.toRadians(degreeRound(deg2));
		if(r1 == r2)
			return deg1;
		double x = 0;
		double y = 0;
		x += Math.cos(r1);
		y += Math.sin(r1);
		x += Math.cos(r2);
		y += Math.sin(r2);
		double deg = Math.toDegrees(TrigMath.atan2(y, x));
		if(deg < 0)
			deg += 360;
		return (float) deg;
	}

	public static float degreeDiff(float deg1, float deg2) {
		float par0 = Math.abs(deg1 - deg2);
		if(par0 > 180)
			return 360 - par0;
		return par0;
	}

	public static float degreeRound(float deg) {
		deg %= 360.0F;
		if(deg >= 180.0F)
			deg -= 360.0F;
		if(deg < -180.0F)
			deg += 360.0F;
		return deg;
	}

	public static float roundYaw(float current, float target, float maximum) {
		float par0 = target - current;
		par0 %= 360.0F;
		if(par0 >= 180.0F)
			par0 -= 360.0F;
		if(par0 < -180.0F)
			par0 += 360.0F;
		if(par0 > maximum)
			par0 = maximum;
		else if(par0 < -maximum)
			par0 = -maximum;
		return current + par0;
	}

	public static float roundPitch(float current, float target, float maximum) {
		float par0 = target - current;
		if(par0 > maximum)
			par0 = maximum;
		else if(par0 < -maximum)
			par0 = -maximum;
		return current + par0;
	}

	public static int getRadius(int x1, int z1, int x2, int z2) {
		return (int) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(z2 - z1, 2));
	}

	public static int getUnitDiff(double loc1, double loc2) {
		return roundDouble(loc1 * 32.0D) - roundDouble(loc2 * 32.0D);
	}

	public static int roundDouble(double paramDouble) {
		int i = (int) paramDouble;
		return paramDouble < i ? i - 1 : i;
	}

	public static boolean isFloatError(float f) {
		if(Float.isNaN(f) || Float.isInfinite(f))
			return true;
		return false;
	}

	public static boolean isDoubleError(double d) {
		if(Double.isNaN(d) || Double.isInfinite(d))
			return true;
		return false;
	}

	public static int roundFloat(float paramFloat) {
		int i = (int) paramFloat;
		return paramFloat < i ? i - 1 : i;
	}

	public static int roundMax(float f, int max) {
		int i = (int) f;
		return i < max ? max : i;
	}

	public static byte yawToFace(float yaw) {
		return (byte) (((Util.roundDouble(yaw / 90.0F + 0.5D) & 0x3) + 2) % 4);
	}

	public static int getUnit(double coord) {
		return roundDouble(coord * 32.0D);
	}

	public static double getCoord(int units) {
		return units / 32.0D;
	}

	public static byte getRotation(float rotation) {
		return (byte) roundFloat(rotation * 32.0F / 45.0F);
	}

	public static float getRotation(byte rotation) {
		return rotation * 45.0F / 32.0F;
	}

	public static String arrayDump(Object[] array) {
		String s = "";
		int i = 0;
		for(Object o : array)
			s += "[" + i + "] " + o;
		return s;
	}

	public static float getMaxMod(float d1, float d2) {
		if(d1 < 0.0F)
			d1 = -d1;
		if(d2 < 0.0F)
			d2 = -d2;
		return d1 > d2 ? d1 : d2;
	}

	public static float pow(float base, float exponent) {
		float m = base < 0.0F ? -1.0F : 1.0F;
		return (float) (Math.pow(base * m, exponent) * m);
	}

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

	public static int[] range(int from, int to) {
		int[] range = new int[to - from + 1];
		int n = 0;
		for(int i = from; i <= to; ++i)
			range[n++] = i;
		return range;
	}

	public static int[] range(int from, int to, int step) {
		int[] range = new int[(to - from + 1) / step];
		int n = 0;
		for(int i = from; i <= to; i += step)
			range[n++] = i;
		return range;
	}

	public static boolean areOnOneSide(double a, double c, double b) {
		return a == c || b == c || a == b || (a > c && b > c) || (a < c && b < c);
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
			LogManager.log.severe("Error creating MD5!");
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
			LogManager.log.severe("Error creating SHA-1");
			e.printStackTrace();
		}
		return null;
	}

	public static <E> List<E> toList(E[] array) {
		List<E> ret = new FastList<E>(array.length);
		for(int i = 0; i < array.length; ++i)
			ret.add(array[i]);
		return ret;
	}

	public static <E> FastList<E> toFastList(E[] array) {
		FastList<E> ret = new FastList<E>(array.length);
		for(int i = 0; i < array.length; ++i)
			if(!ret.contains(array[i]))
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

	public static TIntList parseIntegers(String string) {
		TIntList result = new TIntArrayList(3);
		StringBuilder buffer = null;
		boolean isFloat = false;
		boolean parse = false;
		boolean negative = false;
		for(int i = 0; i < string.length(); ++i) {
			char c = string.charAt(i);
			if(c == '-') {
				if(buffer == null)
					negative = true;
				else
					parse = true;
			} else if(c == '.') {
				if(isFloat) {
					parse = true;
				} else if(buffer != null) {
					buffer.append(c);
					isFloat = true;
				}
			} else if(Character.isDigit(c)) {
				if(buffer != null)
					buffer.append(c);
				else
					buffer = new StringBuilder().append(c);
			} else if(buffer != null)
				parse = true;
			if(parse) {
				if(isFloat) {
					int n = Float.valueOf(buffer.toString()).intValue();
					if(negative)
						n *= -1;
					result.add(n);
					isFloat = false;
				} else {
					int n = Integer.valueOf(buffer.toString()).intValue();
					if(negative)
						n *= -1;
					result.add(n);
				}
				parse = false;
				buffer = null;
				negative = false;
			}
		}
		if(buffer != null) {
			if(isFloat) {
				int n = Float.valueOf(buffer.toString()).intValue();
				if(negative)
					n *= -1;
				result.add(n);
			} else {
				int n = Integer.valueOf(buffer.toString()).intValue();
				if(negative)
					n *= -1;
				result.add(n);
			}
		}
		return result;
	}

	public static int getDamerauLevenshteinDistance(String str1, String str2) {
		if(str1.length() == 0) {
			if(str2.length() == 0)
				return 0;
			else
				return str2.length();
		} else if(str2.length() == 0)
			return str1.length();
		int[][] D = new int[str1.length() + 2][str2.length() + 2];
		int INF = str1.length() + str2.length();

		D[0][0] = INF;
		for(int i = 0; i < str1.length(); ++i) {
			D[i + 1][1] = i;
			D[i + 1][0] = INF;
		}
		for(int i = 0; i < str2.length(); ++i) {
			D[1][i + 1] = i;
			D[0][i + 1] = INF;
		}

		TCharIntMap lastPosition = new TCharIntHashMap();

		for(int i = 0; i < str1.length(); ++i)
			if(!lastPosition.containsKey(str1.charAt(i)))
				lastPosition.put(str1.charAt(i), 0);
		for(int i = 0; i < str2.length(); ++i)
			if(!lastPosition.containsKey(str2.charAt(i)))
				lastPosition.put(str2.charAt(i), 0);

		for(int i = 1; i < str1.length(); ++i) {
			int last = 0;
			for(int j = 1; j < str2.length(); ++j) {
				int i1 = lastPosition.get(str2.charAt(j));
				int j1 = last;
				if(str1.charAt(i) == str2.charAt(j)) {
					D[i + 1][j + 1] = D[i][j];
					last = j;
				} else
					D[i + 1][j + 1] = Math.min(D[i][j], Math.min(D[i + 1][j], D[i][j + 1])) + 1;
				D[i + 1][j + 1] = Math.min(D[i + 1][j + 1], D[i1 + 1][j1 + 1] + (i - i1 - 1) + 1 + (j - j1 - 1));
			}
			lastPosition.put(str1.charAt(i), i);
		}
		return D[str1.length() + 1][str2.length() + 1];
	}

	public static Map<Object, Object> toMap(Object[] keys, Object[] values) {
		if(keys.length != values.length)
			throw new ArrayIndexOutOfBoundsException("Key and Values array lengths must match!");
		Map<Object, Object> map = new THashMap<Object, Object>(keys.length, 1.0f);
		for(int i = 0; i < keys.length; ++i)
			map.put(keys[i], values[i]);
		return map;
	}

	public static String clearFormat(String string) {
		return stringFormat.matcher(string).replaceAll("");
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

	public static File getAppDir(String s) {
		String s1 = System.getProperty("user.home", ".");
		File file;
		switch(EnumOS.getOS()) {
		case LINUX:
		case SOLARIS:
			file = new File(s1, (new StringBuilder()).append('.').append(s).append('/').toString());
			break;
		case WINDOWS:
			String s2 = System.getenv("APPDATA");
			if(s2 != null)
				file = new File(s2, (new StringBuilder()).append(".").append(s).append('/').toString());
			else
				file = new File(s1, (new StringBuilder()).append('.').append(s).append('/').toString());
			break;
		case MACOS:
			file = new File(s1, (new StringBuilder()).append("Library/Application Support/").append(s).toString());
			break;
		default:
			file = new File(s1, (new StringBuilder()).append(s).append('/').toString());
			break;
		}
		if(!file.exists() && !file.mkdirs())
			throw new RuntimeException((new StringBuilder()).append("The working directory could not be created: ").append(file).toString());
		else
			return file;
	}

	public static long combine(long l1, long l2) {
		return (l1 << 32) + (l2 & 0xFFFFFFFFL);
	}

	private static byte[] createChecksum(String string) throws NoSuchAlgorithmException {
		MessageDigest complete = MessageDigest.getInstance("MD5");
		byte[] buffer = string.getBytes();
		complete.update(buffer, 0, buffer.length);
		return complete.digest();
	}

	public static String getMD5Checksum(String string) throws NoSuchAlgorithmException {
		byte[] b = createChecksum(string);
		StringBuilder result = new StringBuilder(32);
		for(int i = 0; i < b.length; i++) {
			result.append(Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1));
		}
		return result.toString();
	}

	public static ArrayList<Integer> toIntegerArrayList(TIntCollection tList) {
		ArrayList<Integer> list = new ArrayList<Integer>(tList.size());
		TIntIterator iterator = tList.iterator();
		while(iterator.hasNext())
			list.add(Integer.valueOf(iterator.next()));
		return list;
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

	/**
	 * Выполняет циклический здвиг массива
	 * @param arr
	 * @param rot
	 */
	@SuppressWarnings("unchecked")
	public static <T> void shiftArray(T[] arr, int rot) {
		T[] buff = (T[]) Array.newInstance(arr.getClass().getComponentType(), arr.length);
		for(int i = 0; i < arr.length; ++i) {
			buff[(i + rot) % buff.length] = arr[i];
		}
		System.arraycopy(buff, 0, arr, 0, arr.length);
	}
}
