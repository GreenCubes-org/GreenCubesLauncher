package org.greencubes.client;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import org.cef.browser.CefBrowser;
import org.greencubes.launcher.LauncherMain;

public abstract class Client {
	
	public static final List<Client> clients = new ArrayList<Client>();
	
	public static final Client MAIN = new ClientMain("client.main", "Новый клиент");
	
	public final String name;
	public final String localizedName;
	
	protected LauncherMain launcherWindow;
	
	protected Client(String name, String localizedName) {
		this.name = name;
		this.localizedName = localizedName;
		clients.add(this);
	}
	
	/**
	 * Notifys that client is now displaying
	 */
	public void load(LauncherMain launcherWindow) {
		this.launcherWindow = launcherWindow;
	}
	
	public abstract File getWorkingDirectory();
	
	public abstract List<Server> getServers();
	
	public abstract boolean isSinglePlayerModeAllowed();
	
	public abstract void openBrowserPage(CefBrowser browser);
	
	public abstract IClientStatus getStatus();
	
	public abstract void doJob();
	
	protected void clientStatusUpdate() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				launcherWindow.clientStatusUpdate(Client.this);
			}
		});
	}
	
}
