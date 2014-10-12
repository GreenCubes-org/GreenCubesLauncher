package org.greencubes.launcher;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

public class LauncherUtil {
	
	public static JSONObject sessionRequest(String args) throws IOException, AuthError {
		JSONObject answer;
		do {
			String read = LauncherOptions.getDownloader().readURL("login/login.php?user=" + LauncherOptions.sessionUserId + "&session=" + LauncherOptions.sessionId + "&" + args);
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
		if(r == 4) {
			LauncherOptions.authSession();
			return false;
		}
		throw new AuthError(answer.optString("message") + " (" + answer.optInt("response", -1));
	}
	
}
