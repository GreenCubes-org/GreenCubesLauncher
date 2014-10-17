package org.greencubes.client;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.cef.browser.CefBrowser;

public abstract class Client {
	
	public static final List<Client> clients = new ArrayList<Client>();
	
	public final String name;
	public final String localizedName;
	
	protected Client(String name, String localizedName) {
		this.name = name;
		this.localizedName = localizedName;
		clients.add(this);
	}
	
	public abstract File getWorkingDirectory();
	
	/**
	 * All parameters can be null!
	 * @param username
	 * @param session
	 * @param server
	 * @return
	 */
	public abstract List<String> getLaunchParameters(String username, String session, Server server);
	
	public abstract List<Server> getServers();
	
	public abstract boolean isSinglePlayerModeAllowed();
	
	public abstract void openBrowserPage(CefBrowser browser);
	
	public abstract IClientStatus getStatus();
	
}
