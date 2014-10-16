package org.greencubes.client;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.cef.browser.CefBrowser;

public class ClientNew extends Client {
	
	public ClientNew(String name, String localizedName) {
		super(name, localizedName);
	}
	
	@Override
	public File getWorkingDirectory() {
		return new File("");
	}
	
	@Override
	public List<String> getLaunchParameters(String username, String session, Server server) {
		List<String> params = new ArrayList<String>();
		params.add("--fullscreen");
		if(username != null) {
			params.add("--session");
			params.add(username);
			if(session == null)
				params.add("-");
			else
				params.add("session");
		}
		if(server != null) {
			params.add("--connect");
			params.add(server.getFullAddress());
		}
		return params;
	}
	
	@Override
	public List<Server> getServers() {
		List<Server> servers = new ArrayList<Server>();
	
		return servers;
	}
	
	@Override
	public boolean isSinglePlayerModeAllowed() {
		return false;
	}
	
	@Override
	public void openBrowserPage(CefBrowser browser) {
		browser.loadURL("https://greencubes.org");
	}
	
}
