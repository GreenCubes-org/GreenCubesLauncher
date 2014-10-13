package org.greencubes.util;

import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.greencubes.main.Main;
import org.json.JSONObject;
import org.json.JSONTokener;

public class I18n {
	
	public static final String DEFAULT_LANG = "ruRU";
	public static final String[] supportedLanguages = new String[] {"ruRU"};
	
	public static Map<String, String> langMap = new HashMap<String, String>();
	public static Locale currentLocale;
	public static String currentLanguage;
	private static SimpleDateFormat localizedDateFormat;
	
	public static Locale getCurrentLocale() {
		return currentLocale;
	}
	
	public static String getLang() {
		return currentLanguage;
	}
	
	public static String getLocalizedDate(long time) {
		return getLocalizedDate(new Date(time));
	}
	
	public static synchronized String getLocalizedDate(Date date) {
		return localizedDateFormat.format(date);
	}
	
	public static boolean hasLang(String key) {
		return langMap.containsKey(key);
	}
	
	public static String get(String key) {
		String s = langMap.get(key);
		return s == null ? key : s;
	}
	
	public static String get(String key, Object ... args) {
		String s = langMap.get(key);
		return s == null ? key : String.format(s, args);
	}
	
	private static boolean isSupportedLang(String lang) {
		for(String l : supportedLanguages)
			if(l.equals(lang))
				return true;
		return false;
	}
	
	private static void loadLanguage(String lang) {
		String l = Locale.getDefault().getLanguage();
		String c = Locale.getDefault().getCountry();
		if(isSupportedLang(l + c.toUpperCase())) {
			currentLanguage = l + c.toUpperCase();
		} else {
			for(String sl : supportedLanguages) {
				if(sl.startsWith(l)) {
					currentLanguage = sl;
					break;
				}
			}
		}
		if(!isSupportedLang(currentLanguage))
			currentLanguage = DEFAULT_LANG;
		JSONObject jo;
		try {
			URL langUrl = I18n.class.getResource("/res/lang/" + currentLanguage + ".js");
			InputStream is = langUrl.openStream();
			jo = new JSONObject(new JSONTokener(is));
			is.close();
		} catch(Exception e) {
			throw new AssertionError(e);
		}
		Iterator<Entry<String,Object>> iterator = jo.getMap().entrySet().iterator();
		while(iterator.hasNext()) {
			Entry<String,Object> e = iterator.next();
			langMap.put(e.getKey(), Util.toString(e.getValue()));
		}
		currentLocale = new Locale(currentLanguage.substring(0, 2), currentLanguage.substring(2, 4));
		localizedDateFormat = new SimpleDateFormat("d MMMM yyyy", currentLocale);
	}
	
	static {
		loadLanguage(Main.getConfig().optString("lang", null));
	}
}
