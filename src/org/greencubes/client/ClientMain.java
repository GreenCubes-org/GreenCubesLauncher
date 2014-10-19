package org.greencubes.client;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.cef.browser.CefBrowser;
import org.greencubes.launcher.LauncherMain;
import org.greencubes.launcher.LauncherOptions;
import org.greencubes.main.Main;

public class ClientMain extends Client {
	
	private final IClientStatus status;
	private final List<Server> servers = new ArrayList<Server>();
	
	public ClientMain(String name, String localizedName) {
		super(name, localizedName);
		status = new MainClinetStatus();
		if(LauncherOptions.showLocalServer || Main.TEST)
			servers.add(new Server("Local", "127.0.0.1", 25565));
		servers.add(new Server("GreenCubes", "5.9.22.202", 25565));
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

	@Override
	public IClientStatus getStatus() {
		return status;
	}
	
	@Override
	public void load(LauncherMain launcherWindow) {
		super.load(launcherWindow);
	}
	
	public class MainClinetStatus implements IClientStatus {

		@Override
		public Status getStatus() {
			return Status.CHECK;
		}

		@Override
		public String getStatusTitle() {
			return "Требуется обновление";
		}

		@Override
		public float getStatusProgress() {
			return 0;
		}
	}
}
