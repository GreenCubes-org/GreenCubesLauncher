package org.greencubes.client;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.web.WebEngine;

import javax.swing.SwingUtilities;

import org.greencubes.launcher.LauncherMain;
import org.greencubes.util.I18n;

public abstract class Client {
	
	public static final List<Client> clients = new ArrayList<Client>();
	
	public static final Client MAIN = new ClientMain("client.main", I18n.get("GreenCubes"));
	public static final Client OLD = new ClientOld("client.old", I18n.get("Старый\nклиент"));
	
	public final String name;
	public final String localizedName;
	
	protected LauncherMain launcherWindow;
	protected Server selectedServer;
	
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
	
	public abstract void updateServerList();
	
	public abstract File getWorkingDirectory();
	
	public abstract List<Server> getServers();
	
	public abstract boolean isSinglePlayerModeAllowed();
	
	public abstract void openBrowserPage(WebEngine browser);
	
	public abstract IClientStatus getStatus();
	
	public abstract void doJob();
	
	public Server getSelectedServer() {
		return selectedServer;
	}
	
	public void selectServer(Server server) {
		selectedServer = server;
	}
	
	protected void clientStatusUpdate() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				launcherWindow.clientStatusUpdate(Client.this);
			}
		});
	}
	
}
