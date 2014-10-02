package org.greencubes.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class I18n {
	
	public static Map<String, String> languageMap = new HashMap<String, String>();
	public static Locale currentLocale;
	public static String currentLanguage;
	
	public static String get(String key) {
		String s = languageMap.get(key);
		return s == null ? key : s;
	}
	
	public static String get(String key, Object ... args) {
		String s = languageMap.get(key);
		return s == null ? key : String.format(key, args);
	}
	
	static {
		// Load language
	}
}
