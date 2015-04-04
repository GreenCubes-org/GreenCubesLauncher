package org.greencubes.client;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.scene.web.WebEngine;

import javax.swing.SwingUtilities;

import org.greencubes.launcher.LauncherMain;
import org.greencubes.util.I18n;

public abstract class Client {
	
	public static final List<Client> clients = new ArrayList<Client>();
	
	public static final ClientMain MAIN = new ClientMain("client.main", I18n.get("GreenCubes"));
	public static final ClientOld OLD = new ClientOld("client.old", I18n.get("Старый\nклиент"));
	public static final ClientMainTest TEST = new ClientMainTest("client.main.test", I18n.get("Тестовый\nклиент"));
	
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
	
	public abstract IClientStatus getStatus();
	
	public abstract void doJob();
	
	protected abstract String getUrlName();
	
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

	public void openBrowserPage(final WebEngine browser) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				browser.load("https://greencubes.org/" + I18n.getLangKey() + "/?action=clientpage&client=" + getUrlName() + "&change_lang=" + I18n.getLangKey());
			}
		});
	}
}
