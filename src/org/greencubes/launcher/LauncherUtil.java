package org.greencubes.launcher;

import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.greencubes.main.Main;
import org.json.JSONException;
import org.json.JSONObject;

public class LauncherUtil {
	
	public static boolean canOpenBrowser() {
		return Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Action.BROWSE);
	}
	
	public static void onenURLInBrowser(String url) {
		if(canOpenBrowser()) {
			try {
				Desktop.getDesktop().browse(URI.create(url));
			} catch(IOException e) {
				if(Main.TEST)
					e.printStackTrace();
			}
		}
	}
	
	public static JSONObject sessionRequest(String args) throws IOException, AuthError {
		JSONObject answer;
		do {
			Map<String,String> post = new HashMap<String,String>();
			post.put("user", String.valueOf(LauncherOptions.sessionUserId));
			post.put("session", LauncherOptions.sessionId);
			String read = LauncherOptions.getDownloader().readURL("login.php?" + args, post);
			try {
				answer = new JSONObject(read);
			} catch(JSONException e) {
				throw new IOException("Wrong response: " + read, e);
			}
		} while(!checkAnswer(answer));
		return answer;
	}
	
	public static boolean checkAnswer(JSONObject answer) throws IOException, AuthError {
		int r = answer.optInt("response", -1);
		if(r == 0)
			return true;
		if(r == -1)
			throw new IOException("Wrong response: " + answer.optInt("response", -1));
		if(r == 1)
			throw new IOException("Server error: " + answer.optString("message") + " (" + answer.optInt("response", -1) + ")");
		if(r == 4) {
			LauncherOptions.authSession();
			return false;
		}
		throw new AuthError(answer.optInt("response", -1), answer.optString("message") + " (" + answer.optInt("response", -1));
	}
	
}
